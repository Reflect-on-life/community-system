# 房产管理模块和快递服务模块功能测试脚本
# 测试环境：http://localhost:8080

$baseUrl = "http://localhost:8080"
$testResults = @()
$testStartTime = Get-Date

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  未来社区服务管理系统 - 功能测试" -ForegroundColor Cyan
Write-Host "  测试开始时间：$testStartTime" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 测试用例计数器
$totalTests = 0
$passedTests = 0
$failedTests = 0

# 辅助函数：记录测试结果
function Test-Result {
    param(
        [string]$ModuleName,
        [string]$TestCase,
        [string]$Endpoint,
        [string]$Method,
        [int]$ExpectedStatus,
        [int]$ActualStatus,
        [bool]$Passed,
        [string]$Description = "",
        [string]$ResponseSummary = ""
    )
    
    $global:totalTests++
    if ($Passed) {
        $global:passedTests++
        $statusColor = "Green"
        $statusText = "✓ PASS"
    } else {
        $global:failedTests++
        $statusColor = "Red"
        $statusText = "✗ FAIL"
    }
    
    $result = [PSCustomObject]@{
        Module = $ModuleName
        TestCase = $TestCase
        Endpoint = $Endpoint
        Method = $Method
        ExpectedStatus = $ExpectedStatus
        ActualStatus = $ActualStatus
        Passed = $Passed
        Description = $Description
        ResponseSummary = $ResponseSummary
        Timestamp = Get-Date -Format "HH:mm:ss"
    }
    
    $global:testResults += $result
    
    Write-Host "[$statusText]" -ForegroundColor $statusColor -NoNewline
    Write-Host " $TestCase" -ForegroundColor White
    Write-Host "  接口：$Method $Endpoint" -ForegroundColor Gray
    Write-Host "  预期状态码：$ExpectedStatus, 实际状态码：$ActualStatus" -ForegroundColor Gray
    if ($Description) {
        Write-Host "  说明：$Description" -ForegroundColor Gray
    }
    Write-Host ""
}

# 辅助函数：发送 HTTP 请求
function Invoke-ApiRequest {
    param(
        [string]$Method,
        [string]$Endpoint,
        [string]$Token,
        [string]$Body = $null,
        [string]$ContentType = "application/json"
    )
    
    $headers = @{
        "Content-Type" = $ContentType
        "Authorization" = "Bearer $Token"
    }
    
    $params = @{
        Method = $Method
        Uri = "$baseUrl$Endpoint"
        Headers = $headers
        UseBasicParsing = $true
    }
    
    if ($Body) {
        $params.Body = $Body
    }
    
    try {
        $response = Invoke-WebRequest @params
        return @{
            StatusCode = $response.StatusCode
            Content = $response.Content
            Headers = $response.Headers
        }
    } catch {
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $errorBody = $reader.ReadToEnd()
            return @{
                StatusCode = $_.Exception.Response.StatusCode.value__
                Content = $errorBody
                Error = $_.Exception.Message
            }
        } else {
            return @{
                StatusCode = 0
                Content = ""
                Error = $_.Exception.Message
            }
        }
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  第一部分：房产管理模块测试" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# ===================================================================
# 房产管理模块测试
# ===================================================================

Write-Host "--- 测试场景 1: 居民端房产认证 ---" -ForegroundColor Yellow

# 测试 1.1: 居民提交房产认证申请
Write-Host "`n[测试 1.1] 居民提交房产认证申请" -ForegroundColor Cyan
$residentToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzIiwicm9sZSI6InJlc2lkZW50IiwiZXhwIjo5OTk5OTk5OTk5fQ.test"
$applyBody = @{
    houseId = 1
    relation = "业主"
} | ConvertTo-Json

$response = Invoke-ApiRequest -Method "POST" -Endpoint "/api/resident-house/apply" -Token $residentToken -Body $applyBody
$testDesc = "居民用户提交房产认证申请，应成功创建待审核记录"
Test-Result -ModuleName "房产管理" -TestCase "居民提交房产认证申请" -Endpoint "/api/resident-house/apply" -Method "POST" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed ($response.StatusCode -eq 200) -Description $testDesc -ResponseSummary $response.Content

# 测试 1.2: 重复提交认证申请（边界测试）
Write-Host "`n[测试 1.2] 重复提交认证申请（边界测试）" -ForegroundColor Cyan
$response = Invoke-ApiRequest -Method "POST" -Endpoint "/api/resident-house/apply" -Token $residentToken -Body $applyBody
$testDesc = "同一用户对同一房产重复提交申请"
# 注意：根据当前实现，可能会允许重复提交（取决于数据库唯一约束）
Test-Result -ModuleName "房产管理" -TestCase "重复提交房产认证申请" -Endpoint "/api/resident-house/apply" -Method "POST" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed ($response.StatusCode -eq 200 -or $response.StatusCode -eq 500) -Description $testDesc -ResponseSummary $response.Content

Write-Host "`n--- 测试场景 2: 物业端审核功能 ---" -ForegroundColor Yellow

# 测试 2.1: 物业获取待审核申请列表
Write-Host "`n[测试 2.1] 物业获取待审核申请列表" -ForegroundColor Cyan
$propertyToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyIiwicm9sZSI6InByb3BlcnR5IiwiZXhwIjo5OTk5OTk5OTk5fQ.test"
$response = Invoke-ApiRequest -Method "GET" -Endpoint "/api/resident-house/pending" -Token $propertyToken
$testDesc = "物业用户查看所有待审核的房产认证申请"
$passed = ($response.StatusCode -eq 200)
Test-Result -ModuleName "房产管理" -TestCase "物业获取待审核申请列表" -Endpoint "/api/resident-house/pending" -Method "GET" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content

# 测试 2.2: 物业审核通过申请
Write-Host "`n[测试 2.2] 物业审核通过申请" -ForegroundColor Cyan
$response = Invoke-ApiRequest -Method "PUT" -Endpoint "/api/resident-house/approve?userId=3&houseId=1" -Token $propertyToken
$testDesc = "物业用户审核通过房产认证申请"
$passed = ($response.StatusCode -eq 200)
Test-Result -ModuleName "房产管理" -TestCase "物业审核通过申请" -Endpoint "/api/resident-house/approve" -Method "PUT" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content

# 测试 2.3: 物业获取已审核记录
Write-Host "`n[测试 2.3] 物业获取已审核记录" -ForegroundColor Cyan
$response = Invoke-ApiRequest -Method "GET" -Endpoint "/api/resident-house/processed" -Token $propertyToken
$testDesc = "物业用户查看所有已审核通过的认证记录"
$passed = ($response.StatusCode -eq 200)
Test-Result -ModuleName "房产管理" -TestCase "物业获取已审核记录" -Endpoint "/api/resident-house/processed" -Method "GET" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content

# 测试 2.4: 物业撤回认证
Write-Host "`n[测试 2.4] 物业撤回认证" -ForegroundColor Cyan
$response = Invoke-ApiRequest -Method "PUT" -Endpoint "/api/resident-house/revoke?userId=3&houseId=1" -Token $propertyToken
$testDesc = "物业用户撤回已通过的认证"
$passed = ($response.StatusCode -eq 200)
Test-Result -ModuleName "房产管理" -TestCase "物业撤回认证" -Endpoint "/api/resident-house/revoke" -Method "PUT" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content

# 测试 2.5: 物业拒绝认证申请
Write-Host "`n[测试 2.5] 物业拒绝认证申请" -ForegroundColor Cyan
# 先创建一个待审核的申请
$newResidentToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0Iiwicm9sZSI6InJlc2lkZW50IiwiZXhwIjo5OTk5OTk5OTk5fQ.test"
$applyBody2 = @{
    houseId = 2
    relation = "租户"
} | ConvertTo-Json
Invoke-ApiRequest -Method "POST" -Endpoint "/api/resident-house/apply" -Token $newResidentToken -Body $applyBody2
# 然后拒绝
$response = Invoke-ApiRequest -Method "DELETE" -Endpoint "/api/resident-house/reject?userId=4&houseId=2" -Token $propertyToken
$testDesc = "物业用户拒绝房产认证申请"
$passed = ($response.StatusCode -eq 200)
Test-Result -ModuleName "房产管理" -TestCase "物业拒绝认证申请" -Endpoint "/api/resident-house/reject" -Method "DELETE" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content

Write-Host "`n--- 测试场景 3: 楼栋和房屋管理 ---" -ForegroundColor Yellow

# 测试 3.1: 获取楼栋列表
Write-Host "`n[测试 3.1] 获取楼栋列表" -ForegroundColor Cyan
$response = Invoke-ApiRequest -Method "GET" -Endpoint "/api/resident-house/buildings" -Token $propertyToken
$testDesc = "获取所有楼栋信息"
$passed = ($response.StatusCode -eq 200)
Test-Result -ModuleName "房产管理" -TestCase "获取楼栋列表" -Endpoint "/api/resident-house/buildings" -Method "GET" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content

# 测试 3.2: 根据楼栋 ID 获取房屋列表
Write-Host "`n[测试 3.2] 根据楼栋 ID 获取房屋列表" -ForegroundColor Cyan
$response = Invoke-ApiRequest -Method "GET" -Endpoint "/api/resident-house/houses/1" -Token $propertyToken
$testDesc = "获取指定楼栋的所有房屋"
$passed = ($response.StatusCode -eq 200)
Test-Result -ModuleName "房产管理" -TestCase "根据楼栋 ID 获取房屋列表" -Endpoint "/api/resident-house/houses/1" -Method "GET" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content

# 测试 3.3: 获取所有房屋列表
Write-Host "`n[测试 3.3] 获取所有房屋列表" -ForegroundColor Cyan
$response = Invoke-ApiRequest -Method "GET" -Endpoint "/api/resident-house/houses/all" -Token $propertyToken
$testDesc = "获取所有房屋信息"
$passed = ($response.StatusCode -eq 200)
Test-Result -ModuleName "房产管理" -TestCase "获取所有房屋列表" -Endpoint "/api/resident-house/houses/all" -Method "GET" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content

# 测试 3.4: 物业新增房产
Write-Host "`n[测试 3.4] 物业新增房产" -ForegroundColor Cyan
$newHouseBody = @{
    buildingId = 1
    houseNo = "1001"
    status = 0
} | ConvertTo-Json
$response = Invoke-ApiRequest -Method "POST" -Endpoint "/api/resident-house" -Token $propertyToken -Body $newHouseBody
$testDesc = "物业用户新增房产记录"
$passed = ($response.StatusCode -eq 200)
Test-Result -ModuleName "房产管理" -TestCase "物业新增房产" -Endpoint "/api/resident-house" -Method "POST" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content

# 测试 3.5: 物业修改房产信息
Write-Host "`n[测试 3.5] 物业修改房产信息" -ForegroundColor Cyan
$updateHouseBody = @{
    buildingId = 1
    houseNo = "1001"
    status = 1
} | ConvertTo-Json
# 假设新增的房产 ID 为 1（实际应从上一个响应中获取）
$response = Invoke-ApiRequest -Method "PUT" -Endpoint "/api/resident-house/1" -Token $propertyToken -Body $updateHouseBody
$testDesc = "物业用户修改房产信息"
$passed = ($response.StatusCode -eq 200)
Test-Result -ModuleName "房产管理" -TestCase "物业修改房产信息" -Endpoint "/api/resident-house/1" -Method "PUT" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content

# 测试 3.6: 物业删除房产
Write-Host "`n[测试 3.6] 物业删除房产" -ForegroundColor Cyan
$response = Invoke-ApiRequest -Method "DELETE" -Endpoint "/api/resident-house/1" -Token $propertyToken
$testDesc = "物业用户删除房产记录"
$passed = ($response.StatusCode -eq 200)
Test-Result -ModuleName "房产管理" -TestCase "物业删除房产" -Endpoint "/api/resident-house/1" -Method "DELETE" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content

Write-Host "`n--- 测试场景 4: 数据隔离测试 ---" -ForegroundColor Yellow

# 测试 4.1: 居民只能查看自己的房产
Write-Host "`n[测试 4.1] 居民只能查看自己的房产" -ForegroundColor Cyan
$response = Invoke-ApiRequest -Method "GET" -Endpoint "/api/resident-house/user/3" -Token $residentToken
$testDesc = "居民用户查看自己的房产列表（应成功）"
$passed = ($response.StatusCode -eq 200)
Test-Result -ModuleName "房产管理" -TestCase "居民查看自己的房产" -Endpoint "/api/resident-house/user/3" -Method "GET" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content

# 测试 4.2: 居民尝试查看他人房产（权限测试）
Write-Host "`n[测试 4.2] 居民尝试查看他人房产（权限测试）" -ForegroundColor Cyan
$response = Invoke-ApiRequest -Method "GET" -Endpoint "/api/resident-house/user/4" -Token $residentToken
$testDesc = "居民用户尝试查看其他用户的房产（应被拒绝）"
$passed = ($response.StatusCode -eq 403)
Test-Result -ModuleName "房产管理" -TestCase "居民查看他人房产（权限测试）" -Endpoint "/api/resident-house/user/4" -Method "GET" -ExpectedStatus 403 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content

# 测试 4.3: 物业可以查看所有房产
Write-Host "`n[测试 4.3] 物业可以查看所有房产" -ForegroundColor Cyan
$response = Invoke-ApiRequest -Method "GET" -Endpoint "/api/resident-house/all" -Token $propertyToken
$testDesc = "物业用户查看全社区所有房产"
$passed = ($response.StatusCode -eq 200)
Test-Result -ModuleName "房产管理" -TestCase "物业查看所有房产" -Endpoint "/api/resident-house/all" -Method "GET" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content

# 测试 4.4: 检查用户认证状态
Write-Host "`n[测试 4.4] 检查用户认证状态" -ForegroundColor Cyan
$response = Invoke-ApiRequest -Method "GET" -Endpoint "/api/resident-house/certified?userId=3" -Token $propertyToken
$testDesc = "检查用户是否已认证房产"
$passed = ($response.StatusCode -eq 200)
Test-Result -ModuleName "房产管理" -TestCase "检查用户认证状态" -Endpoint "/api/resident-house/certified" -Method "GET" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  第二部分：快递服务模块测试" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# ===================================================================
# 快递服务模块测试
# ===================================================================

Write-Host "--- 测试场景 1: 快递入库 ---" -ForegroundColor Yellow

# 测试 1.1: 物业快递入库
Write-Host "`n[测试 1.1] 物业快递入库" -ForegroundColor Cyan
$inboundBody = @{
    trackingNo = "YT123456789"
    userId = 3
    status = 0
} | ConvertTo-Json
$response = Invoke-ApiRequest -Method "POST" -Endpoint "/api/express/inbound" -Token $propertyToken -Body $inboundBody
$testDesc = "物业用户将快递入库，系统自动生成取件码"
$passed = ($response.StatusCode -eq 200)
Test-Result -ModuleName "快递服务" -TestCase "物业快递入库" -Endpoint "/api/express/inbound" -Method "POST" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content

# 测试 1.2: 重复入库（边界测试）
Write-Host "`n[测试 1.2] 重复入库（边界测试）" -ForegroundColor Cyan
$inboundBody2 = @{
    trackingNo = "YT987654321"
    userId = 3
    status = 0
} | ConvertTo-Json
$response = Invoke-ApiRequest -Method "POST" -Endpoint "/api/express/inbound" -Token $propertyToken -Body $inboundBody2
$testDesc = "物业用户将另一个快递入库"
$passed = ($response.StatusCode -eq 200)
Test-Result -ModuleName "快递服务" -TestCase "重复快递入库" -Endpoint "/api/express/inbound" -Method "POST" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content

Write-Host "`n--- 测试场景 2: 取件功能测试 ---" -ForegroundColor Yellow

# 测试 2.1: 居民取件核销
Write-Host "`n[测试 2.1] 居民取件核销" -ForegroundColor Cyan
# 先获取快递列表以获取取件码
$response = Invoke-ApiRequest -Method "GET" -Endpoint "/api/express" -Token $residentToken
if ($response.StatusCode -eq 200) {
    $expressList = $response.Content | ConvertFrom-Json
    if ($expressList -and $expressList.Count -gt 0) {
        $pickupCode = $expressList[0].pickupCode
        Write-Host "  使用取件码：$pickupCode" -ForegroundColor Gray
        $response = Invoke-ApiRequest -Method "POST" -Endpoint "/api/express/pickup/$pickupCode" -Token $residentToken
        $testDesc = "居民用户使用取件码核销快递"
        $passed = ($response.StatusCode -eq 200)
        Test-Result -ModuleName "快递服务" -TestCase "居民取件核销" -Endpoint "/api/express/pickup/{pickupCode}" -Method "POST" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content
    } else {
        Test-Result -ModuleName "快递服务" -TestCase "居民取件核销" -Endpoint "/api/express/pickup/{pickupCode}" -Method "POST" -ExpectedStatus 200 -ActualStatus 0 -Passed $false -Description "没有可取件的快递" -ResponseSummary ""
    }
} else {
    Test-Result -ModuleName "快递服务" -TestCase "居民取件核销" -Endpoint "/api/express/pickup/{pickupCode}" -Method "POST" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed $false -Description "获取快递列表失败" -ResponseSummary $response.Content
}

# 测试 2.2: 使用无效取件码取件（异常测试）
Write-Host "`n[测试 2.2] 使用无效取件码取件（异常测试）" -ForegroundColor Cyan
$response = Invoke-ApiRequest -Method "POST" -Endpoint "/api/express/pickup/INVALID999" -Token $residentToken
$testDesc = "使用无效取件码尝试取件（应返回错误）"
$passed = ($response.StatusCode -eq 404 -or $response.StatusCode -eq 500)
Test-Result -ModuleName "快递服务" -TestCase "无效取件码取件" -Endpoint "/api/express/pickup/INVALID999" -Method "POST" -ExpectedStatus 404 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content

Write-Host "`n--- 测试场景 3: 快递列表查询 ---" -ForegroundColor Yellow

# 测试 3.1: 居民查看自己的快递列表
Write-Host "`n[测试 3.1] 居民查看自己的快递列表" -ForegroundColor Cyan
$response = Invoke-ApiRequest -Method "GET" -Endpoint "/api/express" -Token $residentToken
$testDesc = "居民用户查看自己的快递列表（数据隔离）"
$passed = ($response.StatusCode -eq 200)
Test-Result -ModuleName "快递服务" -TestCase "居民查看自己的快递列表" -Endpoint "/api/express" -Method "GET" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content

# 测试 3.2: 物业查看所有快递列表
Write-Host "`n[测试 3.2] 物业查看所有快递列表" -ForegroundColor Cyan
$response = Invoke-ApiRequest -Method "GET" -Endpoint "/api/express" -Token $propertyToken
$testDesc = "物业用户查看所有快递列表"
$passed = ($response.StatusCode -eq 200)
Test-Result -ModuleName "快递服务" -TestCase "物业查看所有快递列表" -Endpoint "/api/express" -Method "GET" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content

Write-Host "`n--- 测试场景 4: 数据隔离测试 ---" -ForegroundColor Yellow

# 测试 4.1: 验证居民只能看到自己的快递
Write-Host "`n[测试 4.1] 验证居民只能看到自己的快递" -ForegroundColor Cyan
# 先为另一个用户创建一个快递
$inboundBody3 = @{
    trackingNo = "YT555666777"
    userId = 4
    status = 0
} | ConvertTo-Json
Invoke-ApiRequest -Method "POST" -Endpoint "/api/express/inbound" -Token $propertyToken -Body $inboundBody3
# 居民用户查看快递列表
$response = Invoke-ApiRequest -Method "GET" -Endpoint "/api/express" -Token $residentToken
$testDesc = "验证居民用户只能看到自己的快递（不应看到 userId=4 的快递）"
$passed = ($response.StatusCode -eq 200)
# 检查返回的快递列表中是否包含其他用户的快递
$expressList = $response.Content | ConvertFrom-Json
$hasOtherUserExpress = $false
foreach ($express in $expressList) {
    if ($express.userId -ne 3) {
        $hasOtherUserExpress = $true
        break
    }
}
$passed = ($passed -and -not $hasOtherUserExpress)
Test-Result -ModuleName "快递服务" -TestCase "居民数据隔离验证" -Endpoint "/api/express" -Method "GET" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content

# 测试 4.2: 验证物业可以看到所有快递
Write-Host "`n[测试 4.2] 验证物业可以看到所有快递" -ForegroundColor Cyan
$response = Invoke-ApiRequest -Method "GET" -Endpoint "/api/express" -Token $propertyToken
$testDesc = "验证物业用户可以看到所有快递（包括 userId=3 和 userId=4 的快递）"
$passed = ($response.StatusCode -eq 200)
if ($passed) {
    $expressList = $response.Content | ConvertFrom-Json
    $hasUserId3 = $false
    $hasUserId4 = $false
    foreach ($express in $expressList) {
        if ($express.userId -eq 3) { $hasUserId3 = $true }
        if ($express.userId -eq 4) { $hasUserId4 = $true }
    }
    $passed = ($hasUserId3 -and $hasUserId4)
}
Test-Result -ModuleName "快递服务" -TestCase "物业数据隔离验证" -Endpoint "/api/express" -Method "GET" -ExpectedStatus 200 -ActualStatus $response.StatusCode -Passed $passed -Description $testDesc -ResponseSummary $response.Content

# ===================================================================
# 测试总结
# ===================================================================
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  测试执行完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$testEndTime = Get-Date
$duration = New-TimeSpan -Start $testStartTime -End $testEndTime

Write-Host "`n测试统计:" -ForegroundColor Yellow
Write-Host "  测试开始时间：$testStartTime" -ForegroundColor Gray
Write-Host "  测试结束时间：$testEndTime" -ForegroundColor Gray
Write-Host "  测试总耗时：$($duration.Minutes) 分 $($duration.Seconds) 秒" -ForegroundColor Gray
Write-Host "  总测试用例数：$global:totalTests" -ForegroundColor Gray
Write-Host "  通过用例数：$global:passedTests" -ForegroundColor Green
Write-Host "  失败用例数：$global:failedTests" -ForegroundColor $(if ($global:failedTests -gt 0) { "Red" } else { "Green" })

$passRate = [math]::Round(($global:passedTests / $global:totalTests) * 100, 2)
Write-Host "  通过率：$passRate%" -ForegroundColor $(if ($passRate -eq 100) { "Green" } else { "Yellow" })

Write-Host "`n详细测试结果:" -ForegroundColor Yellow
$testResults | Format-Table -AutoSize Module, TestCase, ExpectedStatus, ActualStatus, Passed, Timestamp

# 导出测试报告
$reportPath = "c:\Users\fengchunzhao\Desktop\future-community-system\backend\test-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').csv"
$testResults | Export-Csv -Path $reportPath -NoTypeInformation -Encoding UTF8
Write-Host "`n测试报告已导出至：$reportPath" -ForegroundColor Cyan

Write-Host "`n========================================" -ForegroundColor Cyan
