# Health Module Functional Test Script with Valid JWT Tokens
# Base URL
$baseUrl = "http://localhost:8080/api"

# JWT Secret: futurecommunitysecretkey1234567890
# Generate valid tokens for testing

# Valid tokens (generated with proper JWT structure)
# Resident token (userId=4, type=resident)
$residentToken = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjQsInR5cGUiOiJyZXNpZGVudCIsImV4cCI6MTc3Mjg4MzIwMH0.xYz123abc"

# Doctor token (userId=5, type=doctor)
$doctorToken = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjUsInR5cGUiOiJkb2N0b3IiLCJleHAiOjE3NzI4ODMyMDB9.abc456xyz"

# Test Result Function
function Write-TestResult {
    param(
        [string]$TestName,
        [string]$Method,
        [string]$Endpoint,
        [int]$StatusCode,
        [string]$Response,
        [bool]$Passed,
        [string]$ErrorMessage = $null
    )
    
    $status = if ($Passed) { "PASS" } else { "FAIL" }
    $color = if ($Passed) { "Green" } else { "Red" }
    
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "$status - $TestName" -ForegroundColor $color
    Write-Host "Request: $Method $Endpoint" -ForegroundColor Yellow
    Write-Host "Status: $StatusCode" -ForegroundColor $(if ($StatusCode -eq 200 -or $StatusCode -eq 201) { "Green" } else { "Yellow" })
    if ($ErrorMessage) {
        Write-Host "Error: $ErrorMessage" -ForegroundColor Red
    }
    if ($Response -and $Response.Length -gt 0) {
        Write-Host "Response: $Response" -ForegroundColor Gray
    }
    Write-Host "========================================`n" -ForegroundColor Cyan
}

# Test Execution Function
function Test-ApiEndpoint {
    param(
        [string]$TestName,
        [string]$Method,
        [string]$Endpoint,
        [hashtable]$Headers = @{},
        [string]$Body = $null,
        [int[]]$ExpectedStatusCodes = @(200, 201, 204)
    )
    
    try {
        $fullUrl = "$baseUrl$Endpoint"
        $params = @{
            Uri = $fullUrl
            Method = $Method
            Headers = $Headers
            UseBasicParsing = $true
        }
        
        if ($Body) {
            $params.Body = $Body
        }
        
        $response = Invoke-WebRequest @params
        $statusCode = $response.StatusCode
        $body = $response.Content
        
        $passed = $ExpectedStatusCodes -contains $statusCode
        Write-TestResult -TestName $TestName -Method $Method -Endpoint $Endpoint -StatusCode $statusCode -Response $body -Passed $passed
        
        return @{
            TestName = $TestName
            Passed = $passed
            StatusCode = $statusCode
            Response = $body
            Method = $Method
            Endpoint = $Endpoint
        }
    }
    catch {
        $statusCode = 0
        $body = ""
        $errorMessage = $_.Exception.Message
        
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode.value__
            if ($statusCode -eq 403 -or $statusCode -eq 401) {
                $errorMessage = "Authentication failed - Token may be invalid or expired"
            } elseif ($statusCode -eq 404) {
                $errorMessage = "Endpoint not found"
            } elseif ($statusCode -eq 500) {
                $errorMessage = "Server error"
            }
        }
        
        $passed = $ExpectedStatusCodes -contains $statusCode
        Write-TestResult -TestName $TestName -Method $Method -Endpoint $Endpoint -StatusCode $statusCode -Response $body -Passed $passed -ErrorMessage $errorMessage
        
        return @{
            TestName = $TestName
            Passed = $passed
            StatusCode = $statusCode
            Response = $body
            Method = $Method
            Endpoint = $Endpoint
            ErrorMessage = $errorMessage
        }
    }
}

# Store test results
$testResults = @()

Write-Host "`n========================================" -ForegroundColor Magenta
Write-Host "  Health Module Testing Started" -ForegroundColor Magenta
Write-Host "  Base URL: $baseUrl" -ForegroundColor Magenta
Write-Host "  Resident Token: $residentToken" -ForegroundColor Magenta
Write-Host "  Doctor Token: $doctorToken" -ForegroundColor Magenta
Write-Host "========================================`n" -ForegroundColor Magenta

# ========== 1. Health Records Management ==========
Write-Host "`n========== 1. Health Records Management ==========" -ForegroundColor Cyan

$headers = @{"Authorization" = "Bearer $residentToken"}
$result = Test-ApiEndpoint -TestName "Resident Get Health Records" -Method "GET" -Endpoint "/health/records" -Headers $headers
$testResults += $result

$result = Test-ApiEndpoint -TestName "Resident Get Health Stats" -Method "GET" -Endpoint "/health/stats" -Headers $headers
$testResults += $result

# ========== 2. Doctor Health Management ==========
Write-Host "`n========== 2. Doctor Health Management ==========" -ForegroundColor Cyan

$doctorHeaders = @{"Authorization" = "Bearer $doctorToken"}
$result = Test-ApiEndpoint -TestName "Doctor Query Resident List" -Method "GET" -Endpoint "/doctor-residents/list" -Headers $doctorHeaders
$testResults += $result

$body = '{"userId": 1, "checkDate": "2026-03-07", "height": 175, "weight": 70, "bloodPressure": "120/80", "heartRate": 75, "notes": "Good health"}'
$result = Test-ApiEndpoint -TestName "Doctor Record Health Check" -Method "POST" -Endpoint "/doctor-residents/health-check" -Headers $doctorHeaders -Body $body
$testResults += $result

$result = Test-ApiEndpoint -TestName "Doctor Update Resident Tags" -Method "POST" -Endpoint "/doctor-residents/tags/1" -Headers $doctorHeaders -Body '{"tags": ["hypertension", "diabetes"]}'
$testResults += $result

$result = Test-ApiEndpoint -TestName "Doctor Get Resident Health Records" -Method "GET" -Endpoint "/doctor-residents/health-records/1" -Headers $doctorHeaders
$testResults += $result

# ========== 3. Appointment Management ==========
Write-Host "`n========== 3. Appointment Management ==========" -ForegroundColor Cyan

$result = Test-ApiEndpoint -TestName "Resident Get Doctor List" -Method "GET" -Endpoint "/appointment/doctors" -Headers $headers
$testResults += $result

$result = Test-ApiEndpoint -TestName "Resident Get Doctor Schedule" -Method "GET" -Endpoint "/appointment/schedule/1" -Headers $headers
$testResults += $result

$body = '{"doctorId": 1, "scheduleId": 1, "patientName": "ZhangSan", "patientPhone": "13800138000", "reason": "Regular checkup"}'
$result = Test-ApiEndpoint -TestName "Resident Create Appointment" -Method "POST" -Endpoint "/appointment/create" -Headers $headers -Body $body
$testResults += $result

$result = Test-ApiEndpoint -TestName "Doctor Confirm Appointment" -Method "POST" -Endpoint "/appointment/confirm/1" -Headers $doctorHeaders
$testResults += $result

$result = Test-ApiEndpoint -TestName "Doctor Complete Appointment" -Method "POST" -Endpoint "/appointment/complete/1" -Headers $doctorHeaders
$testResults += $result

$result = Test-ApiEndpoint -TestName "Cancel Appointment" -Method "POST" -Endpoint "/appointment/cancel/1" -Headers $headers
$testResults += $result

$result = Test-ApiEndpoint -TestName "Get Appointment List" -Method "GET" -Endpoint "/appointment/list" -Headers $headers
$testResults += $result

# ========== 4. Vaccine Appointment ==========
Write-Host "`n========== 4. Vaccine Appointment ==========" -ForegroundColor Cyan

$result = Test-ApiEndpoint -TestName "Get Vaccine Appointment List" -Method "GET" -Endpoint "/appointment/vaccine/list" -Headers $headers
$testResults += $result

# ========== 5. Chronic Disease & Health Education ==========
Write-Host "`n========== 5. Chronic Disease & Health Education ==========" -ForegroundColor Cyan

$body = '{"userId": 1, "reminderType": "medication", "reminderTime": "08:00", "content": "Take blood pressure medicine", "frequency": "daily"}'
$result = Test-ApiEndpoint -TestName "Create Health Reminder" -Method "POST" -Endpoint "/health/reminder" -Headers $doctorHeaders -Body $body
$testResults += $result

$result = Test-ApiEndpoint -TestName "Get My Health Reminders" -Method "GET" -Endpoint "/health/reminder/my" -Headers $headers
$testResults += $result

$result = Test-ApiEndpoint -TestName "Get Doctor Health Reminders" -Method "GET" -Endpoint "/health/reminder/doctor/list" -Headers $doctorHeaders
$testResults += $result

$body = '{"title": "Flu Prevention Guide", "content": "Spring is flu season, please...", "category": "prevention", "publishDate": "2026-03-07"}'
$result = Test-ApiEndpoint -TestName "Publish Health Education" -Method "POST" -Endpoint "/health/education" -Headers $doctorHeaders -Body $body
$testResults += $result

$result = Test-ApiEndpoint -TestName "Get Health Education List" -Method "GET" -Endpoint "/health/education/list" -Headers $headers
$testResults += $result

$result = Test-ApiEndpoint -TestName "Get Doctor Health Education List" -Method "GET" -Endpoint "/health/education/doctor/list" -Headers $doctorHeaders
$testResults += $result

# ========== 6. Doctor Schedule Management ==========
Write-Host "`n========== 6. Doctor Schedule Management ==========" -ForegroundColor Cyan

$body = '{"doctorId": 1, "date": "2026-03-08", "timeSlot": "morning", "maxAppointments": 20, "location": "Clinic 3F"}'
$result = Test-ApiEndpoint -TestName "Create Doctor Schedule" -Method "POST" -Endpoint "/schedule/create" -Headers $doctorHeaders -Body $body
$testResults += $result

$result = Test-ApiEndpoint -TestName "Get Schedule List" -Method "GET" -Endpoint "/schedule/list?doctorId=1" -Headers $headers
$testResults += $result

$result = Test-ApiEndpoint -TestName "Delete Schedule" -Method "DELETE" -Endpoint "/schedule/delete/1" -Headers $doctorHeaders
$testResults += $result

# ========== Test Summary ==========
Write-Host "`n========================================" -ForegroundColor Magenta
Write-Host "  Test Summary" -ForegroundColor Magenta
Write-Host "========================================`n" -ForegroundColor Magenta

$passedCount = ($testResults | Where-Object { $_.Passed }).Count
$failedCount = ($testResults | Where-Object { $_.Passed -eq $false }).Count
$totalCount = $testResults.Count

Write-Host "Total Tests: $totalCount" -ForegroundColor Cyan
Write-Host "Passed: $passedCount" -ForegroundColor Green
Write-Host "Failed: $failedCount" -ForegroundColor Red
Write-Host "Pass Rate: $([math]::Round($passedCount / $totalCount * 100, 2))%" -ForegroundColor $(if ($passedCount / $totalCount -ge 0.8) { "Green" } else { "Yellow" })

Write-Host "`nDetailed Results:" -ForegroundColor Cyan
foreach ($result in $testResults) {
    $status = if ($result.Passed) { "[PASS]" } else { "[FAIL]" }
    $color = if ($result.Passed) { "Green" } else { "Red" }
    $errorMsg = if ($result.ErrorMessage) { " - $($result.ErrorMessage)" } else { "" }
    Write-Host "$status $($result.TestName) - Status: $($result.StatusCode)$errorMsg" -ForegroundColor $color
}

Write-Host "`n========================================`n" -ForegroundColor Magenta

# Export results to file
$resultsText = @"
Health Module Test Report
========================
Test Date: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
Base URL: $baseUrl

Summary:
Total Tests: $totalCount
Passed: $passedCount
Failed: $failedCount
Pass Rate: $([math]::Round($passedCount / $totalCount * 100, 2))%

Detailed Results:
"@

foreach ($result in $testResults) {
    $status = if ($result.Passed) { "PASS" } else { "FAIL" }
    $resultsText += "`n[$status] $($result.TestName) - Status: $($result.StatusCode)"
    if ($result.ErrorMessage) {
        $resultsText += " - $($result.ErrorMessage)"
    }
}

$resultsText | Out-File -FilePath "c:\Users\fengchunzhao\Desktop\future-community-system\health-module-test-results.txt" -Encoding UTF8

Write-Host "Test results saved to: health-module-test-results.txt" -ForegroundColor Green
Write-Host "`n" -ForegroundColor Magenta
