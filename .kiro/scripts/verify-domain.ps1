# TimeBack APP-05~APP-09 pure-domain verifier. Compiles Java 17 sources and executes synthetic tests.
[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$mainSource = Join-Path $root 'src\main\java'
$testSource = Join-Path $root 'src\test\java'
$output = Join-Path $root 'build\domain-classes'

if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    throw 'javac is required; JDK 17 must be available on PATH.'
}
if (-not (Test-Path $mainSource) -or -not (Test-Path $testSource)) {
    throw 'Expected src/main/java and src/test/java source directories.'
}

$files = @(Get-ChildItem -Path $mainSource, $testSource -Filter '*.java' -Recurse | ForEach-Object { $_.FullName })
if ($files.Count -eq 0) {
    throw 'No Java source files found.'
}

New-Item -ItemType Directory -Force -Path $output | Out-Null
& javac --release 17 -encoding UTF-8 -d $output @files
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

& java -ea -cp $output io.timeback.domain.DomainEngineTestRunner
exit $LASTEXITCODE
