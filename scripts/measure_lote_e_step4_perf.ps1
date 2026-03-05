param(
    [string]$Package = "com.gurps.ficha.visual",
    [int]$OpenIterations = 12,
    [int]$ScrollIterations = 12,
    [int]$PollMs = 200,
    [int]$PollAttempts = 35,
    [string]$ReportPath = "app/build/reports/nexus_arcano_lote_e_step4_perf_emulador.txt"
)

$ErrorActionPreference = "Stop"

function Resolve-AdbPath {
    $adbCommand = Get-Command adb -ErrorAction SilentlyContinue
    if ($adbCommand) {
        return $adbCommand.Source
    }

    $repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
    $localPropertiesPath = Join-Path $repoRoot "local.properties"
    if (-not (Test-Path $localPropertiesPath)) {
        throw "adb not found in PATH and local.properties not found."
    }

    $sdkLine = Get-Content $localPropertiesPath | Where-Object { $_ -match "^sdk\.dir=" } | Select-Object -First 1
    if (-not $sdkLine) {
        throw "adb not found in PATH and sdk.dir is missing in local.properties."
    }

    $sdkDir = $sdkLine.Substring("sdk.dir=".Length).Trim()
    $sdkDir = $sdkDir -replace "\\:", ":"
    $sdkDir = $sdkDir -replace "\\\\", "\"
    $adbFromSdk = Join-Path $sdkDir "platform-tools\adb.exe"
    if (-not (Test-Path $adbFromSdk)) {
        throw "adb not found in PATH and not found at '$adbFromSdk'."
    }
    return $adbFromSdk
}

$script:AdbPath = Resolve-AdbPath

function Invoke-AdbCapture {
    param([string[]]$CommandArgs)
    $output = & $script:AdbPath @CommandArgs
    $exitCode = $LASTEXITCODE
    $text = ($output | Out-String).Trim()
    if ($exitCode -ne 0) {
        throw "adb command failed: $($CommandArgs -join ' ')`n$text"
    }
    return $text
}

function Invoke-AdbNoCapture {
    param([string[]]$CommandArgs)
    & $script:AdbPath @CommandArgs | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "adb command failed: $($CommandArgs -join ' ')"
    }
}

function Get-UiXml {
    $remoteDumpPath = "/sdcard/window_dump_perf_step4.xml"
    Invoke-AdbCapture @("shell", "uiautomator", "dump", $remoteDumpPath) | Out-Null
    $rawXml = Invoke-AdbCapture @("shell", "cat", $remoteDumpPath)
    return [xml]$rawXml
}

function Parse-Bounds {
    param([string]$Bounds)
    if ($Bounds -match "\[(\d+),(\d+)\]\[(\d+),(\d+)\]") {
        $x1 = [int]$matches[1]
        $y1 = [int]$matches[2]
        $x2 = [int]$matches[3]
        $y2 = [int]$matches[4]
        return [pscustomobject]@{
            X1 = $x1
            Y1 = $y1
            X2 = $x2
            Y2 = $y2
            CenterX = [int](($x1 + $x2) / 2)
            CenterY = [int](($y1 + $y2) / 2)
            Width = $x2 - $x1
            Height = $y2 - $y1
            Area = ($x2 - $x1) * ($y2 - $y1)
        }
    }
    return $null
}

function Find-NodeCenter {
    param(
        [xml]$Xml,
        [string]$Needle
    )
    $needleLower = $Needle.ToLowerInvariant()
    $nodes = $Xml.SelectNodes("//node")
    foreach ($node in $nodes) {
        $text = [string]$node.text
        $desc = [string]$node.'content-desc'
        $combined = ("$text $desc").ToLowerInvariant()
        if ($combined -like "*$needleLower*") {
            $bounds = Parse-Bounds ([string]$node.bounds)
            if ($bounds) {
                return $bounds
            }
        }
    }
    return $null
}

function Wait-NodeCenter {
    param(
        [string]$Needle,
        [bool]$ShouldExist = $true
    )
    for ($attempt = 1; $attempt -le $PollAttempts; $attempt++) {
        $xml = Get-UiXml
        $bounds = Find-NodeCenter -Xml $xml -Needle $Needle
        if ($ShouldExist -and $bounds) {
            return $bounds
        }
        if (-not $ShouldExist -and -not $bounds) {
            return $null
        }
        Start-Sleep -Milliseconds $PollMs
    }

    if ($ShouldExist) {
        throw "Node not found: $Needle"
    }
    throw "Node still visible after timeout: $Needle"
}

function Tap-Label {
    param([string]$Needle)
    $bounds = Wait-NodeCenter -Needle $Needle -ShouldExist $true
    Invoke-AdbNoCapture @("shell", "input", "tap", "$($bounds.CenterX)", "$($bounds.CenterY)")
}

function Find-LargestScrollableBounds {
    param([xml]$Xml)
    $nodes = $Xml.SelectNodes("//node[@scrollable='true']")
    $best = $null
    foreach ($node in $nodes) {
        $candidate = Parse-Bounds ([string]$node.bounds)
        if (-not $candidate) {
            continue
        }
        if (-not $best -or $candidate.Area -gt $best.Area) {
            $best = $candidate
        }
    }
    return $best
}

function Get-GfxP95Ms {
    $gfx = Invoke-AdbCapture @("shell", "dumpsys", "gfxinfo", $Package)
    $match = [regex]::Match($gfx, "95th percentile:\s*([0-9]+(?:\.[0-9]+)?)ms")
    if (-not $match.Success) {
        return $null
    }
    return [double]$match.Groups[1].Value
}

function Get-Percentile {
    param(
        [double[]]$Values,
        [double]$Percent
    )
    if (-not $Values -or $Values.Count -eq 0) {
        return $null
    }
    $sorted = $Values | Sort-Object
    $index = [math]::Floor(($sorted.Count - 1) * ($Percent / 100.0))
    return [math]::Round([double]$sorted[$index], 3)
}

function Get-Median {
    param([double[]]$Values)
    return Get-Percentile -Values $Values -Percent 50
}

function Get-Max {
    param([double[]]$Values)
    if (-not $Values -or $Values.Count -eq 0) {
        return $null
    }
    return [math]::Round(($Values | Measure-Object -Maximum).Maximum, 3)
}

function Get-SamplesText {
    param([double[]]$Values)
    if (-not $Values -or $Values.Count -eq 0) {
        return "[]"
    }
    return "[" + (($Values | ForEach-Object { [math]::Round($_, 3).ToString("0.###") }) -join ", ") + "]"
}

function Require-DeviceReady {
    $state = Invoke-AdbCapture @("get-state")
    if ($state -notmatch "device") {
        throw "No Android device/emulator connected."
    }
    $serial = Invoke-AdbCapture @("get-serialno")
    return $serial.Trim()
}

function Launch-App {
    Invoke-AdbNoCapture @("shell", "am", "force-stop", $Package)
    Start-Sleep -Milliseconds 300
    Invoke-AdbCapture @("shell", "monkey", "-p", $Package, "-c", "android.intent.category.LAUNCHER", "1") | Out-Null
    Start-Sleep -Seconds 3
}

function Prepare-MagiaTab {
    Tap-Label -Needle "Magia"
    Start-Sleep -Milliseconds 500
    Wait-NodeCenter -Needle "Adicionar Magia" -ShouldExist $true | Out-Null
}

function Measure-OpenP95 {
    $samples = New-Object System.Collections.Generic.List[double]
    for ($i = 1; $i -le $OpenIterations; $i++) {
        Invoke-AdbCapture @("shell", "dumpsys", "gfxinfo", $Package, "reset") | Out-Null
        Tap-Label -Needle "Adicionar Magia"
        Wait-NodeCenter -Needle "Selecionar Magia" -ShouldExist $true | Out-Null
        Start-Sleep -Milliseconds 350
        $sample = Get-GfxP95Ms
        if ($null -ne $sample) {
            $samples.Add($sample)
        }
        Tap-Label -Needle "Fechar"
        Wait-NodeCenter -Needle "Selecionar Magia" -ShouldExist $false | Out-Null
        Start-Sleep -Milliseconds 250
    }
    return $samples.ToArray()
}

function Measure-ScrollP95 {
    $samples = New-Object System.Collections.Generic.List[double]
    Tap-Label -Needle "Adicionar Magia"
    Wait-NodeCenter -Needle "Selecionar Magia" -ShouldExist $true | Out-Null
    Start-Sleep -Milliseconds 300

    for ($i = 1; $i -le $ScrollIterations; $i++) {
        $xml = Get-UiXml
        $scrollBounds = Find-LargestScrollableBounds -Xml $xml
        if (-not $scrollBounds) {
            throw "Could not find a scrollable node in selector dialog."
        }
        $x = $scrollBounds.CenterX
        $startY = [int]($scrollBounds.Y2 - ($scrollBounds.Height * 0.22))
        $endY = [int]($scrollBounds.Y1 + ($scrollBounds.Height * 0.22))
        if ($startY -le $endY) {
            $startY = $scrollBounds.Y2 - 40
            $endY = $scrollBounds.Y1 + 40
        }

        Invoke-AdbCapture @("shell", "dumpsys", "gfxinfo", $Package, "reset") | Out-Null
        Invoke-AdbNoCapture @("shell", "input", "swipe", "$x", "$startY", "$x", "$endY", "120")
        Start-Sleep -Milliseconds 450
        $sample = Get-GfxP95Ms
        if ($null -ne $sample) {
            $samples.Add($sample)
        }
        Start-Sleep -Milliseconds 180
    }

    Tap-Label -Needle "Fechar"
    Wait-NodeCenter -Needle "Selecionar Magia" -ShouldExist $false | Out-Null
    return $samples.ToArray()
}

$serial = Require-DeviceReady
Launch-App
Prepare-MagiaTab

$openSamples = Measure-OpenP95
$scrollSamples = Measure-ScrollP95

$openP95 = Get-Percentile -Values $openSamples -Percent 95
$openMedian = Get-Median -Values $openSamples
$openMax = Get-Max -Values $openSamples

$scrollP95 = Get-Percentile -Values $scrollSamples -Percent 95
$scrollMedian = Get-Median -Values $scrollSamples
$scrollMax = Get-Max -Values $scrollSamples

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$fullReportPath = Join-Path $repoRoot $ReportPath
$reportDir = Split-Path $fullReportPath -Parent
if (-not (Test-Path $reportDir)) {
    New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
}

$report = @"
NEXUS ARCANO - Lote E - passo 4
GeneratedAt=$(Get-Date -Format "yyyy-MM-ddTHH:mm:ssK")
DeviceSerial=$serial
Package=$Package
OpenIterationsRequested=$OpenIterations
OpenSamplesCaptured=$($openSamples.Count)
OpenP95FrameMs=$openP95
OpenMedianFrameMs=$openMedian
OpenMaxFrameMs=$openMax
OpenSamples=$(Get-SamplesText -Values $openSamples)
ScrollIterationsRequested=$ScrollIterations
ScrollSamplesCaptured=$($scrollSamples.Count)
ScrollP95FrameMs=$scrollP95
ScrollMedianFrameMs=$scrollMedian
ScrollMaxFrameMs=$scrollMax
ScrollSamples=$(Get-SamplesText -Values $scrollSamples)
"@

Set-Content -Path $fullReportPath -Value $report -Encoding utf8

Write-Host "Report written to: $fullReportPath"
Write-Host "Open selector p95 frame ms: $openP95"
Write-Host "Scroll p95 frame ms: $scrollP95"
