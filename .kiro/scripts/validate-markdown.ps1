<#
.SYNOPSIS
TimeBack Markdown과 Kiro 설정 문서의 최소 구조를 검사한다.

.DESCRIPTION
목적: 에이전트가 저장한 Markdown에서 병합 충돌 표식, 손상된 문자, Kiro frontmatter 누락을 조기에 발견한다.
역할: 전달받은 파일이 workspace 내부인지 확인하고 일반 Markdown, steering, SKILL.md에 맞는 읽기 전용 검사를 수행한다.
사용 흐름:
1. validate-timeback-markdown hook이 PostFileSave 이벤트에서 저장된 Markdown 경로를 전달한다.
2. 경로를 workspace 기준 절대 경로로 정규화하고 workspace 외부 접근을 거부한다.
3. NUL 문자와 미해결 병합 충돌 표식을 검사한다.
4. steering과 SKILL.md이면 frontmatter 및 Skill 필수 필드를 추가 검사한다.
5. 문제가 있으면 exit 1과 오류 내용을, 없으면 exit 0과 통과 메시지를 반환한다.
주의: 파일을 수정하거나 자동 포맷하지 않으며, 제품 요구사항의 의미적 정합성까지 판정하지 않는다.
#>

param(
    [Parameter(Mandatory = $true)]
    [string] $FilePath
)

$ErrorActionPreference = 'Stop'
$workspaceRoot = [IO.Path]::GetFullPath((Get-Location).Path)
$fullPath = if ([IO.Path]::IsPathRooted($FilePath)) {
    [IO.Path]::GetFullPath($FilePath)
} else {
    [IO.Path]::GetFullPath((Join-Path $workspaceRoot $FilePath))
}

$rootPrefix = $workspaceRoot.TrimEnd('\', '/') + [IO.Path]::DirectorySeparatorChar
if ($fullPath -ne $workspaceRoot -and
    -not $fullPath.StartsWith($rootPrefix, [StringComparison]::OrdinalIgnoreCase)) {
    [Console]::Error.WriteLine("Workspace outside path rejected: $fullPath")
    exit 1
}

if (-not (Test-Path -LiteralPath $fullPath -PathType Leaf)) {
    [Console]::Error.WriteLine("Markdown file not found: $fullPath")
    exit 1
}

$content = [IO.File]::ReadAllText($fullPath)
$issues = New-Object 'System.Collections.Generic.List[string]'

if ($content.IndexOf([char]0) -ge 0) {
    $issues.Add('NUL character detected.')
}

if ($content -match '(?m)^(<<<<<<<|=======|>>>>>>>)') {
    $issues.Add('Unresolved merge conflict marker detected.')
}

$relativePath = $fullPath.Substring($workspaceRoot.Length).TrimStart([char]92, [char]47).Replace([char]92, [char]47)
if ($relativePath -match '^\.kiro/steering/.+\.md$' -and $content -notmatch '\A---\r?\n') {
    $issues.Add('Steering frontmatter must be the first content in the file.')
}

if ($relativePath -match '^\.kiro/skills/[^/]+/SKILL\.md$') {
    if ($content -notmatch '\A---\r?\n') {
        $issues.Add('SKILL.md frontmatter must be the first content in the file.')
    }
    if ($content -notmatch '(?m)^name:\s*[a-z0-9-]+\s*$') {
        $issues.Add('SKILL.md requires a lowercase name field.')
    }
    if ($content -notmatch '(?m)^description:\s*\S.+$') {
        $issues.Add('SKILL.md requires a non-empty description field.')
    }
}

if ($issues.Count -gt 0) {
    foreach ($issue in $issues) {
        [Console]::Error.WriteLine("${relativePath}: $issue")
    }
    exit 1
}

Write-Output "Markdown validation passed: $relativePath"
exit 0
