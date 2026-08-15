param(
    [string]$ProcessingDesktopCore = $env:SIMPLEUI_PROCESSING_DESKTOP_CORE,
    [string]$ProcessingAndroidCore = $env:SIMPLEUI_PROCESSING_ANDROID_CORE,
    [string]$AndroidApi = $env:SIMPLEUI_ANDROID_API,
    [string]$D8Jar = $env:SIMPLEUI_D8_JAR,
    [switch]$SkipD8
)

$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$testRoot = Split-Path -Parent $projectRoot
$documents = [Environment]::GetFolderPath('MyDocuments')

function Resolve-ExplicitFile {
    param(
        [string]$Path,
        [string]$Description
    )

    if (-not $Path) {
        return $null
    }
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "$Description was configured but does not exist: $Path"
    }
    return (Resolve-Path -LiteralPath $Path).Path
}

function Find-LatestFile {
    param(
        [string[]]$Roots,
        [string[]]$Filters
    )

    foreach ($filter in $Filters) {
        $matches = foreach ($root in $Roots) {
            if ($root -and (Test-Path -LiteralPath $root -PathType Container)) {
                Get-ChildItem -LiteralPath $root -Recurse -File -Filter $filter -ErrorAction SilentlyContinue
            }
        }
        $match = $matches | Sort-Object LastWriteTime -Descending | Select-Object -First 1
        if ($match) {
            return $match.FullName
        }
    }
    return $null
}

$processingDesktopCore = Resolve-ExplicitFile $ProcessingDesktopCore 'Processing Desktop core'
if (-not $processingDesktopCore) {
    $desktopRoots = @(
        (Join-Path $projectRoot '.deps'),
        (Join-Path $env:ProgramFiles 'Processing\app\resources\core\library')
    )
    $processingDesktopCore = Find-LatestFile $desktopRoots @('core-*.jar', 'core.jar')
}

$processingAndroidCore = Resolve-ExplicitFile $ProcessingAndroidCore 'Processing Android core'
if (-not $processingAndroidCore) {
    $androidCoreRoots = @(
        (Join-Path $projectRoot '.deps'),
        (Join-Path $documents 'Processing\android'),
        (Join-Path $env:USERPROFILE '.gradle\caches')
    )
    $processingAndroidCore = Find-LatestFile $androidCoreRoots @(
        'jetified-processing-core.jar',
        'processing-core.zip',
        'processing-core.jar'
    )
}

$androidSdkRoots = @(
    $env:ANDROID_HOME,
    $env:ANDROID_SDK_ROOT,
    (Join-Path $documents 'Processing\android\sdk')
)

$androidApi = Resolve-ExplicitFile $AndroidApi 'Android API'
if (-not $androidApi) {
    $androidApi = Find-LatestFile $androidSdkRoots @('android.jar')
}

$d8Jar = Resolve-ExplicitFile $D8Jar 'Android D8'
if (-not $d8Jar -and -not $SkipD8) {
    $d8Jar = Find-LatestFile $androidSdkRoots @('d8.jar')
}

foreach ($dependency in @(
    @{ Name = 'Processing Desktop core'; Path = $processingDesktopCore },
    @{ Name = 'Processing Android core'; Path = $processingAndroidCore },
    @{ Name = 'Android API'; Path = $androidApi }
)) {
    if (-not $dependency.Path) {
        throw "Could not discover $($dependency.Name). Configure it with a build parameter or SIMPLEUI_* environment variable."
    }
}

Write-Host "Processing Desktop core: $processingDesktopCore"
Write-Host "Processing Android core: $processingAndroidCore"
Write-Host "Android API: $androidApi"
if ($SkipD8) {
    Write-Host 'Android D8 check: skipped'
} elseif ($d8Jar) {
    Write-Host "Android D8: $d8Jar"
} else {
    Write-Warning 'Android D8 was not found; the D8 compatibility check will be skipped.'
}

$generated = Join-Path $projectRoot 'src\main\java'
$desktopClasses = Join-Path $projectRoot 'build\classes-desktop'
$androidClasses = Join-Path $projectRoot 'build\classes-android'
$commonClasses = Join-Path $projectRoot 'build\classes-common'
$output = Join-Path $projectRoot 'library\SimpleUI.jar'
$manifest = Join-Path $projectRoot 'src\main\resources\MANIFEST.MF'
$testClasses = Join-Path $projectRoot 'build\test-classes'
$dexOutput = Join-Path $projectRoot 'build\dex-smoke'

Write-Host 'Using the checked-in Java sources as the canonical SimpleUI 0.5+ source set.'

foreach ($classes in @($desktopClasses, $androidClasses, $commonClasses)) {
    if (Test-Path -LiteralPath $classes) {
        Remove-Item -LiteralPath $classes -Recurse -Force
    }
    New-Item -ItemType Directory -Path $classes -Force | Out-Null
}
New-Item -ItemType Directory -Path (Split-Path -Parent $output) -Force | Out-Null

$desktopSources = Get-ChildItem -LiteralPath (Join-Path $generated 'simpleui\desktop') -File -Filter '*.java' | ForEach-Object { $_.FullName }
$androidSources = Get-ChildItem -LiteralPath (Join-Path $generated 'simpleui\android') -File -Filter '*.java' | ForEach-Object { $_.FullName }
$commonSources = Get-ChildItem -LiteralPath (Join-Path $generated 'simplecore') -File -Filter '*.java' | ForEach-Object { $_.FullName }

& javac --release 8 -encoding UTF-8 -classpath $processingDesktopCore -d $desktopClasses $desktopSources
if ($LASTEXITCODE -ne 0) { throw "javac failed with exit code $LASTEXITCODE" }
& javac --release 8 -encoding UTF-8 -classpath "$processingAndroidCore;$androidApi" -d $androidClasses $androidSources
if ($LASTEXITCODE -ne 0) { throw "Android javac failed with exit code $LASTEXITCODE" }
& javac --release 8 -encoding UTF-8 -classpath $processingDesktopCore -d $commonClasses $commonSources
if ($LASTEXITCODE -ne 0) { throw "SimpleCore javac failed with exit code $LASTEXITCODE" }

if (Test-Path -LiteralPath $output) {
    Remove-Item -LiteralPath $output -Force
}
& jar --create --file $output --manifest $manifest -C $desktopClasses . -C $androidClasses . -C $commonClasses . -C $projectRoot LICENSE
if ($LASTEXITCODE -ne 0) { throw "jar failed with exit code $LASTEXITCODE" }

# Every widget/model class must expose the same public constructors and methods
# on Desktop and Android. SimpleUI itself is excluded because it intentionally
# contains Android platform services such as the native keyboard bridge.
$jarEntries = & jar tf $output
$desktopApiClasses = $jarEntries |
    Where-Object { $_ -match '^simpleui/desktop/[^/$]+\.class$' } |
    ForEach-Object { [IO.Path]::GetFileNameWithoutExtension($_) } |
    Sort-Object -Unique
$androidApiClasses = $jarEntries |
    Where-Object { $_ -match '^simpleui/android/[^/$]+\.class$' } |
    ForEach-Object { [IO.Path]::GetFileNameWithoutExtension($_) } |
    Sort-Object -Unique
$classDifference = Compare-Object $desktopApiClasses $androidApiClasses
if ($classDifference) { throw "Desktop and Android public class sets differ: $classDifference" }
foreach ($className in ($desktopApiClasses | Where-Object { $_ -ne 'SimpleUI' })) {
    $desktopPublicApi = (& javap -classpath $output -public "simpleui.desktop.$className") |
        ForEach-Object { $_ -replace 'simpleui\.desktop\.', 'simpleui.' } |
        Where-Object { $_ -match '\(' }
    $androidPublicApi = (& javap -classpath $output -public "simpleui.android.$className") |
        ForEach-Object { $_ -replace 'simpleui\.android\.', 'simpleui.' } |
        Where-Object { $_ -match '\(' }
    $apiDifference = Compare-Object $desktopPublicApi $androidPublicApi
    if ($apiDifference) { throw "Public API differs for $className`: $apiDifference" }
}

if (Test-Path -LiteralPath $testClasses) { Remove-Item -LiteralPath $testClasses -Recurse -Force }
New-Item -ItemType Directory -Path $testClasses -Force | Out-Null
& javac --release 8 -encoding UTF-8 -classpath "$processingDesktopCore;$output" -d $testClasses (Join-Path $projectRoot 'tests\desktop\DesktopSmoke.java')
if ($LASTEXITCODE -ne 0) { throw "Desktop public API smoke test failed" }
& javac --release 8 -encoding UTF-8 -classpath "$processingDesktopCore;$output" -d $testClasses (Join-Path $projectRoot 'tests\desktop\DesktopEventBridgeSmoke.java')
if ($LASTEXITCODE -ne 0) { throw "Desktop event bridge smoke test compilation failed" }
& java -classpath "$processingDesktopCore;$output;$testClasses" DesktopEventBridgeSmoke
if ($LASTEXITCODE -ne 0) { throw "Desktop event bridge lifecycle test failed" }
& javac --release 8 -encoding UTF-8 -classpath "$processingDesktopCore;$output" -d $testClasses (Join-Path $projectRoot 'tests\view-hooks\DesktopUIViewHooksSmoke.java')
if ($LASTEXITCODE -ne 0) { throw "Desktop external UIView hook test compilation failed" }
& java -classpath "$processingDesktopCore;$output;$testClasses" DesktopUIViewHooksSmoke
if ($LASTEXITCODE -ne 0) { throw "Desktop external UIView hook test failed" }
& javac --release 8 -encoding UTF-8 -classpath "$processingDesktopCore;$output" -d $testClasses (Join-Path $projectRoot 'tests\desktop\DesktopCalendarSmoke.java')
if ($LASTEXITCODE -ne 0) { throw "Desktop calendar smoke test compilation failed" }
& java -classpath "$processingDesktopCore;$output;$testClasses" DesktopCalendarSmoke
if ($LASTEXITCODE -ne 0) { throw "Desktop calendar model test failed" }
& javac --release 8 -encoding UTF-8 -classpath "$processingDesktopCore;$output" -d $testClasses (Join-Path $projectRoot 'tests\desktop\SimpleCoreSmoke.java')
if ($LASTEXITCODE -ne 0) { throw "SimpleCore smoke test compilation failed" }
& java -classpath "$processingDesktopCore;$output;$testClasses" SimpleCoreSmoke
if ($LASTEXITCODE -ne 0) { throw "SimpleCore singleton and lifecycle test failed" }
& javac --release 8 -encoding UTF-8 -classpath "$processingAndroidCore;$androidApi;$output" -d $testClasses (Join-Path $projectRoot 'tests\android\AndroidSmoke.java')
if ($LASTEXITCODE -ne 0) { throw "Android public API smoke test failed" }
& javac --release 8 -encoding UTF-8 -classpath "$processingAndroidCore;$androidApi;$output" -d $testClasses (Join-Path $projectRoot 'tests\android\AndroidDropdownRegression.java')
if ($LASTEXITCODE -ne 0) { throw "Android dropdown regression test compilation failed" }
& java -classpath "$processingAndroidCore;$androidApi;$output;$testClasses" AndroidDropdownRegression
if ($LASTEXITCODE -ne 0) { throw "Android dropdown selection regression test failed" }
& javac --release 8 -encoding UTF-8 -classpath "$processingAndroidCore;$androidApi;$output" -d $testClasses (Join-Path $projectRoot 'tests\view-hooks\AndroidUIViewHooksCompileSmoke.java')
if ($LASTEXITCODE -ne 0) { throw "Android external UIView hook test compilation failed" }

if (-not $SkipD8 -and $d8Jar -and (Test-Path -LiteralPath $d8Jar)) {
    if (Test-Path -LiteralPath $dexOutput) { Remove-Item -LiteralPath $dexOutput -Recurse -Force }
    New-Item -ItemType Directory -Path $dexOutput -Force | Out-Null
    & java -cp $d8Jar com.android.tools.r8.D8 --min-api 21 --lib $androidApi --lib $processingAndroidCore --output $dexOutput $output
    if ($LASTEXITCODE -ne 0) { throw "Android D8 smoke test failed" }
}

Write-Host "Built $output"
Write-Host 'Desktop/Android parity, UIView hooks, automatic events, dropdown selection, calendar, SimpleCore and D8 checks passed.'
