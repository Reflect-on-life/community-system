$ErrorActionPreference = 'Continue'

$baseUrl = "http://localhost:8080/api"
$results = @()
$adminToken = ""
$userToken = ""

function Test-Api {
    param($name, $url, $method, $body, $headers, $expectSuccess)
    try {
        $params = @{Uri=$url; Method=$method; ContentType='application/json'; UseBasicParsing=$true}
        if ($body) { $params.Body = $body }
        if ($headers) { $params.Headers = $headers }
        
        $response = Invoke-RestMethod @params
        
        if ($expectSuccess) {
            return @{Pass=$true; Response=$response; Error=''}
        } else {
            return @{Pass=$false; Response=$response; Error='Expected failure but succeeded'}
        }
    } catch {
        if (-not $expectSuccess) {
            return @{Pass=$true; Response=$null; Error=$_.Exception.Message}
        } else {
            return @{Pass=$false; Response=$null; Error=$_.Exception.Message}
        }
    }
}

Write-Host "=== API Test Start ===" -ForegroundColor Cyan

# Test 1: Admin Login
Write-Host "`n[Test 1] Admin Login..." -ForegroundColor Yellow
$r = Test-Api -name "Admin Login" -url "$baseUrl/auth/login" -method Post -body (@{phone='13800000001';password='123456'} | ConvertTo-Json) -expectSuccess $true
if ($r.Pass -and $r.Response.token) {
    $adminToken = $r.Response.token
    Write-Host "  PASS - Admin logged in, Type=$($r.Response.type), Roles=$($r.Response.roles -join ',')" -ForegroundColor Green
    $results += @{Name='Admin Login'; Pass=$true}
} else {
    Write-Host "  FAIL - $($r.Error)" -ForegroundColor Red
    $results += @{Name='Admin Login'; Pass=$false}
}

# Test 2: Regular User Login
Write-Host "`n[Test 2] Regular User Login..." -ForegroundColor Yellow
$r = Test-Api -name "User Login" -url "$baseUrl/auth/login" -method Post -body (@{phone='13900002001';password='123456'} | ConvertTo-Json) -expectSuccess $true
if ($r.Pass -and $r.Response.token) {
    $userToken = $r.Response.token
    Write-Host "  PASS - User logged in, Type=$($r.Response.type)" -ForegroundColor Green
    $results += @{Name='User Login'; Pass=$true}
} else {
    Write-Host "  FAIL - $($r.Error)" -ForegroundColor Red
    $results += @{Name='User Login'; Pass=$false}
}

# Test 3: Wrong Password
Write-Host "`n[Test 3] Wrong Password (should fail)..." -ForegroundColor Yellow
$r = Test-Api -name "Wrong Password" -url "$baseUrl/auth/login" -method Post -body (@{phone='13800000001';password='wrong'} | ConvertTo-Json) -expectSuccess $false
if ($r.Pass) {
    Write-Host "  PASS - Login correctly rejected" -ForegroundColor Green
    $results += @{Name='Wrong Password'; Pass=$true}
} else {
    Write-Host "  FAIL - $($r.Error)" -ForegroundColor Red
    $results += @{Name='Wrong Password'; Pass=$false}
}

# Test 4: Get Profile
Write-Host "`n[Test 4] Get User Profile..." -ForegroundColor Yellow
if ($userToken) {
    $headers = @{Authorization="Bearer $userToken"}
    $r = Test-Api -name "Get Profile" -url "$baseUrl/user/profile" -method Get -headers $headers -expectSuccess $true
    if ($r.Pass) {
        Write-Host "  PASS - Profile: ID=$($r.Response.id), Name=$($r.Response.name), Type=$($r.Response.type)" -ForegroundColor Green
        $results += @{Name='Get Profile'; Pass=$true}
    } else {
        Write-Host "  FAIL - $($r.Error)" -ForegroundColor Red
        $results += @{Name='Get Profile'; Pass=$false}
    }
} else {
    Write-Host "  SKIP - No user token" -ForegroundColor Yellow
    $results += @{Name='Get Profile'; Pass=$false}
}

# Test 5: Update Profile
Write-Host "`n[Test 5] Update Profile..." -ForegroundColor Yellow
if ($userToken) {
    $headers = @{Authorization="Bearer $userToken"}
    $body = (@{name='TestName';gender='1'} | ConvertTo-Json)
    $r = Test-Api -name "Update Profile" -url "$baseUrl/user/profile" -method Put -body $body -headers $headers -expectSuccess $true
    if ($r.Pass -and $r.Response.success) {
        Write-Host "  PASS - $($r.Response.message)" -ForegroundColor Green
        $results += @{Name='Update Profile'; Pass=$true}
    } else {
        Write-Host "  FAIL - $($r.Error)" -ForegroundColor Red
        $results += @{Name='Update Profile'; Pass=$false}
    }
} else {
    Write-Host "  SKIP - No user token" -ForegroundColor Yellow
    $results += @{Name='Update Profile'; Pass=$false}
}

# Test 6: Change Password
Write-Host "`n[Test 6] Change Password..." -ForegroundColor Yellow
if ($userToken) {
    $headers = @{Authorization="Bearer $userToken"}
    $body = (@{oldPassword='123456';newPassword='new123456'} | ConvertTo-Json)
    $r = Test-Api -name "Change Password" -url "$baseUrl/user/profile/password" -method Put -body $body -headers $headers -expectSuccess $true
    if ($r.Pass -and $r.Response.success) {
        Write-Host "  PASS - $($r.Response.message)" -ForegroundColor Green
        # Restore password
        $body = (@{oldPassword='new123456';newPassword='123456'} | ConvertTo-Json)
        Test-Api -name "Restore Password" -url "$baseUrl/user/profile/password" -method Put -body $body -headers $headers -expectSuccess $true | Out-Null
        Write-Host "  Password restored" -ForegroundColor Green
        $results += @{Name='Change Password'; Pass=$true}
    } else {
        Write-Host "  FAIL - $($r.Error)" -ForegroundColor Red
        $results += @{Name='Change Password'; Pass=$false}
    }
} else {
    Write-Host "  SKIP - No user token" -ForegroundColor Yellow
    $results += @{Name='Change Password'; Pass=$false}
}

# Test 7: Get Theme
Write-Host "`n[Test 7] Get Theme Preference..." -ForegroundColor Yellow
if ($userToken) {
    $headers = @{Authorization="Bearer $userToken"}
    $r = Test-Api -name "Get Theme" -url "$baseUrl/user/theme" -method Get -headers $headers -expectSuccess $true
    if ($r.Pass) {
        Write-Host "  PASS - Theme: Mode=$($r.Response.data.mode), Color=$($r.Response.data.color)" -ForegroundColor Green
        $results += @{Name='Get Theme'; Pass=$true}
    } else {
        Write-Host "  FAIL - $($r.Error)" -ForegroundColor Red
        $results += @{Name='Get Theme'; Pass=$false}
    }
} else {
    Write-Host "  SKIP - No user token" -ForegroundColor Yellow
    $results += @{Name='Get Theme'; Pass=$false}
}

# Test 8: Update Theme
Write-Host "`n[Test 8] Update Theme Preference..." -ForegroundColor Yellow
if ($userToken) {
    $headers = @{Authorization="Bearer $userToken"}
    $body = (@{mode='dark';color='blue'} | ConvertTo-Json)
    $r = Test-Api -name "Update Theme" -url "$baseUrl/user/theme" -method Put -body $body -headers $headers -expectSuccess $true
    if ($r.Pass -and $r.Response.success) {
        Write-Host "  PASS - $($r.Response.message), New: Mode=$($r.Response.data.mode), Color=$($r.Response.data.color)" -ForegroundColor Green
        # Restore default
        $body = (@{mode='system';color='teal'} | ConvertTo-Json)
        Test-Api -name "Restore Theme" -url "$baseUrl/user/theme" -method Put -body $body -headers $headers -expectSuccess $true | Out-Null
        $results += @{Name='Update Theme'; Pass=$true}
    } else {
        Write-Host "  FAIL - $($r.Error)" -ForegroundColor Red
        $results += @{Name='Update Theme'; Pass=$false}
    }
} else {
    Write-Host "  SKIP - No user token" -ForegroundColor Yellow
    $results += @{Name='Update Theme'; Pass=$false}
}

# Test 9: Admin - Get Users Page
Write-Host "`n[Test 9] Admin - Get Users Page..." -ForegroundColor Yellow
if ($adminToken) {
    $headers = @{Authorization="Bearer $adminToken"}
    $r = Test-Api -name "Get Users" -url "$baseUrl/admin/users/page?page=1&size=10" -method Get -headers $headers -expectSuccess $true
    if ($r.Pass) {
        Write-Host "  PASS - Total=$($r.Response.total), Current=$($r.Response.current), Size=$($r.Response.size)" -ForegroundColor Green
        Write-Host "  Users count: $($r.Response.records.Count)" -ForegroundColor Green
        $results += @{Name='Get Users Page'; Pass=$true}
    } else {
        Write-Host "  FAIL - $($r.Error)" -ForegroundColor Red
        $results += @{Name='Get Users Page'; Pass=$false}
    }
} else {
    Write-Host "  SKIP - No admin token" -ForegroundColor Yellow
    $results += @{Name='Get Users Page'; Pass=$false}
}

# Test 10: Admin - Create User
Write-Host "`n[Test 10] Admin - Create User..." -ForegroundColor Yellow
if ($adminToken) {
    $headers = @{Authorization="Bearer $adminToken"}
    $newPhone = "136$(Get-Random -Minimum 1000000 -Maximum 9999999)"
    $body = (@{phone=$newPhone;password='123456';name="TestUser$newPhone";type='resident';gender='1';status=1} | ConvertTo-Json)
    $r = Test-Api -name "Create User" -url "$baseUrl/admin/users" -method Post -body $body -headers $headers -expectSuccess $true
    if ($r.Pass -and $r.Response.id) {
        $testUserId = $r.Response.id
        Write-Host "  PASS - Created user ID=$testUserId, Name=$($r.Response.name)" -ForegroundColor Green
        $results += @{Name='Create User'; Pass=$true}
        
        # Test 11: Update User
        Write-Host "`n[Test 11] Admin - Update User..." -ForegroundColor Yellow
        $body = (@{phone=$newPhone;password='123456';name='UpdatedName';type='resident';gender='0';status=1} | ConvertTo-Json)
        $r = Test-Api -name "Update User" -url "$baseUrl/admin/users/$testUserId" -method Put -body $body -headers $headers -expectSuccess $true
        if ($r.Pass -and $r.Response.id) {
            Write-Host "  PASS - Updated user ID=$($r.Response.id), Name=$($r.Response.name)" -ForegroundColor Green
            $results += @{Name='Update User'; Pass=$true}
            
            # Test 12: Delete User
            Write-Host "`n[Test 12] Admin - Delete User..." -ForegroundColor Yellow
            $r = Test-Api -name "Delete User" -url "$baseUrl/admin/users/$testUserId" -method Delete -headers $headers -expectSuccess $true
            if ($r.Pass) {
                Write-Host "  PASS - Deleted user ID=$testUserId" -ForegroundColor Green
                $results += @{Name='Delete User'; Pass=$true}
            } else {
                Write-Host "  FAIL - $($r.Error)" -ForegroundColor Red
                $results += @{Name='Delete User'; Pass=$false}
            }
        } else {
            Write-Host "  FAIL - $($r.Error)" -ForegroundColor Red
            $results += @{Name='Update User'; Pass=$false}
        }
    } else {
        Write-Host "  FAIL - $($r.Error)" -ForegroundColor Red
        $results += @{Name='Create User'; Pass=$false}
    }
} else {
    Write-Host "  SKIP - No admin token" -ForegroundColor Yellow
    $results += @{Name='Create User'; Pass=$false}
}

# Test 13: Get Roles
Write-Host "`n[Test 13] Admin - Get Roles..." -ForegroundColor Yellow
if ($adminToken) {
    $headers = @{Authorization="Bearer $adminToken"}
    $r = Test-Api -name "Get Roles" -url "$baseUrl/role" -method Get -headers $headers -expectSuccess $true
    if ($r.Pass) {
        Write-Host "  PASS - Roles count=$($r.Count)" -ForegroundColor Green
        foreach ($role in $r) {
            Write-Host "    - ID=$($role.id), Name=$($role.name), Code=$($role.code)" -ForegroundColor Green
        }
        $results += @{Name='Get Roles'; Pass=$true}
    } else {
        Write-Host "  FAIL - $($r.Error)" -ForegroundColor Red
        $results += @{Name='Get Roles'; Pass=$false}
    }
} else {
    Write-Host "  SKIP - No admin token" -ForegroundColor Yellow
    $results += @{Name='Get Roles'; Pass=$false}
}

# Test 14: Non-admin access to admin endpoint (should fail)
Write-Host "`n[Test 14] Permission Test - Non-admin access (should fail)..." -ForegroundColor Yellow
if ($userToken) {
    $headers = @{Authorization="Bearer $userToken"}
    $r = Test-Api -name "Permission Test" -url "$baseUrl/admin/users/page?page=1&size=10" -method Get -headers $headers -expectSuccess $false
    if ($r.Pass) {
        Write-Host "  PASS - Non-admin correctly denied access" -ForegroundColor Green
        $results += @{Name='Permission Test'; Pass=$true}
    } else {
        Write-Host "  FAIL - $($r.Error)" -ForegroundColor Red
        $results += @{Name='Permission Test'; Pass=$false}
    }
} else {
    Write-Host "  SKIP - No user token" -ForegroundColor Yellow
    $results += @{Name='Permission Test'; Pass=$false}
}

# Test 15: Register new user
Write-Host "`n[Test 15] Register New User..." -ForegroundColor Yellow
$newPhone = "137$(Get-Random -Minimum 1000000 -Maximum 9999999)"
$body = (@{phone=$newPhone;password='123456';name="RegUser$newPhone";type='resident';gender='1'} | ConvertTo-Json)
$r = Test-Api -name "Register" -url "$baseUrl/auth/register" -method Post -body $body -expectSuccess $true
if ($r.Pass) {
    Write-Host "  PASS - Registered user phone=$newPhone" -ForegroundColor Green
    # Verify login
    $loginBody = (@{phone=$newPhone;password='123456'} | ConvertTo-Json)
    $loginR = Test-Api -name "Login After Register" -url "$baseUrl/auth/login" -method Post -body $loginBody -expectSuccess $true
    if ($loginR.Pass) {
        Write-Host "  PASS - New user can login" -ForegroundColor Green
        $results += @{Name='Register'; Pass=$true}
    } else {
        Write-Host "  FAIL - New user cannot login" -ForegroundColor Red
        $results += @{Name='Register'; Pass=$false}
    }
} else {
    Write-Host "  FAIL - $($r.Error)" -ForegroundColor Red
    $results += @{Name='Register'; Pass=$false}
}

# Test 16: Duplicate register (should fail)
Write-Host "`n[Test 16] Register Duplicate (should fail)..." -ForegroundColor Yellow
$body = (@{phone='13800000001';password='123456';name='DupUser';type='admin'} | ConvertTo-Json)
$r = Test-Api -name "Dup Register" -url "$baseUrl/auth/register" -method Post -body $body -expectSuccess $false
if ($r.Pass) {
    Write-Host "  PASS - Duplicate registration correctly rejected" -ForegroundColor Green
    $results += @{Name='Dup Register'; Pass=$true}
} else {
    Write-Host "  FAIL - $($r.Error)" -ForegroundColor Red
    $results += @{Name='Dup Register'; Pass=$false}
}

# Summary
Write-Host "`n=== TEST SUMMARY ===" -ForegroundColor Cyan
$passed = ($results | Where-Object { $_.Pass }).Count
$total = $results.Count
$rate = [Math]::Round(($passed / $total) * 100, 2)

Write-Host "Total: $total | Passed: $passed | Failed: $($total - $passed) | Rate: $rate%" -ForegroundColor Cyan
Write-Host ""

foreach ($r in $results) {
    $icon = if ($r.Pass) { "[PASS]" } else { "[FAIL]" }
    $color = if ($r.Pass) { "Green" } else { "Red" }
    Write-Host "  $icon $($r.Name)" -ForegroundColor $color
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan
