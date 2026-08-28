param(
    [string]$OutputPath = "docs/porting/command-key-catalog.tsv"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$commandsPath = Join-Path $repoRoot "DosNavigator-master/commands.pas"
$output = Join-Path $repoRoot $OutputPath

New-Item -ItemType Directory -Force -Path (Split-Path -Parent $output) | Out-Null

function Add-Row([System.Collections.Generic.List[string]]$rows, [string[]]$values) {
    $rows.Add(($values | ForEach-Object { $_ -replace "`t", " " }) -join "`t")
}

$rows = [System.Collections.Generic.List[string]]::new()
Add-Row $rows @("kind", "pascalName", "pascalValue", "language", "mnemonic", "context", "status", "javaAnalog", "note")
$javaMappings = @{
    cmQuit = "org.dosnavigator.command.CommandId.QUIT"
    cmMenu = "org.dosnavigator.command.CommandId.MENU"
    cmClose = "org.dosnavigator.command.CommandId.CLOSE"
    cmNext = "org.dosnavigator.command.CommandId.NEXT"
    cmPrev = "org.dosnavigator.command.CommandId.PREVIOUS"
    cmOK = "org.dosnavigator.command.CommandId.OK"
    cmCancel = "org.dosnavigator.command.CommandId.CANCEL"
    cmMenuOn = "org.dosnavigator.command.CommandId.MENU_ON"
    cmMenuOff = "org.dosnavigator.command.CommandId.MENU_OFF"
    cmSystemSetup = "org.dosnavigator.command.CommandId.SYSTEM_SETUP"
    cmInterfaceSetup = "org.dosnavigator.command.CommandId.INTERFACE_SETUP"
    cmAbout = "org.dosnavigator.command.CommandId.ABOUT"
    cmRefresh = "org.dosnavigator.command.CommandId.REFRESH"
    kbF10 = "org.dosnavigator.terminal.KeyType.F10"
    kbTab = "org.dosnavigator.terminal.KeyType.Tab"
}

$source = Get-Content -LiteralPath $commandsPath
foreach ($line in $source) {
    if ($line -match '^\s*(?<name>cm[A-Za-z0-9_]+)\s*=\s*(?<value>[^;]+);') {
        $analog = $javaMappings[$Matches.name]
        $status = if ($analog) { "adapter" } else { "prototype to replace" }
        if (-not $analog) { $analog = "none; direct port pending" }
        Add-Row $rows @("command", $Matches.name, $Matches.value.Trim(), "", "", "Commands.pas", $status, $analog, "Pascal command catalogued.")
    }
    elseif ($line -match '^\s*(?<name>kb[A-Za-z0-9_]+)\s*=\s*(?<value>[^;]+);') {
        $analog = $javaMappings[$Matches.name]
        $status = if ($analog) { "adapter" } else { "prototype to replace" }
        if (-not $analog) { $analog = "none; terminal mapping pending" }
        Add-Row $rows @("key", $Matches.name, $Matches.value.Trim(), "", "", "Commands.pas", $status, $analog, "Pascal key catalogued.")
    }
}

foreach ($language in @("ENGLISH", "RUSSIAN", "UKRAIN")) {
    $resourcePath = Join-Path $repoRoot "DosNavigator-master/RESOURCE/$language/dn.dnr"
    $resourceText = [Text.Encoding]::GetEncoding(866).GetString([IO.File]::ReadAllBytes($resourcePath))
    $context = "resource:$language"
    $lineNumber = 0
    foreach ($line in $resourceText -split "`r?`n") {
        $lineNumber++
        if ($line -match '^\s*(MENU|DIALOG)\s+(?<id>\w+)') { $context = "${language}:$($Matches[1]):$($Matches.id)" }
        foreach ($match in [regex]::Matches($line, '~(?<mnemonic>.)~')) {
            Add-Row $rows @("mnemonic", "", "", $language, $match.Groups['mnemonic'].Value, "${context}:$lineNumber", "prototype to replace", "none; resource binding pending", "Language-specific resource mnemonic.")
        }
    }
}

[IO.File]::WriteAllLines($output, $rows, [Text.UTF8Encoding]::new($false))
Write-Host "Generated $output with $($rows.Count - 1) entries."
