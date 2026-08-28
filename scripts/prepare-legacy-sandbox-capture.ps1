param(
    [string]$RunId = (Get-Date -Format "yyyyMMdd-HHmmss"),
    [string]$ScreenWidth = "1280",
    [string]$ScreenHeight = "720",
    [ValidateSet("Forms", "Menus")] [string]$CaptureMode = "Forms"
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$targetRoot = Join-Path $repoRoot "target\windows-sandbox\legacy\$RunId"
$sharedRoot = Join-Path $targetRoot "shared"
$runtimeRoot = Join-Path $sharedRoot "runtime"
$artifactsRoot = Join-Path $sharedRoot "artifacts"
$scriptsRoot = Join-Path $sharedRoot "sandbox-scripts"
$referenceRoot = Join-Path $repoRoot "tests\legacy\DNW-reference"
$shortcutPath = Join-Path ([Environment]::GetFolderPath("Desktop")) "Dos Navigator.lnk"
$wsbPath = Join-Path $targetRoot "legacy-capture.wsb"

New-Item -ItemType Directory -Force -Path $targetRoot, $sharedRoot, $runtimeRoot, $artifactsRoot, $scriptsRoot | Out-Null
Copy-Item -Path (Join-Path $referenceRoot "*") -Destination $runtimeRoot -Recurse -Force
Copy-Item -LiteralPath (Join-Path $repoRoot "scripts\windows-sandbox\legacy-capture-bootstrap.ps1") -Destination $scriptsRoot -Force
Copy-Item -LiteralPath (Join-Path $repoRoot "scripts\windows-sandbox\legacy-capture-bootstrap.cmd") -Destination $scriptsRoot -Force
Copy-Item -LiteralPath $shortcutPath -Destination $scriptsRoot -Force
Set-Content -LiteralPath (Join-Path $scriptsRoot "capture-mode.txt") -Value $CaptureMode -NoNewline

$config = @"
<Configuration>
  <VGpu>Enable</VGpu>
  <Networking>Disable</Networking>
  <AudioInput>Disable</AudioInput>
  <VideoInput>Disable</VideoInput>
  <ProtectedClient>Disable</ProtectedClient>
  <ClipboardRedirection>Disable</ClipboardRedirection>
  <PrinterRedirection>Disable</PrinterRedirection>
  <MemoryInMB>2048</MemoryInMB>
  <MappedFolders>
    <MappedFolder>
      <HostFolder>$sharedRoot</HostFolder>
      <SandboxFolder>C:\Harness</SandboxFolder>
      <ReadOnly>false</ReadOnly>
    </MappedFolder>
    <MappedFolder>
      <HostFolder>$runtimeRoot</HostFolder>
      <SandboxFolder>C:\DNW</SandboxFolder>
      <ReadOnly>false</ReadOnly>
    </MappedFolder>
  </MappedFolders>
  <LogonCommand>
    <Command>cmd.exe /d /c "C:\Harness\sandbox-scripts\legacy-capture-bootstrap.cmd"</Command>
  </LogonCommand>
</Configuration>
"@

[System.IO.File]::WriteAllText($wsbPath, $config, [System.Text.UTF8Encoding]::new($false))

Write-Host "Prepared Windows Sandbox capture package:"
Write-Host "  RunId: $RunId"
Write-Host "  Shared root: $sharedRoot"
Write-Host "  Runtime root: $runtimeRoot"
Write-Host "  Artifacts root: $artifactsRoot"
Write-Host "  WSB config: $wsbPath"
Write-Host "  Capture mode: $CaptureMode"
Write-Host ""
Write-Host "To launch manually on a machine with Windows Sandbox installed:"
Write-Host "  WindowsSandbox.exe `"$wsbPath`""
