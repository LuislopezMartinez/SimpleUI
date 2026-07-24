param(
    [string]$ExpectedVersion
)

$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$releaseRoot = [IO.Path]::GetFullPath((Join-Path $projectRoot 'release'))
$stageRoot = Join-Path $releaseRoot 'stage'
$stageLibrary = Join-Path $stageRoot 'SimpleUI'
$propertiesPath = Join-Path $projectRoot 'library.properties'

if (-not $releaseRoot.StartsWith(
    [IO.Path]::GetFullPath($projectRoot) + [IO.Path]::DirectorySeparatorChar,
    [StringComparison]::OrdinalIgnoreCase
)) {
    throw "Unsafe release directory: $releaseRoot"
}

function Read-LibraryProperties {
    param([string]$Path)

    $result = @{}
    foreach ($line in Get-Content -LiteralPath $Path -Encoding UTF8) {
        $trimmed = $line.Trim()
        if (-not $trimmed -or $trimmed.StartsWith('#')) {
            continue
        }
        $separator = $trimmed.IndexOf('=')
        if ($separator -lt 1) {
            throw "Invalid library.properties line: $line"
        }
        $key = $trimmed.Substring(0, $separator).Trim()
        $value = $trimmed.Substring($separator + 1).Trim()
        $result[$key] = $value
    }
    return $result
}

$properties = Read-LibraryProperties $propertiesPath
$requiredProperties = @(
    'name',
    'authors',
    'url',
    'categories',
    'sentence',
    'version',
    'prettyVersion',
    'minRevision',
    'maxRevision'
)
foreach ($key in $requiredProperties) {
    if (-not $properties.ContainsKey($key) -or -not $properties[$key]) {
        throw "library.properties is missing a value for '$key'."
    }
}
if ($properties.name -ne 'SimpleUI') {
    throw "The release name must remain SimpleUI; found '$($properties.name)'."
}
if ($properties.url -notmatch '^https?://') {
    throw "The library URL must start with http:// or https://."
}
$integerValue = 0
foreach ($key in @('version', 'minRevision', 'maxRevision')) {
    if (-not [int]::TryParse($properties[$key], [ref]$integerValue)) {
        throw "'$key' must be an integer."
    }
}
if ($ExpectedVersion) {
    $normalizedExpectedVersion = $ExpectedVersion -replace '^v', ''
    if ($properties.prettyVersion -ne $normalizedExpectedVersion) {
        throw "Tag/version mismatch: expected $normalizedExpectedVersion but library.properties contains $($properties.prettyVersion)."
    }
}

Write-Host "Building SimpleUI $($properties.prettyVersion)..."
& (Join-Path $projectRoot 'build.ps1')

$jarPath = Join-Path $projectRoot 'library\SimpleUI.jar'
if (-not (Test-Path -LiteralPath $jarPath -PathType Leaf)) {
    throw "Build did not produce library/SimpleUI.jar."
}

if (Test-Path -LiteralPath $releaseRoot) {
    Remove-Item -LiteralPath $releaseRoot -Recurse -Force
}
New-Item -ItemType Directory -Path $stageLibrary -Force | Out-Null

foreach ($directory in @('examples', 'library', 'reference', 'src')) {
    Copy-Item -LiteralPath (Join-Path $projectRoot $directory) -Destination $stageLibrary -Recurse
}
foreach ($file in @(
    'library.properties',
    'LICENSE',
    'README.md',
    'CHANGELOG.md'
)) {
    Copy-Item -LiteralPath (Join-Path $projectRoot $file) -Destination $stageLibrary
}

$zipPath = Join-Path $releaseRoot 'SimpleUI.zip'
$propertiesReleasePath = Join-Path $releaseRoot 'SimpleUI.txt'
$pdexPath = Join-Path $releaseRoot 'SimpleUI.pdex'

Compress-Archive -LiteralPath $stageLibrary -DestinationPath $zipPath -CompressionLevel Optimal
Copy-Item -LiteralPath $propertiesPath -Destination $propertiesReleasePath
Copy-Item -LiteralPath $zipPath -Destination $pdexPath

$sourcePropertiesHash = (Get-FileHash -LiteralPath $propertiesPath -Algorithm SHA256).Hash
$releasePropertiesHash = (Get-FileHash -LiteralPath $propertiesReleasePath -Algorithm SHA256).Hash
if ($sourcePropertiesHash -ne $releasePropertiesHash) {
    throw 'SimpleUI.txt is not an exact copy of library.properties.'
}
$zipHash = (Get-FileHash -LiteralPath $zipPath -Algorithm SHA256).Hash
$pdexHash = (Get-FileHash -LiteralPath $pdexPath -Algorithm SHA256).Hash
if ($zipHash -ne $pdexHash) {
    throw 'SimpleUI.pdex is not byte-identical to SimpleUI.zip.'
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [IO.Compression.ZipFile]::OpenRead($zipPath)
try {
    $entryNames = $archive.Entries | ForEach-Object { $_.FullName -replace '\\', '/' }
    foreach ($requiredEntry in @(
        'SimpleUI/library/SimpleUI.jar',
        'SimpleUI/library.properties',
        'SimpleUI/reference/index.html'
    )) {
        if ($requiredEntry -notin $entryNames) {
            throw "Release archive is missing $requiredEntry."
        }
    }
    $unexpectedLibraryFiles = $entryNames |
        Where-Object { $_ -like 'SimpleUI/library/*' -and $_ -notlike '*/' -and $_ -ne 'SimpleUI/library/SimpleUI.jar' }
    if ($unexpectedLibraryFiles) {
        throw "Unexpected files in the exported library folder: $($unexpectedLibraryFiles -join ', ')"
    }
} finally {
    $archive.Dispose()
}

Remove-Item -LiteralPath $stageRoot -Recurse -Force

Write-Host 'Release artifacts are ready:'
Get-Item $zipPath, $propertiesReleasePath, $pdexPath |
    Select-Object Name, Length |
    Format-Table -AutoSize
