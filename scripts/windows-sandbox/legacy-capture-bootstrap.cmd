@echo off
setlocal

set HARNESS_ROOT=C:\Harness
set ARTIFACTS_ROOT=%HARNESS_ROOT%\artifacts
set CAPTURE_MODE=Forms
if not exist "%ARTIFACTS_ROOT%" mkdir "%ARTIFACTS_ROOT%"
if exist "%HARNESS_ROOT%\sandbox-scripts\capture-mode.txt" set /p CAPTURE_MODE=<"%HARNESS_ROOT%\sandbox-scripts\capture-mode.txt"

echo Legacy sandbox cmd bootstrap started.>"%ARTIFACTS_ROOT%\bootstrap-cmd.log"
echo Launching PowerShell bootstrap...>>"%ARTIFACTS_ROOT%\bootstrap-cmd.log"

powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%HARNESS_ROOT%\sandbox-scripts\legacy-capture-bootstrap.ps1" -RuntimeRoot "C:\DNW" -LauncherPath "%HARNESS_ROOT%\sandbox-scripts\Dos Navigator.lnk" -ArtifactsRoot "%HARNESS_ROOT%\artifacts" -ScreenWidth 1280 -ScreenHeight 720 -CaptureMode "%CAPTURE_MODE%" >>"%ARTIFACTS_ROOT%\bootstrap-cmd.log" 2>&1

echo PowerShell bootstrap finished with exit code %ERRORLEVEL%.>>"%ARTIFACTS_ROOT%\bootstrap-cmd.log"
endlocal
