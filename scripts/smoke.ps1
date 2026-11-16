$root = 'C:\Users\Lenovo\Desktop\projects\book-a-bin'
$dir = Join-Path $root 'smoke-snapshots'
New-Item -Path $dir -ItemType Directory -Force | Out-Null

$max = 60; $i = 0; $ok = $false
while ($i -lt $max) {
    try {
        $r = Invoke-WebRequest -Uri 'http://localhost:8080/' -UseBasicParsing -TimeoutSec 3
        if ($r.StatusCode -eq 200) { $ok = $true; break }
    }
    catch {}
    Start-Sleep -Seconds 1
    $i++
    Write-Host "Waiting... $i"
}

if (-not $ok) {
    Write-Host 'App did not start in time'
    exit 1
}

function DoLogin($role, $user, $pwd, $path) {
    $session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $body = @{username = $user; password = $pwd; role = $role } | ConvertTo-Json
    try {
        $resp = Invoke-RestMethod -Uri 'http://localhost:8080/api/login' -Method Post -Body $body -ContentType 'application/json' -WebSession $session -ErrorAction Stop
        Write-Host "Login $role $user -> $($resp.status)"
        if ($resp.status -eq 'success') {
            Invoke-WebRequest -Uri "http://localhost:8080/$role/dashboard" -WebSession $session -OutFile $path -UseBasicParsing
            Write-Host "Saved $path"
        }
        else {
            Write-Host "Login failed: $($resp.error)"
        }
    }
    catch {
        Write-Host ("Error logging " + $user + ": " + ($_.ToString()))
    }
}

DoLogin admin 'admin1@bbmp.local' 'Admin@123' (Join-Path $dir 'admin-dashboard.html')
DoLogin collector 'collector1@bookabin.local' 'Collector@123' (Join-Path $dir 'collector-dashboard.html')
DoLogin bbmp 'bbmp1@bbmp.local' 'Bbmp@123' (Join-Path $dir 'bbmp-dashboard.html')
DoLogin user 'testuser' 'Test@123' (Join-Path $dir 'user-dashboard.html')

Write-Host 'Smoke test finished.'
