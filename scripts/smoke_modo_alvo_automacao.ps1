param(
    [string]$Package = "com.gurps.ficha.visual",
    [string]$ReportPath = "app/build/reports/nexus_arcano_lote4_smoke_modo_alvo_automacao.txt"
)

$ErrorActionPreference = "Stop"

function Normalize-Text {
    param([string]$Value)
    if ([string]::IsNullOrWhiteSpace($Value)) { return "" }
    $normalized = $Value.Normalize([Text.NormalizationForm]::FormD)
    $withoutMarks = [string]::Concat(
        ($normalized.ToCharArray() | Where-Object {
            [Globalization.CharUnicodeInfo]::GetUnicodeCategory($_) -ne [Globalization.UnicodeCategory]::NonSpacingMark
        })
    )
    return ($withoutMarks.ToLowerInvariant() -replace "[^a-z0-9\s:\.-]", " " -replace "\s+", " ").Trim()
}

function Resolve-AdbPath {
    $adbCommand = Get-Command adb -ErrorAction SilentlyContinue
    if ($adbCommand) { return $adbCommand.Source }

    $repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
    $localPropertiesPath = Join-Path $repoRoot "local.properties"
    if (-not (Test-Path $localPropertiesPath)) {
        throw "local.properties not found and adb not in PATH."
    }

    $sdkLine = Get-Content $localPropertiesPath | Where-Object { $_ -match "^sdk\.dir=" } | Select-Object -First 1
    if (-not $sdkLine) { throw "sdk.dir missing in local.properties." }
    $sdkDir = $sdkLine.Substring("sdk.dir=".Length).Trim()
    $sdkDir = $sdkDir -replace "\\:", ":"
    $sdkDir = $sdkDir -replace "\\\\", "\"
    $adbFromSdk = Join-Path $sdkDir "platform-tools\adb.exe"
    if (-not (Test-Path $adbFromSdk)) { throw "adb not found at '$adbFromSdk'." }
    return $adbFromSdk
}

$script:AdbPath = Resolve-AdbPath

function Invoke-AdbCapture {
    param([string[]]$CommandArgs)
    $output = & $script:AdbPath @CommandArgs
    if ($LASTEXITCODE -ne 0) {
        throw "adb failed: $($CommandArgs -join ' ')"
    }
    return ($output | Out-String).Trim()
}

function Invoke-AdbNoCapture {
    param([string[]]$CommandArgs)
    & $script:AdbPath @CommandArgs | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "adb failed: $($CommandArgs -join ' ')"
    }
}

function Get-UiXml {
    $remoteDumpPath = "/sdcard/window_dump_smoke_modo_alvo.xml"
    Invoke-AdbCapture @("shell", "uiautomator", "dump", $remoteDumpPath) | Out-Null
    return [xml](Invoke-AdbCapture @("shell", "cat", $remoteDumpPath))
}

function Parse-Bounds {
    param([string]$Bounds)
    if ($Bounds -match "\[(\d+),(\d+)\]\[(\d+),(\d+)\]") {
        $x1 = [int]$matches[1]
        $y1 = [int]$matches[2]
        $x2 = [int]$matches[3]
        $y2 = [int]$matches[4]
        return [pscustomobject]@{
            CenterX = [int](($x1 + $x2) / 2)
            CenterY = [int](($y1 + $y2) / 2)
        }
    }
    return $null
}

function Find-Node {
    param(
        [xml]$Xml,
        [string]$Needle
    )
    $needleNorm = Normalize-Text $Needle
    foreach ($node in $Xml.SelectNodes("//node")) {
        $text = [string]$node.text
        $desc = [string]$node.'content-desc'
        $hay = Normalize-Text "$text $desc"
        if ($hay.Contains($needleNorm)) {
            $bounds = Parse-Bounds ([string]$node.bounds)
            if ($bounds) {
                return [pscustomobject]@{
                    Bounds = $bounds
                    Text = $text
                    Desc = $desc
                }
            }
        }
    }
    return $null
}

function Wait-Node {
    param(
        [string]$Needle,
        [int]$Attempts = 35,
        [int]$SleepMs = 300
    )
    for ($i = 1; $i -le $Attempts; $i++) {
        $xml = Get-UiXml
        $node = Find-Node -Xml $xml -Needle $Needle
        if ($node) {
            return [pscustomobject]@{ Node = $node; Xml = $xml }
        }
        Start-Sleep -Milliseconds $SleepMs
    }
    throw "Node not found: $Needle"
}

function Tap-Label {
    param([string]$Needle)
    $result = Wait-Node -Needle $Needle
    Invoke-AdbNoCapture @("shell", "input", "tap", "$($result.Node.Bounds.CenterX)", "$($result.Node.Bounds.CenterY)")
    Start-Sleep -Milliseconds 450
    return $result
}

function Node-Exists {
    param([xml]$Xml, [string]$Needle)
    return [bool](Find-Node -Xml $Xml -Needle $Needle)
}

function Try-Tap {
    param([string[]]$Needles)
    foreach ($needle in $Needles) {
        try {
            Tap-Label -Needle $needle | Out-Null
            return $true
        } catch {
            continue
        }
    }
    return $false
}

$serial = Invoke-AdbCapture @("get-serialno")
Invoke-AdbNoCapture @("logcat", "-c")
Invoke-AdbNoCapture @("shell", "am", "force-stop", $Package)
Start-Sleep -Milliseconds 250
Invoke-AdbCapture @("shell", "monkey", "-p", $Package, "-c", "android.intent.category.LAUNCHER", "1") | Out-Null
Start-Sleep -Seconds 3

$xmlStart = Get-UiXml
$hadMagiaTabAtStart = Node-Exists -Xml $xmlStart -Needle "Magia"
$addedAptidao = $false

if (-not $hadMagiaTabAtStart) {
    try {
        Try-Tap -Needles @("Tracos", "Traços") | Out-Null
        Tap-Label -Needle "Adicionar Vantagem" | Out-Null
        Wait-Node -Needle "Selecionar Vantagem" | Out-Null
        Tap-Label -Needle "Buscar" | Out-Null
        Invoke-AdbNoCapture @("shell", "input", "text", "aptidao")
        Start-Sleep -Milliseconds 700
        $picked = Try-Tap -Needles @("Aptidao Magica", "Aptidão Mágica")
        if ($picked) {
            Tap-Label -Needle "Adicionar" | Out-Null
            Start-Sleep -Milliseconds 700
            Try-Tap -Needles @("Fechar") | Out-Null
            Start-Sleep -Milliseconds 700
            $addedAptidao = $true
        }
    } catch {
        $addedAptidao = $false
    }
}

$xmlBeforeMagia = Get-UiXml
$hasMagiaTab = Node-Exists -Xml $xmlBeforeMagia -Needle "Magia"
if (-not $hasMagiaTab) {
    throw "Magia tab not available even after setup."
}

Tap-Label -Needle "Magia" | Out-Null
Tap-Label -Needle "Adicionar Magia" | Out-Null
Wait-Node -Needle "Selecionar Magia" | Out-Null

$xmlSelector = Get-UiXml
$hasModoAlvoChip = Node-Exists -Xml $xmlSelector -Needle "Modo Alvo"
if (-not $hasModoAlvoChip) {
    throw "Modo Alvo chip not visible in selector."
}

Tap-Label -Needle "Modo Alvo" | Out-Null
Tap-Label -Needle "Buscar" | Out-Null
Invoke-AdbNoCapture @("shell", "input", "text", "desejo")
Start-Sleep -Milliseconds 900
Tap-Label -Needle "Definir Alvo" | Out-Null
Start-Sleep -Seconds 2

$xmlAfterTarget = Get-UiXml
$hasAlvoLine = Node-Exists -Xml $xmlAfterTarget -Needle "Alvo:"
$hasGuideLine = (Node-Exists -Xml $xmlAfterTarget -Needle "Proxima recomendada") -or
    (Node-Exists -Xml $xmlAfterTarget -Needle "Proxima etapa recomendada") -or
    (Node-Exists -Xml $xmlAfterTarget -Needle "Sem recomendacao imediata liberada")
$hasProgressLine = (Node-Exists -Xml $xmlAfterTarget -Needle "Escolas") -or
    (Node-Exists -Xml $xmlAfterTarget -Needle "Cadeia")

$recommendNode = Find-Node -Xml $xmlAfterTarget -Needle "Proxima recomendada:"
if (-not $recommendNode) {
    $recommendNode = Find-Node -Xml $xmlAfterTarget -Needle "Proxima etapa recomendada:"
}

$triedAddRecommended = $false
$addedRecommended = $false
$addedSpell = ""

if ($recommendNode) {
    $combined = "$($recommendNode.Text) $($recommendNode.Desc)".Trim()
    $combinedNorm = Normalize-Text $combined
    if ($combinedNorm -match ":\s*(.+)$") {
        $spellNorm = $matches[1].Trim()
        if ($spellNorm.Length -gt 1) {
            $triedAddRecommended = $true
            if ($combined -match ":\s*(.+)$") {
                $addedSpell = $matches[1].Trim()
            }
            try {
                Tap-Label -Needle $spellNorm | Out-Null
                Wait-Node -Needle "Adicionar" | Out-Null
                Tap-Label -Needle "Adicionar" | Out-Null
                Start-Sleep -Milliseconds 900
                $addedRecommended = $true
            } catch {
                $addedRecommended = $false
            }
        }
    }
}

Try-Tap -Needles @("Fechar") | Out-Null
Start-Sleep -Milliseconds 500

$activities = Invoke-AdbCapture @("shell", "dumpsys", "activity", "activities")
$resumedLine = ($activities -split "`n" | Where-Object {
        $_ -match "topResumedActivity|mResumedActivity" -and $_ -match "com\.gurps\.ficha\.visual"
    } | Select-Object -First 1)

$logcat = Invoke-AdbCapture @("logcat", "-d", "-v", "brief")
$fatal = ($logcat -split "`n" | Where-Object {
        $_ -match "FATAL EXCEPTION|ANR in com\.gurps\.ficha\.visual|Input dispatching timed out"
    })

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$fullReportPath = Join-Path $repoRoot $ReportPath
$reportDir = Split-Path $fullReportPath -Parent
if (-not (Test-Path $reportDir)) {
    New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
}

$lines = @()
$lines += "SMOKE_MODO_ALVO_AUTOMACAO"
$lines += "GeneratedAt=$(Get-Date -Format 'yyyy-MM-ddTHH:mm:ssK')"
$lines += "DeviceSerial=$serial"
$lines += "Package=$Package"
$lines += "HadMagiaTabAtStart=$hadMagiaTabAtStart"
$lines += "AddedAptidaoMagicaForSetup=$addedAptidao"
$lines += "HasMagiaTab=$hasMagiaTab"
$lines += "HasModoAlvoChip=$hasModoAlvoChip"
$lines += "HasAlvoLine=$hasAlvoLine"
$lines += "HasGuideLine=$hasGuideLine"
$lines += "HasProgressLine=$hasProgressLine"
$lines += "TriedAddRecommended=$triedAddRecommended"
$lines += "AddedRecommended=$addedRecommended"
$lines += "AddedSpell=$addedSpell"
$lines += "ResumedVisual=$([bool]($null -ne $resumedLine))"
$lines += "FatalOrAnrDetected=$([bool]($fatal -and $fatal.Count -gt 0))"
if ($resumedLine) {
    $lines += "ResumedLine=$($resumedLine.Trim())"
}
if ($fatal -and $fatal.Count -gt 0) {
    $lines += "FatalSample=$(($fatal | Select-Object -First 1).Trim())"
}

Set-Content -Path $fullReportPath -Value ($lines -join "`n") -Encoding utf8
Write-Host "Report written to: $fullReportPath"
Write-Host ($lines -join "`n")
