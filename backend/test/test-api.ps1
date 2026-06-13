# API 功能测试脚本 - 房产管理和快递服务模块
# 测试环境：http://localhost:8080

$ErrorActionPreference = "Continue"
$baseUrl = "http://localhost:8080"

Write-Host "========================================"
Write-Host "  未来社区服务管理系统 - 功能测试"
Write-Host "  测试时间：$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Write-Host "========================================"
Write-Host ""

# 测试结果统计
$totalTests = 0
$passedTests = 0
$failedTests = 0
$results = @()

# Token（基于 JWT）
$residentToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzIiwicm9sZSI6InJlc2lkZW50IiwiZXhwIjo5OTk5OTk5OTk5fQ.test"
$propertyToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyIiwicm9sZSI6InByb3BlcnR5IiwiZXhwIjo5OTk5OTk5OTk5fQ.test"

# 辅助函数：发送 HTTP 请求
function Send-Request {
    param($method, $endpoint, $token, $body = $null)
    
    $headers = @{
        "Content-Type" = "application/json"
        "Authorization" = "Bearer $token"
    }
    
    $url = "$baseUrl$endpoint"
    
    try {
        if ($body) {
            $response = Invoke-RestMethod -Method $method -Uri $url -Headers $headers -Body $body
        } else {
            $response = Invoke-RestMethod -Method $method -Uri $url -Headers $headers
        }
        return @{ StatusCode = 200; Content = $response }
    } catch {
        $statusCode = 0
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode.value__
        }
        return @{ StatusCode = $statusCode; Content = $_.Exception.Message }
    }
}

# 辅助函数：记录测试结果
function Record-Test {
    param($name, $expected, $actual, $desc = "")
    
    $totalTests++
    $passed = ($expected -eq $actual)
    if ($passed) {
        $passedTests++
        Write-Host "[PASS] $name" -ForegroundColor Green
    } else {
        $failedTests++
        Write-Host "[FAIL] $name (期望：$expected, 实际：$actual)" -ForegroundColor Red
    }
    if ($desc) {
        Write-Host "       $desc" -ForegroundColor Gray
    }
    
    $results += [PSCustomObject]@{
        Test = $name
        Expected = $expected
        Actual = $actual
        Passed = $passed
        Description = $desc
    }
}

Write-Host "=== 第一部分：房产管理模块测试 ===" -ForegroundColor Cyan
Write-Host ""

# 测试 1: 居民提交房产认证申请
Write-Host "--- 测试场景 1: 居民端房产认证 ---" -ForegroundColor Yellow
$body = @{ houseId = 1; relation = "业主" } | ConvertTo-Json -Compress
$result = Send-Request -Method "POST" -Endpoint "/api/resident-house/apply" -Token $residentToken -Body $body
Record-Test "居民提交房产认证申请" 200 $result.StatusCode "居民用户提交房产认证申请"

# 测试 2: 物业获取待审核申请列表
Write-Host ""
Write-Host "--- 测试场景 2: 物业端审核功能 ---" -ForegroundColor Yellow
$result = Send-Request -Method "GET" -Endpoint "/api/resident-house/pending" -Token $propertyToken
Record-Test "物业获取待审核申请列表" 200 $result.StatusCode "获取所有待审核的认证申请"

# 测试 3: 物业审核通过
$result = Send-Request -Method "PUT" -Endpoint "/api/resident-house/approve?userId=3&houseId=1" -Token $propertyToken
Record-Test "物业审核通过申请" 200 $result.StatusCode "审核通过房产认证申请"

# 测试 4: 物业获取已审核记录
$result = Send-Request -Method "GET" -Endpoint "/api/resident-house/processed" -Token $propertyToken
Record-Test "物业获取已审核记录" 200 $result.StatusCode "获取已审核通过的认证记录"

# 测试 5: 物业撤回认证
$result = Send-Request -Method "PUT" -Endpoint "/api/resident-house/revoke?userId=3&houseId=1" -Token $propertyToken
Record-Test "物业撤回认证" 200 $result.StatusCode "撤回已通过的认证"

# 测试 6: 物业拒绝认证申请
$body = @{ houseId = 2; relation = "租户" } | ConvertTo-Json -Compress
Send-Request -Method "POST" -Endpoint "/api/resident-house/apply" -Token $residentToken -Body $body
$result = Send-Request -Method "DELETE" -Endpoint "/api/resident-house/reject?userId=3&houseId=2" -Token $propertyToken
Record-Test "物业拒绝认证申请" 200 $result.StatusCode "拒绝房产认证申请"

# 测试 7: 获取楼栋列表
Write-Host ""
Write-Host "--- 测试场景 3: 楼栋和房屋管理 ---" -ForegroundColor Yellow
$result = Send-Request -Method "GET" -Endpoint "/api/resident-house/buildings" -Token $propertyToken
Record-Test "获取楼栋列表" 200 $result.StatusCode "获取所有楼栋信息"

# 测试 8: 根据楼栋 ID 获取房屋列表
$result = Send-Request -Method "GET" -Endpoint "/api/resident-house/houses/1" -Token $propertyToken
Record-Test "根据楼栋 ID 获取房屋列表" 200 $result.StatusCode "获取指定楼栋的所有房屋"

# 测试 9: 获取所有房屋列表
$result = Send-Request -Method "GET" -Endpoint "/api/resident-house/houses/all" -Token $propertyToken
Record-Test "获取所有房屋列表" 200 $result.StatusCode "获取所有房屋信息"

# 测试 10: 物业新增房产
$body = @{ buildingId = 1; houseNo = "1001"; status = 0 } | ConvertTo-Json -Compress
$result = Send-Request -Method "POST" -Endpoint "/api/resident-house" -Token $propertyToken -Body $body
Record-Test "物业新增房产" 200 $result.StatusCode "新增房产记录"

# 测试 11: 物业修改房产信息
$body = @{ buildingId = 1; houseNo = "1001"; status = 1 } | ConvertTo-Json -Compress
$result = Send-Request -Method "PUT" -Endpoint "/api/resident-house/1" -Token $propertyToken -Body $body
Record-Test "物业修改房产信息" 200 $result.StatusCode "修改房产信息"

# 测试 12: 物业删除房产
$result = Send-Request -Method "DELETE" -Endpoint "/api/resident-house/1" -Token $propertyToken
Record-Test "物业删除房产" 200 $result.StatusCode "删除房产记录"

# 测试 13: 居民查看自己的房产
Write-Host ""
Write-Host "--- 测试场景 4: 数据隔离测试 ---" -ForegroundColor Yellow
$result = Send-Request -Method "GET" -Endpoint "/api/resident-house/user/3" -Token $residentToken
Record-Test "居民查看自己的房产" 200 $result.StatusCode "居民用户查看自己的房产列表"

# 测试 14: 居民查看他人房产（权限测试）
$result = Send-Request -Method "GET" -Endpoint "/api/resident-house/user/4" -Token $residentToken
Record-Test "居民查看他人房产 (权限测试)" 403 $result.StatusCode "居民用户尝试查看其他用户的房产（应被拒绝）"

# 测试 15: 物业查看所有房产
$result = Send-Request -Method "GET" -Endpoint "/api/resident-house/all" -Token $propertyToken
Record-Test "物业查看所有房产" 200 $result.StatusCode "物业用户查看全社区所有房产"

# 测试 16: 检查用户认证状态
$result = Send-Request -Method "GET" -Endpoint "/api/resident-house/certified?userId=3" -Token $propertyToken
Record-Test "检查用户认证状态" 200 $result.StatusCode "检查用户是否已认证房产"

Write-Host ""
Write-Host "=== 第二部分：快递服务模块测试 ===" -ForegroundColor Cyan
Write-Host ""

# 测试 17: 物业快递入库
Write-Host "--- 测试场景 1: 快递入库 ---" -ForegroundColor Yellow
$body = @{ trackingNo = "YT123456789"; userId = 3; status = 0 } | ConvertTo-Json -Compress
$result = Send-Request -Method "POST" -Endpoint "/api/express/inbound" -Token $propertyToken -Body $body
Record-Test "物业快递入库" 200 $result.StatusCode "快递入库，自动生成取件码"

# 测试 18: 重复入库
$body = @{ trackingNo = "YT987654321"; userId = 3; status = 0 } | ConvertTo-Json -Compress
$result = Send-Request -Method "POST" -Endpoint "/api/express/inbound" -Token $propertyToken -Body $body
Record-Test "重复快递入库" 200 $result.StatusCode "另一个快递入库"

# 测试 19: 居民取件核销
Write-Host ""
Write-Host "--- 测试场景 2: 取件功能测试 ---" -ForegroundColor Yellow
$result = Send-Request -Method "GET" -Endpoint "/api/express" -Token $residentToken
if ($result.StatusCode -eq 200 -and $result.Content) {
    $expressList = $result.Content
    if ($expressList -and $expressList.Count -gt 0) {
        $pickupCode = $expressList[0].pickupCode
        Write-Host "  使用取件码：$pickupCode" -ForegroundColor Gray
        $result = Send-Request -Method "POST" -Endpoint "/api/express/pickup/$pickupCode" -Token $residentToken
        Record-Test "居民取件核销" 200 $result.StatusCode "使用取件码核销快递"
    } else {
        Record-Test "居民取件核销" 200 0 "没有可取件的快递"
    }
} else {
    Record-Test "居民取件核销" 200 $result.StatusCode "获取快递列表失败"
}

# 测试 20: 无效取件码取件
$result = Send-Request -Method "POST" -Endpoint "/api/express/pickup/INVALID999" -Token $residentToken
Record-Test "无效取件码取件" 500 $result.StatusCode "使用无效取件码尝试取件（应返回错误）"

# 测试 21: 居民查看自己的快递列表
Write-Host ""
Write-Host "--- 测试场景 3: 快递列表查询 ---" -ForegroundColor Yellow
$result = Send-Request -Method "GET" -Endpoint "/api/express" -Token $residentToken
Record-Test "居民查看自己的快递列表" 200 $result.StatusCode "居民用户查看自己的快递"

# 测试 22: 物业查看所有快递列表
$result = Send-Request -Method "GET" -Endpoint "/api/express" -Token $propertyToken
Record-Test "物业查看所有快递列表" 200 $result.StatusCode "物业用户查看所有快递"

# 测试 23: 居民数据隔离验证
Write-Host ""
Write-Host "--- 测试场景 4: 数据隔离测试 ---" -ForegroundColor Yellow
$body = @{ trackingNo = "YT555666777"; userId = 4; status = 0 } | ConvertTo-Json -Compress
Send-Request -Method "POST" -Endpoint "/api/express/inbound" -Token $propertyToken -Body $body
$result = Send-Request -Method "GET" -Endpoint "/api/express" -Token $residentToken
$hasOtherUserExpress = $false
if ($result.Content) {
    foreach ($express in $result.Content) {
        if ($express.userId -ne 3) {
            $hasOtherUserExpress = $true
            break
        }
    }
}
$passed = (-not $hasOtherUserExpress)
$totalTests++
if ($passed) {
    $passedTests++
    Write-Host "[PASS] 居民数据隔离验证" -ForegroundColor Green
} else {
    $failedTests++
    Write-Host "[FAIL] 居民数据隔离验证 (居民看到了其他用户的快递)" -ForegroundColor Red
}
$results += [PSCustomObject]@{
    Test = "居民数据隔离验证"
    Expected = "只能看到自己的快递"
    Actual = $(if ($passed) { "通过" } else { "失败" })
    Passed = $passed
    Description = "验证居民用户只能看到自己的快递"
}

# 测试 24: 物业数据隔离验证
$result = Send-Request -Method "GET" -Endpoint "/api/express" -Token $propertyToken
$hasUserId3 = $false
$hasUserId4 = $false
if ($result.Content) {
    foreach ($express in $result.Content) {
        if ($express.userId -eq 3) { $hasUserId3 = $true }
        if ($express.userId -eq 4) { $hasUserId4 = $true }
    }
}
$passed = ($hasUserId3 -and $hasUserId4)
$totalTests++
if ($passed) {
    $passedTests++
    Write-Host "[PASS] 物业数据隔离验证" -ForegroundColor Green
} else {
    $failedTests++
    Write-Host "[FAIL] 物业数据隔离验证 (物业未看到所有快递)" -ForegroundColor Red
}
$results += [PSCustomObject]@{
    Test = "物业数据隔离验证"
    Expected = "可以看到所有快递"
    Actual = $(if ($passed) { "通过" } else { "失败" })
    Passed = $passed
    Description = "验证物业用户可以看到所有快递"
}

# 测试总结
Write-Host ""
Write-Host "========================================"
Write-Host "  测试执行完成"
Write-Host "========================================"
Write-Host ""
Write-Host "测试统计:" -ForegroundColor Yellow
Write-Host "  总测试用例数：$totalTests"
Write-Host "  通过用例数：$passedTests" -ForegroundColor Green
Write-Host "  失败用例数：$failedTests" -ForegroundColor $(if ($failedTests -gt 0) { "Red" } else { "Green" })
$passRate = [math]::Round(($passedTests / $totalTests) * 100, 2)
Write-Host "  通过率：$passRate%" -ForegroundColor $(if ($passRate -eq 100) { "Green" } else { "Yellow" })
Write-Host ""

# 导出测试报告
$reportPath = "c:\Users\fengchunzhao\Desktop\future-community-system\backend\test-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').csv"
$results | Export-Csv -Path $reportPath -NoTypeInformation -Encoding UTF8
Write-Host "测试报告已导出至：$reportPath" -ForegroundColor Cyan
Write-Host ""
