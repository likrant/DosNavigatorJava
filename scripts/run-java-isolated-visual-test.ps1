param(
    [switch]$Quiet
)

$arguments = @(
    "-q"
    "test-compile"
)

if (-not $Quiet) {
    Write-Host "Running isolated Java DN visual harness..."
}

mvn @arguments
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

java -cp "target\classes;target\test-classes" org.dosnavigator.testing.IsolatedVisualHarnessMain
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
