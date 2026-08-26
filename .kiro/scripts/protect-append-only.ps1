<#
.SYNOPSIS
AI-DLC append-only 기록에 대한 파괴적 도구 호출을 차단한다.

.DESCRIPTION
목적: spec.md, audit.md, mistakes.md의 기존 원문과 감사 이력이 덮어쓰기·치환·삭제로 손상되는 것을 방지한다.
역할: PreToolUse hook이 전달한 JSON에서 대상 경로 속성을 재귀적으로 찾아 보호 파일인지 판정한다.
사용 흐름:
1. protect-aidlc-append-only hook이 fs_write, str_replace, delete_file 실행 전에 이벤트 JSON을 표준 입력으로 전달한다.
2. JSON 안의 path, targetFile, filePath 계열 속성을 수집하고 경로 구분자를 정규화한다.
3. 보호 대상 파일이 아직 없으면 최초 복구 기록 생성을 허용한다.
4. 보호 대상 파일이 이미 있으면 오류 메시지와 exit 2를 반환해 덮어쓰기·치환·삭제를 차단한다.
5. 보호 대상이 아니면 exit 0으로 통과시키며, append 전용 fs_append는 hook matcher 대상이 아니므로 허용된다.
주의: 입력 JSON을 해석할 수 없을 때는 작업을 임의로 차단하지 않고 경고 후 통과시킨다.
#>

$rawInput = [Console]::In.ReadToEnd()
if ([string]::IsNullOrWhiteSpace($rawInput)) {
    exit 0
}

try {
    $payload = $rawInput | ConvertFrom-Json -ErrorAction Stop
} catch {
    [Console]::Error.WriteLine('Append-only guard could not parse hook input; operation was not blocked.')
    exit 0
}

$pathPropertyNames = @('path', 'targetFile', 'target_file', 'filePath', 'file_path')
$pathValues = New-Object 'System.Collections.Generic.List[string]'

function Collect-TargetPaths {
    param([object] $Value)

    if ($null -eq $Value) { return }

    if ($Value -is [System.Collections.IDictionary]) {
        foreach ($key in $Value.Keys) {
            if ($pathPropertyNames -contains [string]$key -and $Value[$key] -is [string]) {
                $pathValues.Add([string]$Value[$key])
            } else {
                Collect-TargetPaths $Value[$key]
            }
        }
        return
    }

    if ($Value -is [System.Collections.IEnumerable] -and $Value -isnot [string]) {
        foreach ($item in $Value) { Collect-TargetPaths $item }
        return
    }

    if ($Value -is [string] -or $Value.GetType().IsPrimitive) {
        return
    }

    foreach ($property in $Value.PSObject.Properties) {
        if ($pathPropertyNames -contains $property.Name -and $property.Value -is [string]) {
            $pathValues.Add([string]$property.Value)
        } else {
            Collect-TargetPaths $property.Value
        }
    }
}

Collect-TargetPaths $payload
$protectedSuffixes = @(
    'spec.md',
    'aidlc-docs/audit.md',
    'aidlc-docs/mistakes.md'
)

foreach ($pathValue in $pathValues) {
    $normalized = ($pathValue -replace '\\', '/').TrimStart('.', '/').ToLowerInvariant()
    foreach ($protected in $protectedSuffixes) {
        if ($normalized -eq $protected -or $normalized.EndsWith('/' + $protected)) {
            $fullTargetPath = if ([IO.Path]::IsPathRooted($pathValue)) {
                [IO.Path]::GetFullPath($pathValue)
            } else {
                [IO.Path]::GetFullPath((Join-Path (Get-Location).Path $pathValue))
            }

            if (Test-Path -LiteralPath $fullTargetPath -PathType Leaf) {
                [Console]::Error.WriteLine("Blocked destructive write to existing append-only record: $pathValue. Use fs_append instead.")
                exit 2
            }
        }
    }
}

exit 0
