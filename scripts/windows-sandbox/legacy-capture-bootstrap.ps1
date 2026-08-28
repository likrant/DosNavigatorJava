param(
    [Parameter(Mandatory = $true)] [string]$RuntimeRoot,
    [Parameter(Mandatory = $true)] [string]$LauncherPath,
    [Parameter(Mandatory = $true)] [string]$ArtifactsRoot,
    [int]$ScreenWidth = 1280,
    [int]$ScreenHeight = 720,
    [ValidateSet("Forms", "Menus")] [string]$CaptureMode = "Forms"
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

$signature = @"
using System;
using System.Runtime.InteropServices;
public static class NativeWin {
    [StructLayout(LayoutKind.Sequential)] public struct RECT { public int Left; public int Top; public int Right; public int Bottom; }
    [DllImport("user32.dll")] public static extern bool GetWindowRect(IntPtr hWnd, out RECT rect);
    [DllImport("user32.dll")] public static extern bool MoveWindow(IntPtr hWnd, int x, int y, int width, int height, bool repaint);
    [DllImport("user32.dll")] public static extern bool SetForegroundWindow(IntPtr hWnd);
    [DllImport("user32.dll")] public static extern bool ShowWindow(IntPtr hWnd, int command);
    [DllImport("user32.dll")] public static extern bool SetCursorPos(int x, int y);
    [DllImport("user32.dll")] public static extern void mouse_event(uint flags, uint dx, uint dy, uint data, UIntPtr extraInfo);
}
"@
Add-Type -TypeDefinition $signature

New-Item -ItemType Directory -Force -Path $ArtifactsRoot | Out-Null
$logPath = Join-Path $ArtifactsRoot "bootstrap.log"
Set-Content -LiteralPath $logPath -Value "Legacy sandbox bootstrap started."
$localeMarkerPath = Join-Path $ArtifactsRoot "system-locale-ru-RU.txt"

if (-not (Test-Path -LiteralPath $localeMarkerPath)) {
    Set-WinSystemLocale -SystemLocale "ru-RU"
    Set-Content -LiteralPath $localeMarkerPath -Value "ru-RU system locale configured; Sandbox reboot requested."
    Restart-Computer -Force
    exit
}

$shell = New-Object -ComObject WScript.Shell
$steps = @()

function Write-Log([string]$Message) {
    Add-Content -LiteralPath $logPath -Value ("[{0}] {1}" -f (Get-Date -Format "yyyy-MM-dd HH:mm:ss"), $Message)
}

function Save-Screenshot([string]$Path) {
    $bounds = New-Object System.Drawing.Rectangle 0, 0, $ScreenWidth, $ScreenHeight
    $rect = New-Object NativeWin+RECT
    if ($script:process -and $script:process.MainWindowHandle -ne 0 -and [NativeWin]::GetWindowRect($script:process.MainWindowHandle, [ref]$rect)) {
        $bounds = [System.Drawing.Rectangle]::FromLTRB($rect.Left, $rect.Top, $rect.Right, $rect.Bottom)
        $bounds = [System.Drawing.Rectangle]::Intersect($bounds, [System.Windows.Forms.Screen]::PrimaryScreen.Bounds)
    }
    $bitmap = New-Object System.Drawing.Bitmap $bounds.Width, $bounds.Height
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    try {
        $graphics.CopyFromScreen($bounds.Location, [System.Drawing.Point]::Empty, $bounds.Size)
        $bitmap.Save($Path, [System.Drawing.Imaging.ImageFormat]::Png)
    }
    finally {
        $graphics.Dispose()
        $bitmap.Dispose()
    }
}

function Capture-Step([string]$Id, [string]$Behavior) {
    Start-Sleep -Milliseconds 600
    $pngPath = Join-Path $ArtifactsRoot ($Id + ".png")
    Save-Screenshot $pngPath
    $script:steps += [pscustomobject]@{ Id = $Id; Behavior = $Behavior; Png = [System.IO.Path]::GetFileName($pngPath) }
}

function Send-Key([string]$Keys, [string]$Id, [string]$Behavior) {
    [void]$shell.AppActivate($script:process.Id)
    Start-Sleep -Milliseconds 250
    $shell.SendKeys($Keys)
    Capture-Step $Id $Behavior
}

function Send-Mnemonic([string]$Key) {
    $shell.SendKeys($Key)
    Start-Sleep -Milliseconds 250
}

function Open-Route([string[]]$Route) {
    [void]$shell.AppActivate($script:process.Id)
    $shell.SendKeys("{F10}")
    Start-Sleep -Milliseconds 250
    foreach ($key in $Route) { Send-Mnemonic $key }
}

function Click-Relative([int]$X, [int]$Y) {
    $rect = New-Object NativeWin+RECT
    if (-not [NativeWin]::GetWindowRect($script:process.MainWindowHandle, [ref]$rect)) {
        throw "Could not resolve DN window position for menu click."
    }
    [NativeWin]::SetCursorPos($rect.Left + $X, $rect.Top + $Y)
    Start-Sleep -Milliseconds 100
    [NativeWin]::mouse_event(0x0002, 0, 0, 0, [UIntPtr]::Zero)
    [NativeWin]::mouse_event(0x0004, 0, 0, 0, [UIntPtr]::Zero)
    Start-Sleep -Milliseconds 300
}

function Close-ToMainPanel {
    [void]$shell.AppActivate($script:process.Id)
    1..3 | ForEach-Object { $shell.SendKeys("{ESC}"); Start-Sleep -Milliseconds 150 }
}

function Capture-OpenedForm([string]$Id, [string]$Behavior) {
    Capture-Step $Id $Behavior
    # Escape is sent first, but each capture gets a fresh DN process so a
    # modal dialog can never leak focus into the next form route.
    $shell.SendKeys("{ESC}")
    Start-Sleep -Milliseconds 250
    Stop-Dn
}

function Start-Dn {
    $priorDnProcessIds = @(Get-Process -Name dn -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Id)
    Start-Process -FilePath $LauncherPath
    $dnProcess = $null
    for ($attempt = 0; $attempt -lt 20; $attempt++) {
        Start-Sleep -Milliseconds 500
        $dnProcess = Get-Process -Name dn -ErrorAction SilentlyContinue | Where-Object { $_.Id -notin $priorDnProcessIds } | Sort-Object StartTime -Descending | Select-Object -First 1
        if ($dnProcess -and $dnProcess.MainWindowHandle -ne 0) { break }
    }
    if (-not $dnProcess) { throw "The shortcut did not start dn.exe." }

    $script:process = $dnProcess
    Write-Log "Started dn.exe pid=$($process.Id) through shortcut."
    Start-Sleep -Seconds 3
    $rect = New-Object NativeWin+RECT
    if (-not [NativeWin]::GetWindowRect($dnProcess.MainWindowHandle, [ref]$rect)) {
        throw "DN process has no main window handle."
    }
    $workArea = [System.Windows.Forms.Screen]::PrimaryScreen.WorkingArea
    $windowWidth = $rect.Right - $rect.Left
    $windowHeight = $rect.Bottom - $rect.Top
    $targetY = $workArea.Bottom - $windowHeight
    [void][NativeWin]::MoveWindow($dnProcess.MainWindowHandle, 0, $targetY, $windowWidth, $windowHeight, $true)
    [void][NativeWin]::SetForegroundWindow($dnProcess.MainWindowHandle)
    [void]$shell.AppActivate($process.Id)
}

function Stop-Dn {
    if ($script:process -and -not $script:process.HasExited) {
        Stop-Process -Id $script:process.Id -Force
        Start-Sleep -Milliseconds 500
    }
    $script:process = $null
}

function Get-ActiveLanguage {
    $iniPath = Join-Path $RuntimeRoot "dn.ini"
    if (-not (Test-Path -LiteralPath $iniPath)) { throw "Missing DN configuration: $iniPath" }

    $languageLine = Get-Content -LiteralPath $iniPath | Where-Object { $_ -match '^ActiveLanguage=' } | Select-Object -First 1
    if (-not $languageLine) { return "English" }
    return ($languageLine -replace '^ActiveLanguage=', '').Trim()
}

function Get-InterfaceSettingsMnemonics([string]$Language) {
    switch -Regex ($Language) {
        '^Russian$' { return @("Y", "R", "B") }
        '^English$' { return @("O", "C", "I") }
        default { throw "No verified Interface Settings mnemonic route for ActiveLanguage=$Language." }
    }
}

function Open-InterfaceSettings {
    $language = Get-ActiveLanguage
    $route = Get-InterfaceSettingsMnemonics $language
    Start-Dn
    Open-Route $route
    Capture-OpenedForm "settings-interface" "Opened Interface Settings through ActiveLanguage=$language and F10 -> $($route -join ' -> ') mnemonics."
}

function Capture-RussianConfigurationForms {
    # These coordinates select visible Configuration items after F10 -> Y -> R.
    # Clicks avoid ambiguous duplicate mnemonics without using arrow keys.
    $forms = @(
        @{ Id = "settings-system"; Y = 85; Name = "System settings" },
        @{ Id = "settings-startup"; Y = 97; Name = "Startup/load-unload settings" },
        @{ Id = "settings-interface"; Y = 109; Name = "Interface settings" },
        @{ Id = "settings-confirmations"; Y = 121; Name = "Confirmation settings" },
        @{ Id = "settings-savers"; Y = 145; Name = "Screen saver settings" },
        @{ Id = "settings-printer"; Y = 157; Name = "Printer settings" },
        @{ Id = "settings-country"; Y = 169; Name = "Country settings" },
        @{ Id = "settings-mouse"; Y = 181; Name = "Mouse settings" },
        @{ Id = "settings-editor-viewer"; Y = 205; Name = "Editor/viewer settings" }
    )
    foreach ($form in $forms) {
        Start-Dn
        Open-Route @("Y", "R")
        Click-Relative 480 $form.Y
        Capture-OpenedForm $form.Id ("Opened Russian Configuration form: " + $form.Name + ".")
    }
}

function Capture-RussianFileManagerForms {
    $forms = @(
        @{ Id = "settings-file-manager-setup"; Route = @("Y", "V", "E"); Name = "File manager setup" },
        @{ Id = "settings-file-manager-information"; Route = @("Y", "V", "B"); Name = "Information panel setup" },
        @{ Id = "settings-file-manager-groups"; Route = @("Y", "V", "U"); Name = "File groups setup" }
    )
    foreach ($form in $forms) {
        Start-Dn
        Open-Route $form.Route
        Capture-OpenedForm $form.Id ("Opened Russian File Manager form: " + $form.Name + ".")
    }
}

function Capture-RussianPanelForms {
    $forms = @(
        @{ Id = "settings-panel-view"; Route = @("Y", "G", "Y"); Name = "Panel view setup" },
        @{ Id = "settings-panel-sort"; Route = @("Y", "G", "E"); Name = "Panel sort setup" },
        @{ Id = "settings-panel-filter"; Route = @("Y", "G", "A"); Name = "Panel filter setup" }
    )
    foreach ($form in $forms) {
        Start-Dn
        Open-Route $form.Route
        Capture-OpenedForm $form.Id ("Opened Russian Panel form: " + $form.Name + ".")
    }
}

function Capture-RussianUtilityForms {
    $forms = @(
        @{ Id = "utility-calculator"; Key = "^{F6}"; Name = "Calculator" },
        @{ Id = "utility-calendar"; Key = "^{F12}"; Name = "Calendar" },
        @{ Id = "utility-ascii-table"; Route = @("E", ","); Name = "ASCII table" }
    )
    foreach ($form in $forms) {
        Start-Dn
        if ($form.Route) {
            Open-Route $form.Route
        }
        else {
            [void]$shell.AppActivate($script:process.Id)
            $shell.SendKeys($form.Key)
        }
        Capture-OpenedForm $form.Id ("Opened Russian Utilities form by visible shortcut: " + $form.Name + ".")
    }
}

function Capture-RussianSettingsForms {
    Capture-RussianConfigurationForms
    Capture-RussianFileManagerForms
    Capture-RussianPanelForms
    Start-Dn
    Open-Route @("Y", "W")
    Capture-OpenedForm "settings-colors" "Opened Russian Colors settings through F10 -> Y -> W."
    Capture-RussianUtilityForms
}

function Capture-RussianTopMenus {
    $menus = @(
        @{ Id = "menu-file"; Route = @("A"); Name = "File" },
        @{ Id = "menu-disk"; Route = @("L"); Name = "Disk" },
        @{ Id = "menu-utilities"; Route = @("E"); Name = "Utilities" },
        @{ Id = "menu-panel"; Route = @("G"); Name = "Panel" },
        @{ Id = "menu-manager"; Route = @("V"); Name = "Manager" },
        @{ Id = "menu-settings"; Route = @("Y"); Name = "Settings" },
        @{ Id = "menu-windows"; Route = @("J"); Name = "Windows" }
    )
    foreach ($menu in $menus) {
        Start-Dn
        Open-Route $menu.Route
        Capture-Step $menu.Id ("Opened Russian top-level menu: " + $menu.Name + ".")
        Stop-Dn
    }
}

function Write-Report {
    $reportPath = Join-Path $ArtifactsRoot "report.md"
    $lines = @("# Legacy DN sandbox capture", "", "Runtime root: $RuntimeRoot", "", "## Steps", "")
    foreach ($step in $steps) { $lines += "- $($step.Id): $($step.Behavior) ($($step.Png))" }
    [System.IO.File]::WriteAllLines($reportPath, $lines, [System.Text.UTF8Encoding]::new($false))
}

try {
    Write-Log "RuntimeRoot=$RuntimeRoot"
    Write-Log "LauncherPath=$LauncherPath"
    Start-Dn
    Capture-Step "00-startup" "Initial legacy DN screen."
    Stop-Dn
    $activeLanguage = Get-ActiveLanguage
    if ($activeLanguage -eq "Russian" -and $CaptureMode -eq "Forms") {
        Capture-RussianSettingsForms
    }
    elseif ($activeLanguage -eq "Russian" -and $CaptureMode -eq "Menus") {
        Capture-RussianTopMenus
    }
    else {
        Open-InterfaceSettings
        Write-Log "Only the language-specific Interface Settings route is captured for ActiveLanguage=$activeLanguage."
    }
    Write-Report
    Write-Log "Bootstrap completed. CaptureMode=$CaptureMode"
}
catch {
    Write-Log ("ERROR: " + $_.Exception.Message)
    throw
}
finally {
    Stop-Dn
}
