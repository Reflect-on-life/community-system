Write-Host "Creating admin user for testing..."

# Create a new admin user
$adminUser = @"
{
  "name": "系统管理员",
  "phone": "13999999999",
  "password": "123456",
  "type": "admin",
  "gender": "male"
}
"@

try {
    Write-Host "Registering admin user..."
    $registerResult = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method POST -ContentType "application/json" -Body $adminUser
    Write-Host "Admin user registered successfully!"
    
    Write-Host "`nLogging in as admin..."
    $loginBody = @"
{
  "phone": "13999999999",
  "password": "123456"
}
"@
    $loginResult = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body $loginBody
    Write-Host "Admin login successful!"
    Write-Host "Token: $($loginResult.token)"
    Write-Host "Roles: $($loginResult.roles -join ', ')"
    
    # Assign admin role manually via SQL if needed
    if ($loginResult.roles -notcontains "ROLE_ADMIN") {
        Write-Host "`nAdmin role not assigned, assigning manually..."
        $userId = $loginResult.userId
        mysql -u root -proot future_community -e "INSERT INTO user_role (user_id, role_id) VALUES ($userId, 1) ON DUPLICATE KEY UPDATE role_id = 1;"
        Write-Host "Admin role assigned!"
        
        # Login again to get updated roles
        Write-Host "`nRe-logging in to get updated roles..."
        $loginResult = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body $loginBody
        Write-Host "Admin login successful!"
        Write-Host "Roles: $($loginResult.roles -join ', ')"
    }
    
    # Save token for later use
    $global:adminToken = $loginResult.token
    Write-Host "`n=== Admin Token ==="
    Write-Host $global:adminToken
    
} catch {
    Write-Host "Error: $_"
    if ($_.Exception.Response.StatusCode) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode)"
    }
}
