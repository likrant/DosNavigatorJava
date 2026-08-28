param(
    [string]$FormsRunId = "legacy-all-safe-forms-v2-20260828",
    [string]$MenusRunId = "legacy-top-menus-20260828",
    [string]$OutputRoot = "tests\legacy\ui-reference"
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

$repoRoot = Split-Path -Parent $PSScriptRoot
$legacyRoot = Join-Path $repoRoot "target\windows-sandbox\legacy"
$formsRoot = Join-Path $legacyRoot "$FormsRunId\shared\artifacts"
$menusRoot = Join-Path $legacyRoot "$MenusRunId\shared\artifacts"
$output = Join-Path $repoRoot $OutputRoot
$screenshotsRoot = Join-Path $output "screenshots"
$formsOutput = Join-Path $screenshotsRoot "forms"
$menusOutput = Join-Path $screenshotsRoot "menus"
$resourcesOutput = Join-Path $output "resources"

$formFiles = @(
    "00-startup.png",
    "settings-system.png",
    "settings-startup.png",
    "settings-interface.png",
    "settings-confirmations.png",
    "settings-savers.png",
    "settings-printer.png",
    "settings-country.png",
    "settings-mouse.png",
    "settings-editor-viewer.png",
    "settings-file-manager-setup.png",
    "settings-file-manager-information.png",
    "settings-file-manager-groups.png",
    "settings-panel-view.png",
    "settings-panel-sort.png",
    "settings-panel-filter.png",
    "settings-colors.png",
    "utility-calculator.png",
    "utility-calendar.png"
)
$menuFiles = @(
    "menu-file.png",
    "menu-disk.png",
    "menu-utilities.png",
    "menu-panel.png",
    "menu-manager.png",
    "menu-settings.png",
    "menu-windows.png"
)

New-Item -ItemType Directory -Force -Path $formsOutput, $menusOutput, $resourcesOutput | Out-Null

function Copy-ReferenceScreens([string]$SourceRoot, [string[]]$Names, [string]$Destination) {
    foreach ($name in $Names) {
        $source = Join-Path $SourceRoot $name
        if (-not (Test-Path -LiteralPath $source)) { throw "Missing reference screenshot: $source" }
        Copy-Item -LiteralPath $source -Destination (Join-Path $Destination $name) -Force
    }
}

function Get-ScreenMetadata([string]$Path, [string]$Category) {
    $bitmap = [System.Drawing.Bitmap]::FromFile($Path)
    try {
        $colors = @{}
        for ($y = 0; $y -lt $bitmap.Height; $y += 4) {
            for ($x = 0; $x -lt $bitmap.Width; $x += 4) {
                $color = $bitmap.GetPixel($x, $y)
                $key = "#{0:X2}{1:X2}{2:X2}" -f $color.R, $color.G, $color.B
                if ($colors.ContainsKey($key)) { $colors[$key]++ } else { $colors[$key] = 1 }
            }
        }
        return [ordered]@{
            id = [IO.Path]::GetFileNameWithoutExtension($Path)
            category = $Category
            file = "$Category/" + [IO.Path]::GetFileName($Path)
            widthPx = $bitmap.Width
            heightPx = $bitmap.Height
            expectedTextGrid = @{ columns = 160; rows = 50; cellWidthPx = 8; cellHeightPx = 14 }
            logicalClient = @{ xPx = 8; yPx = 31; widthPx = 1280; heightPx = 700 }
            sampledPalette = @($colors.GetEnumerator() | Sort-Object Value -Descending | Select-Object -First 16 | ForEach-Object { [ordered]@{ rgb = $_.Key; samples = $_.Value } })
        }
    }
    finally {
        $bitmap.Dispose()
    }
}

Copy-ReferenceScreens $formsRoot $formFiles $formsOutput
Copy-ReferenceScreens $menusRoot $menuFiles $menusOutput

$resourcePath = Join-Path $repoRoot "DosNavigator-master\RESOURCE\RUSSIAN\dn.dnr"
$resourceText = [Text.Encoding]::GetEncoding(866).GetString([IO.File]::ReadAllBytes($resourcePath))
[IO.File]::WriteAllText((Join-Path $resourcesOutput "russian-ui-resources.dnr"), $resourceText, [Text.UTF8Encoding]::new($false))

$dialogHeaders = [regex]::Matches($resourceText, "(?m)^DIALOG\s+(?<id>\w+),\s*(?<x>\d+),\s*(?<y>\d+),\s*'(?<title>[^']*)'") |
    ForEach-Object { [ordered]@{ id = $_.Groups['id'].Value; x = [int]$_.Groups['x'].Value; y = [int]$_.Groups['y'].Value; title = $_.Groups['title'].Value } }
$menuHeaders = [regex]::Matches($resourceText, "(?im)^MENU\s+(?<id>\w+)") |
    ForEach-Object { $_.Groups['id'].Value }

$screenContracts = @(
    @{ screen = "00-startup"; command = "main-panel"; dialog = $null },
    @{ screen = "settings-system"; command = "cmSystemSetup"; dialog = "dlgSystemSetup" },
    @{ screen = "settings-startup"; command = "cmStartup"; dialog = "dlgStartupSetup" },
    @{ screen = "settings-interface"; command = "cmInterfaceSetup"; dialog = "dlgInterfaceSetup" },
    @{ screen = "settings-confirmations"; command = "cmSetupConfirmation"; dialog = "dlgConfirmations" },
    @{ screen = "settings-savers"; command = "cmSaversSetup"; dialog = $null },
    @{ screen = "settings-printer"; command = "cmSetupPrinter"; dialog = "dlgPrinterSetup" },
    @{ screen = "settings-country"; command = "cmCountrySetup"; dialog = "dlgCountrySetup" },
    @{ screen = "settings-mouse"; command = "cmSetupMouse"; dialog = "dlgMouseSetup" },
    @{ screen = "settings-editor-viewer"; command = "cmEditorDefaults"; dialog = "dlgEditorDefaults" },
    @{ screen = "settings-file-manager-setup"; command = "cmFMSetup"; dialog = "dlgFMSetup" },
    @{ screen = "settings-file-manager-information"; command = "cmDriveInfoSetup"; dialog = "dlgDriveInfoSetup" },
    @{ screen = "settings-file-manager-groups"; command = "cmHighlightGroups"; dialog = "dlgHighlightGroups" },
    @{ screen = "settings-panel-view"; command = "cmPanelShowSetup"; dialog = "dlgPanelShowSetup" },
    @{ screen = "settings-panel-sort"; command = "cmPanelSortSetup"; dialog = "dlgPanelSortSetup" },
    @{ screen = "settings-panel-filter"; command = "cmAdvFilter"; dialog = "dlgAdvancedFilter" },
    @{ screen = "settings-colors"; command = "cmChangeColors"; dialog = $null },
    @{ screen = "utility-calculator"; command = "cmCalculator"; dialog = "dlgCalculator" },
    @{ screen = "utility-calendar"; command = "cmCalendar"; dialog = $null }
)

$screens = @()
Get-ChildItem -LiteralPath $formsOutput -Filter '*.png' | Sort-Object Name | ForEach-Object { $screens += Get-ScreenMetadata $_.FullName "forms" }
Get-ChildItem -LiteralPath $menusOutput -Filter '*.png' | Sort-Object Name | ForEach-Object { $screens += Get-ScreenMetadata $_.FullName "menus" }

$manifest = [ordered]@{
    schemaVersion = 1
    language = "Russian"
    source = [ordered]@{
        formsRunId = $FormsRunId
        menusRunId = $MenusRunId
        resource = "resources/russian-ui-resources.dnr"
        exclusions = @("utility-ascii-table.png: invalid first capture; not included")
    }
    rendering = [ordered]@{
        textGrid = @{ columns = 160; rows = 50; cellWidthPx = 8; cellHeightPx = 14 }
        corePalette = @{ black = "#000000"; navy = "#000080"; teal = "#008080"; gray = "#C0C0C0"; darkGray = "#808080"; cyan = "#00FFFF"; yellow = "#FFFF00"; green = "#00FF00" }
        notes = @("Compare DN client content, not Windows title bar or desktop chrome.", "Pseudographics, labels, positions, and hotkeys are defined by the decoded DNR resource.")
    }
    screens = $screens
    screenContracts = $screenContracts
    dialogs = @($dialogHeaders)
    menus = @($menuHeaders)
}
$manifest | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath (Join-Path $output "screen-index.json") -Encoding utf8

$readme = @'
# Legacy DN UI Reference

This bundle is the Java UI-test baseline for the Russian legacy DN fixture.

- `screenshots/forms`: validated form captures.
- `screenshots/menus`: top-level menu captures.
- `screen-index.json`: image dimensions, sampled RGB palette, expected 160x50 text grid, and the complete menu/dialog index.
- `resources/russian-ui-resources.dnr`: UTF-8 decoded authoritative resource source. `MENU` and `DIALOG` entries provide labels, hotkeys, pseudographics, and text-cell coordinates; `~x~` marks a mnemonic.

The `logicalClient` rectangle in each screen excludes Windows chrome. `screenContracts` links each captured form to its DN command and, where statically declared, to the exact `DIALOG` id and text-cell origin. The renderer uses the DOS frame primitives from the resource/UI implementation; compare the resulting glyphs as cells, not anti-aliased pixels.

Do not OCR the screenshots during Java testing. Assert the Java text grid, component bounds, colors, and resource-derived labels against `screen-index.json` and `russian-ui-resources.dnr`; use PNGs only for pixel-level visual regression.
'@
[IO.File]::WriteAllText((Join-Path $output "README.md"), $readme, [Text.UTF8Encoding]::new($false))

Write-Host "Built legacy UI reference bundle: $output"
