param(
    [string]$Version = "0.1.0-SNAPSHOT",
    [string]$PackageVersion = "0.1.0",
    [string]$AppName = "dnj"
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$TargetDir = Join-Path $ProjectRoot "target"
$JPackageInputDir = Join-Path $TargetDir "jpackage-input"
$ReleaseRoot = Join-Path $TargetDir "release"
$WindowsReleaseRoot = Join-Path $ReleaseRoot "windows"
$AppImageDir = Join-Path $WindowsReleaseRoot $AppName
$DistributionZip = Join-Path $WindowsReleaseRoot "$AppName-$Version-windows-x64.zip"
$MainJar = Join-Path $TargetDir "dos-navigator-java-$Version.jar"

function Resolve-Tool {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name
    )

    $command = Get-Command $Name -ErrorAction SilentlyContinue
    if ($command) {
        return $command.Source
    }

    if ($env:JAVA_HOME) {
        $javaHomeTool = Join-Path $env:JAVA_HOME "bin\$Name.exe"
        if (Test-Path $javaHomeTool) {
            return $javaHomeTool
        }
    }

    $javaCommand = Get-Command "java" -ErrorAction SilentlyContinue
    if ($javaCommand) {
        $javaBin = Split-Path $javaCommand.Source -Parent
        $javaSiblingTool = Join-Path $javaBin "$Name.exe"
        if (Test-Path $javaSiblingTool) {
            return $javaSiblingTool
        }
    }

    throw "Cannot find $Name. Use a full JDK 21+ and make sure $Name.exe is available in PATH or JAVA_HOME\bin."
}

$mvn = Resolve-Tool "mvn"
$jpackage = Resolve-Tool "jpackage"

Push-Location $ProjectRoot
try {
    & $mvn "-DskipTests" "clean" "package"

    if (!(Test-Path $MainJar)) {
        throw "Main jar was not produced: $MainJar"
    }

    if (Test-Path $JPackageInputDir) {
        Remove-Item -LiteralPath $JPackageInputDir -Recurse -Force
    }
    if (Test-Path $WindowsReleaseRoot) {
        Remove-Item -LiteralPath $WindowsReleaseRoot -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path $JPackageInputDir | Out-Null
    New-Item -ItemType Directory -Force -Path $WindowsReleaseRoot | Out-Null
    Copy-Item -LiteralPath $MainJar -Destination (Join-Path $JPackageInputDir (Split-Path $MainJar -Leaf))

    & $jpackage `
        "--type" "app-image" `
        "--name" $AppName `
        "--app-version" $PackageVersion `
        "--dest" $WindowsReleaseRoot `
        "--input" $JPackageInputDir `
        "--main-jar" (Split-Path $MainJar -Leaf) `
        "--main-class" "org.dosnavigator.Main" `
        "--java-options" "-Dfile.encoding=UTF-8" `
        "--win-console" `
        "--vendor" "Dos Navigator Java"

    Copy-Item -LiteralPath (Join-Path $ProjectRoot "README.md") -Destination (Join-Path $AppImageDir "README.md")

    if (Test-Path $DistributionZip) {
        Remove-Item -LiteralPath $DistributionZip -Force
    }
    Compress-Archive -LiteralPath $AppImageDir -DestinationPath $DistributionZip -Force

    Write-Host "Windows release created:"
    Write-Host "  App: $AppImageDir"
    Write-Host "  Exe: $(Join-Path $AppImageDir "$AppName.exe")"
    Write-Host "  Zip: $DistributionZip"
}
finally {
    Pop-Location
}
