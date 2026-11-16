param([int]$port = 8080)

$conns = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
if (-not $conns) {
    Write-Host ('No process found listening on port ' + $port)
    exit 0
}

$pids = $conns | Select-Object -ExpandProperty OwningProcess -Unique
foreach ($thePid in $pids) {
    try {
        $proc = Get-Process -Id $thePid -ErrorAction Stop
        Write-Host ('Found process on port ' + $port + ' PID=' + $thePid + ' Name=' + $proc.ProcessName)
        Stop-Process -Id $thePid -Force -ErrorAction Stop
        Write-Host ('Stopped PID ' + $thePid)
    }
    catch {
        Write-Host ('Failed to stop PID ' + $thePid + ': ' + ($_.ToString()))
    }
}
