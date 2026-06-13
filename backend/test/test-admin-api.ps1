$baseUrl = "http://localhost:8080"
$adminToken = ""
$userToken = ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "System Management Module Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Admin Login
Write-Host "[Test 1] Admin Login" -ForegroundColor Yellow
$adminBody = @"
{
  "phone": "13800138000",
  "password": "123456"
}
"@
try {
    $loginResult = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -ContentType "application/json" -Body $adminBody
    $adminToken = $loginResult.token
    Write-Host "PASS: Admin login successful" -ForegroundColor Green
    Write-Host "  Roles: $($loginResult.roles -join ', ')" -ForegroundColor Gray
} catch {
    Write-Host "FAIL: Admin login failed: $_" -ForegroundColor Red
}
Write-Host ""

# Test 2: User Login
Write-Host "[Test 2] User Login" -ForegroundColor Yellow
$userBody = @"
{
  "phone": "13899999999",
  "password": "123456"
}
"@
try {
    $userLoginResult = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -ContentType "application/json" -Body $userBody
    $userToken = $userLoginResult.token
    Write-Host "PASS: User login successful" -ForegroundColor Green
    Write-Host "  Roles: $($userLoginResult.roles -join ', ')" -ForegroundColor Gray
} catch {
    Write-Host "FAIL: User login failed: $_" -ForegroundColor Red
}
Write-Host ""

# Test 3: Operation Log Page Query (Admin)
Write-Host "[Test 3] Operation Log Page Query (GET /api/admin/logs/page)" -ForegroundColor Yellow
if ($adminToken) {
    $headers = @{"Authorization" = "Bearer $adminToken"}
    try {
        $logsResult = Invoke-RestMethod -Uri "$baseUrl/api/admin/logs/page?page=1&size=10" -Method GET -Headers $headers
        Write-Host "PASS: Log query successful" -ForegroundColor Green
        Write-Host "  Total: $($logsResult.total), Current: $($logsResult.current)" -ForegroundColor Gray
    } catch {
        Write-Host "FAIL: Log query failed: $_" -ForegroundColor Red
    }
} else {
    Write-Host "SKIP: No admin token" -ForegroundColor Gray
}
Write-Host ""

# Test 4: Operation Log Statistics (Admin)
Write-Host "[Test 4] Operation Log Statistics (GET /api/admin/logs/statistics)" -ForegroundColor Yellow
if ($adminToken) {
    $headers = @{"Authorization" = "Bearer $adminToken"}
    try {
        $statsResult = Invoke-RestMethod -Uri "$baseUrl/api/admin/logs/statistics" -Method GET -Headers $headers
        Write-Host "PASS: Statistics query successful" -ForegroundColor Green
        Write-Host "  Today Total: $($statsResult.todayTotal)" -ForegroundColor Gray
        Write-Host "  Today Fail: $($statsResult.todayFail)" -ForegroundColor Gray
        Write-Host "  Admin Count: $($statsResult.adminCount)" -ForegroundColor Gray
        Write-Host "  User Count: $($statsResult.userCount)" -ForegroundColor Gray
    } catch {
        Write-Host "FAIL: Statistics query failed: $_" -ForegroundColor Red
    }
} else {
    Write-Host "SKIP: No admin token" -ForegroundColor Gray
}
Write-Host ""

# Test 5: Dict Page Query (Admin)
Write-Host "[Test 5] Dict Page Query (GET /api/admin/dict/page)" -ForegroundColor Yellow
if ($adminToken) {
    $headers = @{"Authorization" = "Bearer $adminToken"}
    try {
        $dictResult = Invoke-RestMethod -Uri "$baseUrl/api/admin/dict/page?page=1&size=10" -Method GET -Headers $headers
        Write-Host "PASS: Dict query successful" -ForegroundColor Green
        Write-Host "  Total: $($dictResult.total), Current: $($dictResult.current)" -ForegroundColor Gray
    } catch {
        Write-Host "FAIL: Dict query failed: $_" -ForegroundColor Red
    }
} else {
    Write-Host "SKIP: No admin token" -ForegroundColor Gray
}
Write-Host ""

# Test 6: Create Dict (Admin)
Write-Host "[Test 6] Create Dict (POST /api/admin/dict)" -ForegroundColor Yellow
$newDictId = $null
if ($adminToken) {
    $headers = @{"Authorization" = "Bearer $adminToken"}
    $newDict = @"
{
  "type": "TEST_TYPE",
  "code": "test_key_001",
  "value": "Test Value",
  "description": "Auto test dict"
}
"@
    try {
        $createResult = Invoke-RestMethod -Uri "$baseUrl/api/admin/dict" -Method POST -Headers $headers -ContentType "application/json" -Body $newDict
        $newDictId = $createResult.id
        Write-Host "PASS: Dict created successfully, ID: $newDictId" -ForegroundColor Green
    } catch {
        Write-Host "FAIL: Dict creation failed: $_" -ForegroundColor Red
    }
} else {
    Write-Host "SKIP: No admin token" -ForegroundColor Gray
}
Write-Host ""

# Test 7: Update Dict (Admin)
Write-Host "[Test 7] Update Dict (PUT /api/admin/dict/{id})" -ForegroundColor Yellow
if ($adminToken -and $newDictId) {
    $headers = @{"Authorization" = "Bearer $adminToken"}
    $updateDict = @"
{
  "id": $newDictId,
  "type": "TEST_TYPE_UPDATED",
  "code": "test_key_001_updated",
  "value": "Test Value Updated",
  "description": "Auto test dict updated"
}
"@
    try {
        $updateResult = Invoke-RestMethod -Uri "$baseUrl/api/admin/dict/$newDictId" -Method PUT -Headers $headers -ContentType "application/json" -Body $updateDict
        Write-Host "PASS: Dict updated successfully" -ForegroundColor Green
    } catch {
        Write-Host "FAIL: Dict update failed: $_" -ForegroundColor Red
    }
} else {
    Write-Host "SKIP: No admin token or dict ID" -ForegroundColor Gray
}
Write-Host ""

# Test 8: Delete Dict (Admin)
Write-Host "[Test 8] Delete Dict (DELETE /api/admin/dict/{id})" -ForegroundColor Yellow
if ($adminToken -and $newDictId) {
    $headers = @{"Authorization" = "Bearer $adminToken"}
    try {
        Invoke-RestMethod -Uri "$baseUrl/api/admin/dict/$newDictId" -Method DELETE -Headers $headers
        Write-Host "PASS: Dict deleted successfully" -ForegroundColor Green
    } catch {
        Write-Host "FAIL: Dict deletion failed: $_" -ForegroundColor Red
    }
} else {
    Write-Host "SKIP: No admin token or dict ID" -ForegroundColor Gray
}
Write-Host ""

# Test 9: Permission Test - User Access Admin API
Write-Host "[Test 9] Permission Test - User Access Admin API" -ForegroundColor Yellow
if ($userToken) {
    $headers = @{"Authorization" = "Bearer $userToken"}
    try {
        Invoke-RestMethod -Uri "$baseUrl/api/admin/logs/page?page=1&size=10" -Method GET -Headers $headers
        Write-Host "FAIL: Permission control failed! User accessed admin API" -ForegroundColor Red
    } catch {
        if ($_.Exception.Response.StatusCode -eq 403) {
            Write-Host "PASS: Permission control works (403 Forbidden)" -ForegroundColor Green
        } else {
            Write-Host "PASS: Access denied" -ForegroundColor Green
        }
    }
} else {
    Write-Host "SKIP: No user token" -ForegroundColor Gray
}
Write-Host ""

# Test 10: No Token Access Admin API
Write-Host "[Test 10] Permission Test - No Token Access Admin API" -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$baseUrl/api/admin/logs/page?page=1&size=10" -Method GET
    Write-Host "FAIL: Authentication failed! No token accessed admin API" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host "PASS: Authentication works (401 Unauthorized)" -ForegroundColor Green
    } else {
        Write-Host "PASS: Access denied" -ForegroundColor Green
    }
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Completed" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
