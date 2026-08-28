param(
    [string]$RunId = (Get-Date -Format "yyyyMMdd-HHmmss"),
    [string]$ScreenWidth = "1280",
    [string]$ScreenHeight = "900"
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$prepareScript = Join-Path $repoRoot "scripts\prepare-legacy-sandbox-capture.ps1"
$sandboxExe = "C:\Windows\System32\WindowsSandbox.exe"

& $prepareScript -RunId $RunId -ScreenWidth $ScreenWidth -ScreenHeight $ScreenHeight
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$wsbPath = Join-Path $repoRoot "target\windows-sandbox\legacy\$RunId\legacy-capture.wsb"
if (-not (Test-Path -LiteralPath $sandboxExe)) {
    throw "Windows Sandbox is not installed or not available at $sandboxExe"
}

Start-Process -FilePath $sandboxExe -ArgumentList "`"$wsbPath`"" -WindowStyle Hidden

Write-Host "Windows Sandbox launch requested."
Write-Host "Artifacts will appear in:"
Write-Host "  $repoRoot\target\windows-sandbox\legacy\$RunId\shared\artifacts"
