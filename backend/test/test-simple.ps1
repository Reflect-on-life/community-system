$adminBody = @"
{
  "phone": "13800138000",
  "password": "123456"
}
"@

try {
    $loginResult = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body $adminBody
    $adminToken = $loginResult.token
    Write-Host "Login successful"
    Write-Host "Token: $adminToken"
    Write-Host "Roles: $($loginResult.roles -join ', ')"
    Write-Host "UserId: $($loginResult.userId)"
    
    $headers = @{"Authorization" = "Bearer $adminToken"}
    
    Write-Host "`nTesting /api/admin/logs/page..."
    $logsResult = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/logs/page?page=1&size=10" -Method GET -Headers $headers
    Write-Host "Success! Total: $($logsResult.total)"
} catch {
    Write-Host "Error: $_"
    Write-Host "Error Details: $($_.Exception.Response.StatusCode)"
    Write-Host "Error Message: $($_.ErrorDetails)"
}
