$ErrorActionPreference = 'Stop'
$projectDir = Join-Path $PSScriptRoot 'project'
$ffmpegDir = Join-Path $PSScriptRoot 'node_modules\@ffmpeg-installer\win32-x64'
$ffprobeDir = Join-Path $PSScriptRoot 'node_modules\@ffprobe-installer\win32-x64'
$cli = Join-Path $PSScriptRoot 'node_modules\.bin\hyperframes.cmd'

if (-not (Test-Path -LiteralPath $cli)) {
    throw 'Dependencies are missing. Run npm ci in this folder first.'
}

$env:PATH = "$ffmpegDir;$ffprobeDir;$env:PATH"
Push-Location -LiteralPath $projectDir
try {
    & $cli render --output renders/endermites-test.mp4 --fps 24 --quality looks
    if ($LASTEXITCODE -ne 0) {
        throw "HyperFrames render failed with exit code $LASTEXITCODE."
    }
} finally {
    Pop-Location
}
