$envFilePath = Join-Path $PSScriptRoot '.env'

if (-not (Test-Path $envFilePath)) {
    throw ".env 파일이 없습니다: $envFilePath"
}

Get-Content $envFilePath -Encoding UTF8 | ForEach-Object {
    $line = $_.Trim()
    if ($line -eq '' -or $line.StartsWith('#')) { return }

    $name, $value = $line -split '=', 2
    Set-Item -Path "Env:$($name.Trim())" -Value $value.Trim()
}

k6 run @args
