param([string]$Distro = 'SeamlessRowingElbowQA-20261011')
$ErrorActionPreference = 'Stop'
$stage = 'D:\SeamlessRowingQA-20261011'
$repo = Split-Path -Parent $PSScriptRoot
$jarName = 'seamless-rowing-0.1.0-preview.2+mc26.3-fabric.jar'
$expected = 'd862f745ef14d700ea21d2d0c17193eb3613bf8217badfa19812e4df12019891'
if ((Get-FileHash -LiteralPath (Join-Path $repo ('fabric\build\libs\' + $jarName))).Hash.ToLowerInvariant() -ne $expected) { throw 'Unexpected candidate' }
foreach ($entry in @(
    @{Run='f1';Report='rowing-contact.json'},
    @{Run='f2';Report='rowing-matrix.json'},
    @{Run='f3';Report='rowing-lifecycle.json'}
)) {
    $profile = Join-Path $stage ('client-' + $entry.Run)
    $before = @{}
    Get-ChildItem -LiteralPath (Join-Path $profile 'mods') -Filter '*.jar' | ForEach-Object {
        $before[$_.Name] = (Get-FileHash -LiteralPath $_.FullName).Hash.ToLowerInvariant()
    }
    if ($before[$jarName] -ne $expected) { throw 'Incorrect profile candidate' }
    $before | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $profile 'jar-hashes-final.json') -Encoding utf8
    & wsl --distribution $Distro --exec env ALSOFT_DRIVERS=null ISOLATED_CLIENT_TIMEOUT_SECONDS=700 bash /mnt/c/Users/derko/Desktop/minecraft/tools/Run-IsolatedMinecraftClient.sh python3 ('/mnt/d/SeamlessRowingQA-20261011/run_client_' + $entry.Run + '.py') *> (Join-Path $stage ($entry.Run + '-launch.log'))
    if ($LASTEXITCODE -ne 0) { throw ('Client failed: ' + $entry.Run) }
    foreach ($name in @($entry.Report,'rowing-anatomy.json')) {
        $report = Get-Content -LiteralPath (Join-Path $profile $name) -Raw | ConvertFrom-Json
        if (-not $report.pass) { throw ('Failed ' + $entry.Run + '/' + $name) }
    }
    Get-ChildItem -LiteralPath (Join-Path $profile 'mods') -Filter '*.jar' | ForEach-Object {
        if ((Get-FileHash -LiteralPath $_.FullName).Hash.ToLowerInvariant() -ne $before[$_.Name]) { throw 'JAR changed during run' }
    }
    Write-Output ('PASSED ' + $entry.Run)
}
& py -3 (Join-Path $PSScriptRoot 'prepare_pair.py') pair11 f1
if ($LASTEXITCODE -ne 0) { throw 'Pair preparation failed' }
foreach ($role in @('rower','observer')) {
    Get-ChildItem -LiteralPath (Join-Path $stage 'integrations') -Filter '*.jar' | ForEach-Object {
        Copy-Item -LiteralPath $_.FullName -Destination (Join-Path $stage ('client-pair11' + $role + '\mods'))
    }
    if ((Get-FileHash -LiteralPath (Join-Path $stage ('client-pair11' + $role + '\mods\' + $jarName))).Hash.ToLowerInvariant() -ne $expected) { throw 'Incorrect pair candidate' }
}
& wsl --distribution $Distro --exec env ALSOFT_DRIVERS=null ISOLATED_CLIENT_TIMEOUT_SECONDS=900 bash /mnt/c/Users/derko/Desktop/minecraft/tools/Run-IsolatedMinecraftClient.sh python3 /mnt/c/Users/derko/Desktop/minecraft/seamless-rowing/qa/run_pair.py pair11 *> (Join-Path $stage 'pair11-launch.log')
if ($LASTEXITCODE -ne 0) { throw 'Pair failed' }
$pair = Get-Content -LiteralPath (Join-Path $stage 'pair11\pair-results.json') -Raw | ConvertFrom-Json
if (-not $pair.pass) { throw 'Failed paired contact/ownership gates' }
foreach ($role in @('rower','observer')) {
    $report = Get-Content -LiteralPath (Join-Path $stage ('client-pair11' + $role + '\rowing-anatomy.json')) -Raw | ConvertFrom-Json
    if (-not $report.pass) { throw ('Failed ' + $role + ' anatomy') }
}
Write-Output 'PASSED pair11; all new-candidate gates passed'
