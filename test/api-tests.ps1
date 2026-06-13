# API Test Script - User Management Module
$baseUrl = "http://localhost:8080/api"
$testResults = @()
$authToken = ""
$adminToken = ""
$testUserId = 0

function Add-TestResult {
    param(
        [string]$TestCase,
        [string]$Endpoint,
        [string]$Method,
        [bool]$Passed,
        [string]$Description,
        [string]$Response = "",
        [string]$Error = ""
    )
    $testResults += [PSCustomObject]@{
        TestCase    = $TestCase
        Endpoint    = $Endpoint
        Method      = $Method
        Passed      = $Passed
        Description = $Description
        Response    = $Response
        Error       = $Error
    }
}

function Get-AuthHeader {
    param([string]$token)
    return @{
        "Authorization" = "Bearer $token"
        "Content-Type"  = "application/json"
    }
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  API Test - User Management Module" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Test Login
Write-Host "[1] Testing Login POST /api/auth/login" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

Write-Host "`n 1.1 Admin Login Test..." -ForegroundColor Green
try {
    $loginData = @{
        phone     = "13800138000"
        password  = "123456"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginData -ContentType "application/json"
    
    if ($response.token) {
        $adminToken = $response.token
        Add-TestResult -TestCase "Admin Login - Valid Credentials" -Endpoint "/api/auth/login" -Method "POST" `
            -Passed $true -Description "Admin login successful" `
            -Response "Token: $($response.token.Substring(0, [Math]::Min(50, $response.token.Length)))..."
        Write-Host "  [PASS] Admin login successful" -ForegroundColor Green
        Write-Host "  User: ID=$($response.userId), Name=$($response.name), Type=$($response.type)" -ForegroundColor Green
    } else {
        Add-TestResult -TestCase "Admin Login - Valid Credentials" -Endpoint "/api/auth/login" -Method "POST" `
            -Passed $false -Description "Admin login failed, no token returned" -Response $response
        Write-Host "  [FAIL] Admin login failed" -ForegroundColor Red
    }
} catch {
    Add-TestResult -TestCase "Admin Login - Valid Credentials" -Endpoint "/api/auth/login" -Method "POST" `
        -Passed $false -Description "Admin login exception" -Error $_.Exception.Message
    Write-Host "  [FAIL] Admin login exception: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n 1.2 Regular User Login Test..." -ForegroundColor Green
try {
    $loginData = @{
        phone     = "13900139000"
        password  = "123456"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginData -ContentType "application/json"
    
    if ($response.token) {
        $authToken = $response.token
        Add-TestResult -TestCase "User Login - Valid Credentials" -Endpoint "/api/auth/login" -Method "POST" `
            -Passed $true -Description "User login successful" `
            -Response "Token: $($response.token.Substring(0, [Math]::Min(50, $response.token.Length)))..."
        Write-Host "  [PASS] User login successful" -ForegroundColor Green
        Write-Host "  User: ID=$($response.userId), Name=$($response.name), Type=$($response.type)" -ForegroundColor Green
    } else {
        Add-TestResult -TestCase "User Login - Valid Credentials" -Endpoint "/api/auth/login" -Method "POST" `
            -Passed $false -Description "User login failed" -Response $response
        Write-Host "  [FAIL] User login failed" -ForegroundColor Red
    }
} catch {
    Add-TestResult -TestCase "User Login - Valid Credentials" -Endpoint "/api/auth/login" -Method "POST" `
        -Passed $false -Description "User login exception" -Error $_.Exception.Message
    Write-Host "  [FAIL] User login exception: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n 1.3 Wrong Password Test..." -ForegroundColor Green
try {
    $loginData = @{
        phone     = "13800138000"
        password  = "wrongpassword"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginData -ContentType "application/json"
    Add-TestResult -TestCase "Login - Wrong Password" -Endpoint "/api/auth/login" -Method "POST" `
        -Passed $false -Description "Should fail with wrong password" -Response $response
    Write-Host "  [FAIL] Login succeeded with wrong password (should fail)" -ForegroundColor Red
} catch {
    Add-TestResult -TestCase "Login - Wrong Password" -Endpoint "/api/auth/login" -Method "POST" `
        -Passed $true -Description "Login failed as expected" -Error $_.Exception.Message
    Write-Host "  [PASS] Login failed as expected" -ForegroundColor Green
}

Write-Host "`n 1.4 Non-existent User Test..." -ForegroundColor Green
try {
    $loginData = @{
        phone     = "13999999999"
        password  = "123456"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginData -ContentType "application/json"
    Add-TestResult -TestCase "Login - Non-existent User" -Endpoint "/api/auth/login" -Method "POST" `
        -Passed $false -Description "Should fail for non-existent user" -Response $response
    Write-Host "  [FAIL] Login succeeded for non-existent user (should fail)" -ForegroundColor Red
} catch {
    Add-TestResult -TestCase "Login - Non-existent User" -Endpoint "/api/auth/login" -Method "POST" `
        -Passed $true -Description "Login failed as expected" -Error $_.Exception.Message
    Write-Host "  [PASS] Login failed as expected" -ForegroundColor Green
}

# 2. Test Register
Write-Host "`n[2] Testing Register POST /api/auth/register" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

$randomPhone = "13" + [string](Get-Random -Minimum 700000000 -Maximum 799999999)
Write-Host "`n 2.1 New User Registration (Phone: $randomPhone)..." -ForegroundColor Green
try {
    $registerData = @{
        phone    = $randomPhone
        password = "123456"
        name     = "TestUser$randomPhone"
        type     = "resident"
        gender   = "male"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -Body $registerData -ContentType "application/json"
    
    Add-TestResult -TestCase "Register - New User" -Endpoint "/api/auth/register" -Method "POST" `
        -Passed $true -Description "User registration successful" -Response "User ID created"
    Write-Host "  [PASS] User registration successful" -ForegroundColor Green
    
    Write-Host "  Verifying new user login..." -ForegroundColor Green
    $loginData = @{
        phone    = $randomPhone
        password = "123456"
    } | ConvertTo-Json
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginData -ContentType "application/json"
    
    if ($loginResponse.token) {
        Write-Host "  [PASS] New user login verification successful" -ForegroundColor Green
        Add-TestResult -TestCase "Register - Login After Register" -Endpoint "/api/auth/login" -Method "POST" `
            -Passed $true -Description "Registered user can login"
    } else {
        Write-Host "  [FAIL] New user login verification failed" -ForegroundColor Red
        Add-TestResult -TestCase "Register - Login After Register" -Endpoint "/api/auth/login" -Method "POST" `
            -Passed $false -Description "Registered user cannot login"
    }
} catch {
    Add-TestResult -TestCase "Register - New User" -Endpoint "/api/auth/register" -Method "POST" `
        -Passed $false -Description "User registration exception" -Error $_.Exception.Message
    Write-Host "  [FAIL] User registration exception: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n 2.2 Duplicate Phone Registration Test..." -ForegroundColor Green
try {
    $registerData = @{
        phone    = "13800138000"
        password = "123456"
        name     = "DuplicateTestUser"
        type     = "resident"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -Body $registerData -ContentType "application/json"
    Add-TestResult -TestCase "Register - Duplicate Phone" -Endpoint "/api/auth/register" -Method "POST" `
        -Passed $false -Description "Should fail with duplicate phone" -Response $response
    Write-Host "  [FAIL] Duplicate phone registration succeeded (should fail)" -ForegroundColor Red
} catch {
    Add-TestResult -TestCase "Register - Duplicate Phone" -Endpoint "/api/auth/register" -Method "POST" `
        -Passed $true -Description "Duplicate phone registration failed as expected" -Error $_.Exception.Message
    Write-Host "  [PASS] Duplicate phone registration failed as expected" -ForegroundColor Green
}

# 3. Test Profile
Write-Host "`n[3] Testing Profile GET/PUT /api/user/profile" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

Write-Host "`n 3.1 Get Current User Profile Test..." -ForegroundColor Green
try {
    $headers = Get-AuthHeader -token $authToken
    $response = Invoke-RestMethod -Uri "$baseUrl/user/profile" -Method Get -Headers $headers
    
    Add-TestResult -TestCase "Profile - Get User Info" -Endpoint "/api/user/profile" -Method "GET" `
        -Passed $true -Description "Get user profile successful" -Response "ID=$($response.id), Name=$($response.name)"
    Write-Host "  [PASS] Get user profile successful" -ForegroundColor Green
    Write-Host "  Profile: ID=$($response.id), Name=$($response.name), Phone=$($response.phone), Type=$($response.type)" -ForegroundColor Green
} catch {
    Add-TestResult -TestCase "Profile - Get User Info" -Endpoint "/api/user/profile" -Method "GET" `
        -Passed $false -Description "Get user profile exception" -Error $_.Exception.Message
    Write-Host "  [FAIL] Get user profile exception: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n 3.2 Update Profile Test..." -ForegroundColor Green
try {
    $headers = Get-AuthHeader -token $authToken
    $updateData = @{
        name   = "Updated Name"
        gender = "female"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$baseUrl/user/profile" -Method Put -Body $updateData -Headers $headers -ContentType "application/json"
    
    if ($response.success) {
        Add-TestResult -TestCase "Profile - Update User Info" -Endpoint "/api/user/profile" -Method "PUT" `
            -Passed $true -Description "Update profile successful" -Response $response.message
        Write-Host "  [PASS] Update profile successful" -ForegroundColor Green
        Write-Host "  Response: $($response.message)" -ForegroundColor Green
    } else {
        Add-TestResult -TestCase "Profile - Update User Info" -Endpoint "/api/user/profile" -Method "PUT" `
            -Passed $false -Description "Update profile failed" -Response $response
        Write-Host "  [FAIL] Update profile failed" -ForegroundColor Red
    }
} catch {
    Add-TestResult -TestCase "Profile - Update User Info" -Endpoint "/api/user/profile" -Method "PUT" `
        -Passed $false -Description "Update profile exception" -Error $_.Exception.Message
    Write-Host "  [FAIL] Update profile exception: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. Test Password Change
Write-Host "`n[4] Testing Password Change PUT /api/user/profile/password" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

Write-Host "`n 4.1 Change Password Test..." -ForegroundColor Green
try {
    $headers = Get-AuthHeader -token $authToken
    $passwordData = @{
        oldPassword = "123456"
        newPassword = "newpassword123"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$baseUrl/user/profile/password" -Method Put -Body $passwordData -Headers $headers -ContentType "application/json"
    
    if ($response.success) {
        Add-TestResult -TestCase "Password - Change Password" -Endpoint "/api/user/profile/password" -Method "PUT" `
            -Passed $true -Description "Password change successful" -Response $response.message
        Write-Host "  [PASS] Password change successful" -ForegroundColor Green
        Write-Host "  Response: $($response.message)" -ForegroundColor Green
        
        Write-Host "  Restoring original password..." -ForegroundColor Green
        $restoreData = @{
            oldPassword = "newpassword123"
            newPassword = "123456"
        } | ConvertTo-Json
        $restoreResponse = Invoke-RestMethod -Uri "$baseUrl/user/profile/password" -Method Put -Body $restoreData -Headers $headers -ContentType "application/json"
        
        if ($restoreResponse.success) {
            Write-Host "  [PASS] Password restored" -ForegroundColor Green
        }
    } else {
        Add-TestResult -TestCase "Password - Change Password" -Endpoint "/api/user/profile/password" -Method "PUT" `
            -Passed $false -Description "Password change failed" -Response $response
        Write-Host "  [FAIL] Password change failed" -ForegroundColor Red
    }
} catch {
    Add-TestResult -TestCase "Password - Change Password" -Endpoint "/api/user/profile/password" -Method "PUT" `
        -Passed $false -Description "Password change exception" -Error $_.Exception.Message
    Write-Host "  [FAIL] Password change exception: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n 4.2 Wrong Old Password Test..." -ForegroundColor Green
try {
    $headers = Get-AuthHeader -token $authToken
    $passwordData = @{
        oldPassword = "wrongpassword"
        newPassword = "newpassword123"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$baseUrl/user/profile/password" -Method Put -Body $passwordData -Headers $headers -ContentType "application/json"
    Add-TestResult -TestCase "Password - Wrong Old Password" -Endpoint "/api/user/profile/password" -Method "PUT" `
        -Passed $false -Description "Should fail with wrong old password" -Response $response
    Write-Host "  [FAIL] Password change succeeded with wrong old password (should fail)" -ForegroundColor Red
} catch {
    Add-TestResult -TestCase "Password - Wrong Old Password" -Endpoint "/api/user/profile/password" -Method "PUT" `
        -Passed $true -Description "Password change failed as expected" -Error $_.Exception.Message
    Write-Host "  [PASS] Password change failed as expected" -ForegroundColor Green
}

# 5. Test Theme Preference
Write-Host "`n[5] Testing Theme Preference GET/PUT /api/user/theme" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

Write-Host "`n 5.1 Get Theme Preference Test..." -ForegroundColor Green
try {
    $headers = Get-AuthHeader -token $authToken
    $response = Invoke-RestMethod -Uri "$baseUrl/user/theme" -Method Get -Headers $headers
    
    Add-TestResult -TestCase "Theme - Get Preference" -Endpoint "/api/user/theme" -Method "GET" `
        -Passed $true -Description "Get theme preference successful" -Response "Mode=$($response.data.mode), Color=$($response.data.color)"
    Write-Host "  [PASS] Get theme preference successful" -ForegroundColor Green
    Write-Host "  Current Theme: Mode=$($response.data.mode), Color=$($response.data.color)" -ForegroundColor Green
} catch {
    Add-TestResult -TestCase "Theme - Get Preference" -Endpoint "/api/user/theme" -Method "GET" `
        -Passed $false -Description "Get theme preference exception" -Error $_.Exception.Message
    Write-Host "  [FAIL] Get theme preference exception: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n 5.2 Update Theme Preference Test..." -ForegroundColor Green
try {
    $headers = Get-AuthHeader -token $authToken
    $themeData = @{
        mode  = "dark"
        color = "blue"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$baseUrl/user/theme" -Method Put -Body $themeData -Headers $headers -ContentType "application/json"
    
    if ($response.success) {
        Add-TestResult -TestCase "Theme - Update Preference" -Endpoint "/api/user/theme" -Method "PUT" `
            -Passed $true -Description "Update theme preference successful" -Response $response.message
        Write-Host "  [PASS] Update theme preference successful" -ForegroundColor Green
        Write-Host "  Response: $($response.message)" -ForegroundColor Green
        Write-Host "  New Theme: Mode=$($response.data.mode), Color=$($response.data.color)" -ForegroundColor Green
        
        Write-Host "  Restoring default theme..." -ForegroundColor Green
        $restoreData = @{
            mode  = "system"
            color = "teal"
        } | ConvertTo-Json
        $restoreResponse = Invoke-RestMethod -Uri "$baseUrl/user/theme" -Method Put -Body $restoreData -Headers $headers -ContentType "application/json"
        
        if ($restoreResponse.success) {
            Write-Host "  [PASS] Theme restored" -ForegroundColor Green
        }
    } else {
        Add-TestResult -TestCase "Theme - Update Preference" -Endpoint "/api/user/theme" -Method "PUT" `
            -Passed $false -Description "Update theme preference failed" -Response $response
        Write-Host "  [FAIL] Update theme preference failed" -ForegroundColor Red
    }
} catch {
    Add-TestResult -TestCase "Theme - Update Preference" -Endpoint "/api/user/theme" -Method "PUT" `
        -Passed $false -Description "Update theme preference exception" -Error $_.Exception.Message
    Write-Host "  [FAIL] Update theme preference exception: $($_.Exception.Message)" -ForegroundColor Red
}

# 6. Test User Management (Admin Only)
Write-Host "`n[6] Testing User Management (Admin Required)" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

if ([string]::IsNullOrEmpty($adminToken)) {
    Write-Host "  [SKIP] No admin token, skipping user management tests" -ForegroundColor Yellow
    Add-TestResult -TestCase "User Management - All Tests" -Endpoint "/api/admin/users/*" -Method "N/A" `
        -Passed $false -Description "No admin token, cannot test"
} else {
    $adminHeaders = Get-AuthHeader -token $adminToken

    Write-Host "`n 6.1 Get Users Page Test..." -ForegroundColor Green
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/admin/users/page?page=1&size=10" -Method Get -Headers $adminHeaders
        
        Add-TestResult -TestCase "UserMgmt - Get Users Page" -Endpoint "/api/admin/users/page" -Method "GET" `
            -Passed $true -Description "Get users page successful" -Response "Total=$($response.total), Size=$($response.size)"
        Write-Host "  [PASS] Get users page successful" -ForegroundColor Green
        Write-Host "  Total: $($response.total), Current Page: $($response.current), Page Size: $($response.size)" -ForegroundColor Green
        Write-Host "  Users:" -ForegroundColor Green
        foreach ($user in $response.records) {
            Write-Host "    - ID=$($user.id), Name=$($user.name), Phone=$($user.phone), Type=$($user.type)" -ForegroundColor Green
        }
    } catch {
        Add-TestResult -TestCase "UserMgmt - Get Users Page" -Endpoint "/api/admin/users/page" -Method "GET" `
            -Passed $false -Description "Get users page exception" -Error $_.Exception.Message
        Write-Host "  [FAIL] Get users page exception: $($_.Exception.Message)" -ForegroundColor Red
    }

    Write-Host "`n 6.2 Create User Test..." -ForegroundColor Green
    $newUserPhone = "13" + [string](Get-Random -Minimum 600000000 -Maximum 699999999)
    try {
        $newUserData = @{
            phone    = $newUserPhone
            password = "123456"
            name     = "AdminCreatedUser$newUserPhone"
            type     = "resident"
            gender   = "male"
            status   = 1
        } | ConvertTo-Json

        $response = Invoke-RestMethod -Uri "$baseUrl/admin/users" -Method Post -Body $newUserData -Headers $adminHeaders -ContentType "application/json"
        
        if ($response.id) {
            $testUserId = $response.id
            Add-TestResult -TestCase "UserMgmt - Create User" -Endpoint "/api/admin/users" -Method "POST" `
                -Passed $true -Description "Create user successful" -Response "ID=$($response.id), Name=$($response.name)"
            Write-Host "  [PASS] Create user successful" -ForegroundColor Green
            Write-Host "  User ID=$($response.id), Name=$($response.name), Phone=$($response.phone)" -ForegroundColor Green
        } else {
            Add-TestResult -TestCase "UserMgmt - Create User" -Endpoint "/api/admin/users" -Method "POST" `
                -Passed $false -Description "Create user failed" -Response $response
            Write-Host "  [FAIL] Create user failed" -ForegroundColor Red
        }
    } catch {
        Add-TestResult -TestCase "UserMgmt - Create User" -Endpoint "/api/admin/users" -Method "POST" `
            -Passed $false -Description "Create user exception" -Error $_.Exception.Message
        Write-Host "  [FAIL] Create user exception: $($_.Exception.Message)" -ForegroundColor Red
    }

    Write-Host "`n 6.3 Update User Test..." -ForegroundColor Green
    if ($testUserId -gt 0) {
        try {
            $updateUserData = @{
                phone  = $newUserPhone
                password = "123456"
                name   = "UpdatedUserName"
                type   = "resident"
                gender = "female"
                status = 1
            } | ConvertTo-Json

            $response = Invoke-RestMethod -Uri "$baseUrl/admin/users/$testUserId" -Method Put -Body $updateUserData -Headers $adminHeaders -ContentType "application/json"
            
            if ($response.id) {
                Add-TestResult -TestCase "UserMgmt - Update User" -Endpoint "/api/admin/users/{id}" -Method "PUT" `
                    -Passed $true -Description "Update user successful" -Response "ID=$($response.id), Name=$($response.name)"
                Write-Host "  [PASS] Update user successful" -ForegroundColor Green
                Write-Host "  Updated: ID=$($response.id), Name=$($response.name), Gender=$($response.gender)" -ForegroundColor Green
            } else {
                Add-TestResult -TestCase "UserMgmt - Update User" -Endpoint "/api/admin/users/{id}" -Method "PUT" `
                    -Passed $false -Description "Update user failed" -Response $response
                Write-Host "  [FAIL] Update user failed" -ForegroundColor Red
            }
        } catch {
            Add-TestResult -TestCase "UserMgmt - Update User" -Endpoint "/api/admin/users/{id}" -Method "PUT" `
                -Passed $false -Description "Update user exception" -Error $_.Exception.Message
            Write-Host "  [FAIL] Update user exception: $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "  [SKIP] Skip update user test (no test user ID)" -ForegroundColor Yellow
    }

    Write-Host "`n 6.4 Delete User Test..." -ForegroundColor Green
    if ($testUserId -gt 0) {
        try {
            Invoke-RestMethod -Uri "$baseUrl/admin/users/$testUserId" -Method Delete -Headers $adminHeaders
            
            Add-TestResult -TestCase "UserMgmt - Delete User" -Endpoint "/api/admin/users/{id}" -Method "DELETE" `
                -Passed $true -Description "Delete user successful" -Response "User ID=$testUserId deleted"
            Write-Host "  [PASS] Delete user successful" -ForegroundColor Green
            Write-Host "  Deleted user ID=$testUserId" -ForegroundColor Green
        } catch {
            Add-TestResult -TestCase "UserMgmt - Delete User" -Endpoint "/api/admin/users/{id}" -Method "DELETE" `
                -Passed $false -Description "Delete user exception" -Error $_.Exception.Message
            Write-Host "  [FAIL] Delete user exception: $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "  [SKIP] Skip delete user test (no test user ID)" -ForegroundColor Yellow
    }

    Write-Host "`n 6.5 Non-Admin Access Test..." -ForegroundColor Green
    try {
        $headers = Get-AuthHeader -token $authToken
        $response = Invoke-RestMethod -Uri "$baseUrl/admin/users/page?page=1&size=10" -Method Get -Headers $headers
        Add-TestResult -TestCase "UserMgmt - Permission Control" -Endpoint "/api/admin/users/page" -Method "GET" `
            -Passed $false -Description "Non-admin should not access" -Response $response
        Write-Host "  [FAIL] Non-admin can access admin endpoint (should fail)" -ForegroundColor Red
    } catch {
        Add-TestResult -TestCase "UserMgmt - Permission Control" -Endpoint "/api/admin/users/page" -Method "GET" `
            -Passed $true -Description "Non-admin access denied as expected" -Error $_.Exception.Message
        Write-Host "  [PASS] Non-admin access denied as expected" -ForegroundColor Green
    }
}

# 7. Test Role Management
Write-Host "`n[7] Testing Role Management GET /api/role" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

if ([string]::IsNullOrEmpty($adminToken)) {
    Write-Host "  [SKIP] No admin token, skipping role management tests" -ForegroundColor Yellow
    Add-TestResult -TestCase "Role Management - All Tests" -Endpoint "/api/role/*" -Method "N/A" `
        -Passed $false -Description "No admin token, cannot test"
} else {
    $adminHeaders = Get-AuthHeader -token $adminToken

    Write-Host "`n 7.1 Get All Roles Test..." -ForegroundColor Green
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/role" -Method Get -Headers $adminHeaders
        
        Add-TestResult -TestCase "RoleMgmt - Get All Roles" -Endpoint "/api/role" -Method "GET" `
            -Passed $true -Description "Get roles successful" -Response "Role Count=$($response.Count)"
        Write-Host "  [PASS] Get roles successful" -ForegroundColor Green
        Write-Host "  Roles:" -ForegroundColor Green
        foreach ($role in $response) {
            Write-Host "    - ID=$($role.id), Name=$($role.name), Code=$($role.code)" -ForegroundColor Green
        }
    } catch {
        Add-TestResult -TestCase "RoleMgmt - Get All Roles" -Endpoint "/api/role" -Method "GET" `
            -Passed $false -Description "Get roles exception" -Error $_.Exception.Message
        Write-Host "  [FAIL] Get roles exception: $($_.Exception.Message)" -ForegroundColor Red
    }

    Write-Host "`n 7.2 Non-Admin Access Test..." -ForegroundColor Green
    try {
        $headers = Get-AuthHeader -token $authToken
        $response = Invoke-RestMethod -Uri "$baseUrl/role" -Method Get -Headers $headers
        Add-TestResult -TestCase "RoleMgmt - Permission Control" -Endpoint "/api/role" -Method "GET" `
            -Passed $false -Description "Non-admin should not access" -Response $response
        Write-Host "  [FAIL] Non-admin can access role endpoint (should fail)" -ForegroundColor Red
    } catch {
        Add-TestResult -TestCase "RoleMgmt - Permission Control" -Endpoint "/api/role" -Method "GET" `
            -Passed $true -Description "Non-admin access denied as expected" -Error $_.Exception.Message
        Write-Host "  [PASS] Non-admin access denied as expected" -ForegroundColor Green
    }
}

# Generate Test Report
Write-Host "`n" -NoNewline
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "           TEST REPORT SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$totalTests = $testResults.Count
$passedTests = ($testResults | Where-Object { $_.Passed }).Count
$failedTests = $totalTests - $passedTests
if ($totalTests -gt 0) {
    $passRate = [Math]::Round(($passedTests / $totalTests) * 100, 2)
} else {
    $passRate = 0
}

Write-Host "Total Tests: $totalTests" -ForegroundColor Cyan
Write-Host "Passed: $passedTests" -ForegroundColor Green
Write-Host "Failed: $failedTests" -ForegroundColor Red
Write-Host "Pass Rate: $passRate%" -ForegroundColor Cyan
Write-Host ""

Write-Host "By Module:" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

$moduleStats = $testResults | Group-Object { $_.TestCase.Split(" - ")[0] }
foreach ($stat in $moduleStats) {
    $modulePassed = ($stat.Group | Where-Object { $_.Passed }).Count
    $moduleTotal = $stat.Group.Count
    if ($moduleTotal -gt 0) {
        $moduleRate = [Math]::Round(($modulePassed / $moduleTotal) * 100, 2)
    } else {
        $moduleRate = 0
    }
    $color = if ($modulePassed -eq $moduleTotal) { "Green" } else { "Yellow" }
    Write-Host "  $($stat.Name): $modulePassed/$moduleTotal ($moduleRate%)" -ForegroundColor $color
}

Write-Host ""
Write-Host "Detailed Results:" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

foreach ($result in $testResults) {
    $icon = if ($result.Passed) { "PASS" } else { "FAIL" }
    $color = if ($result.Passed) { "Green" } else { "Red" }
    Write-Host "  [$icon] $($result.TestCase)" -ForegroundColor $color
    Write-Host "      Endpoint: $($result.Method) $($result.Endpoint)" -ForegroundColor Gray
    Write-Host "      Description: $($result.Description)" -ForegroundColor Gray
    if ($result.Response) {
        Write-Host "      Response: $($result.Response)" -ForegroundColor Gray
    }
    if ($result.Error) {
        Write-Host "      Error: $($result.Error)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "          TEST EXECUTION COMPLETE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Export report to file
$reportFile = "c:\Users\fengchunzhao\Desktop\future-community-system\api-test-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').txt"
$testResults | Format-Table -AutoSize | Out-File -FilePath $reportFile -Encoding utf8
Write-Host "`nTest report saved to: $reportFile" -ForegroundColor Cyan
