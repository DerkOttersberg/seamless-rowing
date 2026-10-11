param(
    [string]$Stage = 'D:\SeamlessRowingQA-20261011',
    [string]$Distro = 'SeamlessRowingQA-20261011'
)
$ErrorActionPreference = 'Stop'
$expected = 'f49494a27cbd9701b6b031844698dee224ebffd3f8e12240152147c3305dd855'
$results = @()
foreach ($entry in @(
    @{ Run = 'r19'; Report = 'rowing-contact.json' },
    @{ Run = 'r20'; Report = 'rowing-matrix.json' },
    @{ Run = 'r21'; Report = 'rowing-lifecycle.json' },
    @{ Run = 'r22'; Report = 'rowing-settings.json' },
    @{ Run = 'r23'; Report = 'rowing-contact.json' }
)) {
    $profile = Join-Path $Stage ('client-' + $entry.Run)
    $before = @{}
    Get-ChildItem -LiteralPath (Join-Path $profile 'mods') -Filter '*.jar' | ForEach-Object {
        $before[$_.Name] = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
    }
    if ($before['seamless-rowing-0.1.0-preview.1+mc26.3-fabric.jar'] -ne $expected) { throw 'Unexpected candidate JAR' }
    $before | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $profile 'jar-hashes-final.json') -Encoding utf8
    $log = Join-Path $Stage ($entry.Run + '-final-launch.log')
    & wsl --distribution $Distro --exec env ALSOFT_DRIVERS=null ISOLATED_CLIENT_TIMEOUT_SECONDS=700 bash /mnt/c/Users/derko/Desktop/minecraft/tools/Run-IsolatedMinecraftClient.sh python3 ('/mnt/d/SeamlessRowingQA-20261011/run_client_' + $entry.Run + '.py') *> $log
    if ($LASTEXITCODE -ne 0) { throw ('Client failed: ' + $entry.Run) }
    $report = Get-Content -LiteralPath (Join-Path $profile $entry.Report) -Raw | ConvertFrom-Json
    if (-not $report.pass) { throw ('Acceptance failed: ' + $entry.Run) }
    Get-ChildItem -LiteralPath (Join-Path $profile 'mods') -Filter '*.jar' | ForEach-Object {
        if ((Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant() -ne $before[$_.Name]) { throw 'JAR changed during test' }
    }
    $results += @{ run = $entry.Run; pass = $true; candidateSha256 = $expected; report = $entry.Report }
    Write-Output ('PASSED ' + $entry.Run)
}
& wsl --distribution $Distro --exec env ALSOFT_DRIVERS=null ISOLATED_CLIENT_TIMEOUT_SECONDS=900 bash /mnt/c/Users/derko/Desktop/minecraft/tools/Run-IsolatedMinecraftClient.sh python3 /mnt/c/Users/derko/Desktop/minecraft/seamless-rowing/qa/run_pair.py pair8 *> (Join-Path $Stage 'pair8-final-launch.log')
if ($LASTEXITCODE -ne 0) { throw 'Pair failed' }
$pair = Get-Content -LiteralPath (Join-Path $Stage 'pair8\pair-results.json') -Raw | ConvertFrom-Json
if (-not $pair.pass) { throw 'Pair acceptance failed' }
$results += @{ run = 'pair8'; pass = $true; candidateSha256 = $expected; report = 'pair-results.json' }
$results | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $Stage 'final-run-results.json') -Encoding utf8
Write-Output 'PASSED pair8; final packaged preview gates complete'
