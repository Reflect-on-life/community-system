# API 测试脚本 - 公告管理模块和数据驾驶舱模块
# 测试社区服务管理系统的 API 功能

$baseUrl = "http://localhost:8080"
$loginUrl = "$baseUrl/api/login"
$testResults = @()

# 颜色输出函数
function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Error_ { Write-Host $args -ForegroundColor Red }
function Write-Warning_ { Write-Host $args -ForegroundColor Yellow }
function Write-Info { Write-Host $args -ForegroundColor Cyan }

# 记录测试结果
function Add-TestResult {
    param(
        [string]$Module,
        [string]$TestCase,
        [string]$Endpoint,
        [string]$Method,
        [int]$StatusCode,
        [string]$Result,
        [string]$Message
    )
    $testResults += [PSCustomObject]@{
        Module = $Module
        TestCase = $TestCase
        Endpoint = $Endpoint
        Method = $Method
        StatusCode = $StatusCode
        Result = $Result
        Message = $Message
    }
}

Write-Info "========================================="
Write-Info "   社区服务管理系统 API 功能测试"
Write-Info "   测试模块：公告管理、数据驾驶舱"
Write-Info "========================================="
Write-Host ""

# ==================== 登录获取 Token ====================
Write-Info "【步骤 1】登录获取管理员 Token..."
try {
    $loginBody = @{
        username = "admin"
        password = "123456"
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri $loginUrl -Method Post -Body $loginBody -ContentType "application/json; charset=utf-8"
    $adminToken = $loginResponse.token
    Write-Success "✓ 管理员登录成功，Token: ${adminToken.Substring(0, 20)}..."
} catch {
    Write-Error_ "✗ 管理员登录失败：$($_.Exception.Message)"
    # 尝试使用物业管理员登录
    try {
        $loginBody = @{
            username = "property"
            password = "123456"
        } | ConvertTo-Json
        $loginResponse = Invoke-RestMethod -Uri $loginUrl -Method Post -Body $loginBody -ContentType "application/json; charset=utf-8"
        $adminToken = $loginResponse.token
        Write-Success "✓ 物业管理员登录成功，Token: ${adminToken.Substring(0, 20)}..."
    } catch {
        Write-Error_ "✗ 物业管理员登录也失败：$($_.Exception.Message)"
        $adminToken = ""
    }
}

Write-Host ""

# ==================== 公告管理模块测试 ====================
Write-Info "========================================="
Write-Info "   模块一：公告管理模块测试"
Write-Info "========================================="
Write-Host ""

$headers = @{
    "Authorization" = "Bearer $adminToken"
    "Content-Type" = "application/json; charset=utf-8"
}

# --- 测试用例 1: 发布公告 ---
Write-Info "[测试 1.1] 发布公告 (POST /api/notice)"
try {
    $noticeBody = @{
        title = "系统维护通知 - API 测试"
        content = "<p>尊敬的业主们：</p><p>系统将于今晚进行例行维护，请提前保存数据。</p>"
        targetType = "all"
        repairId = $null
    } | ConvertTo-Json -Depth 10

    $response = Invoke-RestMethod -Uri "$baseUrl/api/notice" -Method Post -Body $noticeBody -Headers $headers
    $noticeId = $response.id
    Write-Success "✓ 发布公告成功"
    Write-Info "  公告 ID: $noticeId"
    Write-Info "  标题：$($response.title)"
    Write-Info "  发布人：$($response.publisher)"
    Add-TestResult -Module "公告管理" -TestCase "发布公告" -Endpoint "/api/notice" -Method "POST" -StatusCode 200 -Result "PASS" -Message "公告创建成功，ID=$noticeId"
} catch {
    Write-Error_ "✗ 发布公告失败：$($_.Exception.Message)"
    Add-TestResult -Module "公告管理" -TestCase "发布公告" -Endpoint "/api/notice" -Method "POST" -StatusCode $_.Exception.Response.StatusCode.value__ -Result "FAIL" -Message $_.Exception.Message
    $noticeId = $null
}

Write-Host ""

# --- 测试用例 2: 获取公告列表 ---
Write-Info "[测试 1.2] 获取公告列表 (GET /api/notice)"
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/notice" -Method Get -Headers $headers
    Write-Success "✓ 获取公告列表成功"
    Write-Info "  公告总数：$($response.Count)"
    if ($response.Count -gt 0) {
        Write-Info "  最新公告：$($response[0].title)"
    }
    Add-TestResult -Module "公告管理" -TestCase "获取公告列表" -Endpoint "/api/notice" -Method "GET" -StatusCode 200 -Result "PASS" -Message "返回 $($response.Count) 条公告"
} catch {
    Write-Error_ "✗ 获取公告列表失败：$($_.Exception.Message)"
    Add-TestResult -Module "公告管理" -TestCase "获取公告列表" -Endpoint "/api/notice" -Method "GET" -StatusCode $_.Exception.Response.StatusCode.value__ -Result "FAIL" -Message $_.Exception.Message
}

Write-Host ""

# --- 测试用例 3: 获取用户可见公告 ---
Write-Info "[测试 1.3] 获取用户可见公告 (GET /api/notice/user)"
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/notice/user" -Method Get -Headers $headers
    Write-Success "✓ 获取用户可见公告成功"
    Write-Info "  可见公告数：$($response.Count)"
    Add-TestResult -Module "公告管理" -TestCase "获取用户可见公告" -Endpoint "/api/notice/user" -Method "GET" -StatusCode 200 -Result "PASS" -Message "返回 $($response.Count) 条可见公告"
} catch {
    Write-Error_ "✗ 获取用户可见公告失败：$($_.Exception.Message)"
    Add-TestResult -Module "公告管理" -TestCase "获取用户可见公告" -Endpoint "/api/notice/user" -Method "GET" -StatusCode $_.Exception.Response.StatusCode.value__ -Result "FAIL" -Message $_.Exception.Message
}

Write-Host ""

# --- 测试用例 4: 获取公告详情 ---
Write-Info "[测试 1.4] 获取公告详情 (GET /api/notice/{id})"
if ($noticeId) {
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/notice/$noticeId" -Method Get -Headers $headers
        Write-Success "✓ 获取公告详情成功"
        Write-Info "  公告标题：$($response.title)"
        Write-Info "  公告内容：$($response.content.Substring(0, [Math]::Min(50, $response.content.Length)))..."
        Add-TestResult -Module "公告管理" -TestCase "获取公告详情" -Endpoint "/api/notice/{id}" -Method "GET" -StatusCode 200 -Result "PASS" -Message "成功获取公告详情"
    } catch {
        Write-Error_ "✗ 获取公告详情失败：$($_.Exception.Message)"
        Add-TestResult -Module "公告管理" -TestCase "获取公告详情" -Endpoint "/api/notice/{id}" -Method "GET" -StatusCode $_.Exception.Response.StatusCode.value__ -Result "FAIL" -Message $_.Exception.Message
    }
} else {
    Write-Warning_ "⚠ 跳过测试（无可用公告 ID）"
    Add-TestResult -Module "公告管理" -TestCase "获取公告详情" -Endpoint "/api/notice/{id}" -Method "GET" -StatusCode "N/A" -Result "SKIP" -Message "无可用公告 ID"
}

Write-Host ""

# --- 测试用例 5: 按报修 ID 获取公告 ---
Write-Info "[测试 1.5] 按报修 ID 获取公告 (GET /api/notice/repair/{repairId})"
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/notice/repair/1" -Method Get -Headers $headers
    Write-Success "✓ 按报修 ID 获取公告成功"
    Write-Info "  相关公告数：$($response.Count)"
    Add-TestResult -Module "公告管理" -TestCase "按报修 ID 获取公告" -Endpoint "/api/notice/repair/{repairId}" -Method "GET" -StatusCode 200 -Result "PASS" -Message "返回 $($response.Count) 条相关公告"
} catch {
    Write-Error_ "✗ 按报修 ID 获取公告失败：$($_.Exception.Message)"
    Add-TestResult -Module "公告管理" -TestCase "按报修 ID 获取公告" -Endpoint "/api/notice/repair/{repairId}" -Method "GET" -StatusCode $_.Exception.Response.StatusCode.value__ -Result "FAIL" -Message $_.Exception.Message
}

Write-Host ""

# --- 测试用例 6: 修改公告 ---
Write-Info "[测试 1.6] 修改公告 (PUT /api/notice/{id})"
if ($noticeId) {
    try {
        $updateBody = @{
            title = "系统维护通知 - API 测试 (已更新)"
            content = "<p>尊敬的业主们：</p><p>系统维护时间调整为明晚，请知悉。</p>"
            targetType = "all"
            repairId = $null
        } | ConvertTo-Json -Depth 10

        $response = Invoke-RestMethod -Uri "$baseUrl/api/notice/$noticeId" -Method Put -Body $updateBody -Headers $headers
        Write-Success "✓ 修改公告成功"
        Write-Info "  更新后标题：$($response.title)"
        Add-TestResult -Module "公告管理" -TestCase "修改公告" -Endpoint "/api/notice/{id}" -Method "PUT" -StatusCode 200 -Result "PASS" -Message "公告更新成功"
    } catch {
        Write-Error_ "✗ 修改公告失败：$($_.Exception.Message)"
        Add-TestResult -Module "公告管理" -TestCase "修改公告" -Endpoint "/api/notice/{id}" -Method "PUT" -StatusCode $_.Exception.Response.StatusCode.value__ -Result "FAIL" -Message $_.Exception.Message
    }
} else {
    Write-Warning_ "⚠ 跳过测试（无可用公告 ID）"
    Add-TestResult -Module "公告管理" -TestCase "修改公告" -Endpoint "/api/notice/{id}" -Method "PUT" -StatusCode "N/A" -Result "SKIP" -Message "无可用公告 ID"
}

Write-Host ""

# --- 测试用例 7: 删除公告 ---
Write-Info "[测试 1.7] 删除公告 (DELETE /api/notice/{id})"
if ($noticeId) {
    try {
        Invoke-RestMethod -Uri "$baseUrl/api/notice/$noticeId" -Method Delete -Headers $headers
        Write-Success "✓ 删除公告成功"
        Write-Info "  已删除公告 ID: $noticeId"
        Add-TestResult -Module "公告管理" -TestCase "删除公告" -Endpoint "/api/notice/{id}" -Method "DELETE" -StatusCode 200 -Result "PASS" -Message "公告删除成功"
        $noticeId = $null
    } catch {
        Write-Error_ "✗ 删除公告失败：$($_.Exception.Message)"
        Add-TestResult -Module "公告管理" -TestCase "删除公告" -Endpoint "/api/notice/{id}" -Method "DELETE" -StatusCode $_.Exception.Response.StatusCode.value__ -Result "FAIL" -Message $_.Exception.Message
    }
} else {
    Write-Warning_ "⚠ 跳过测试（无可用公告 ID）"
    Add-TestResult -Module "公告管理" -TestCase "删除公告" -Endpoint "/api/notice/{id}" -Method "DELETE" -StatusCode "N/A" -Result "SKIP" -Message "无可用公告 ID"
}

Write-Host ""

# --- 测试用例 8: 公告统计 ---
Write-Info "[测试 1.8] 公告阅读统计 (GET /api/admin/notice-stats/{noticeId})"
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/admin/notice-stats/1" -Method Get -Headers $headers
    Write-Success "✓ 获取公告统计成功"
    Write-Info "  总用户数：$($response.totalUsers)"
    Write-Info "  已阅读数：$($response.readCount)"
    Write-Info "  未阅读数：$($response.unreadCount)"
    Write-Info "  阅读率：$([math]::Round($response.readRate * 100, 2))%"
    if ($response.roleStats) {
        Write-Info "  角色统计："
        foreach ($role in $response.roleStats.GetEnumerator()) {
            Write-Info "    - $($role.Key): $($role.Value) 人"
        }
    }
    Add-TestResult -Module "公告管理" -TestCase "公告阅读统计" -Endpoint "/api/admin/notice-stats/{noticeId}" -Method "GET" -StatusCode 200 -Result "PASS" -Message "统计数据分析正常"
} catch {
    Write-Error_ "✗ 获取公告统计失败：$($_.Exception.Message)"
    Add-TestResult -Module "公告管理" -TestCase "公告阅读统计" -Endpoint "/api/admin/notice-stats/{noticeId}" -Method "GET" -StatusCode $_.Exception.Response.StatusCode.value__ -Result "FAIL" -Message $_.Exception.Message
}

Write-Host ""

# ==================== 数据驾驶舱模块测试 ====================
Write-Info "========================================="
Write-Info "   模块二：数据驾驶舱模块测试"
Write-Info "========================================="
Write-Host ""

# --- 测试用例 9: 获取驾驶舱数据 ---
Write-Info "[测试 2.1] 获取驾驶舱数据 (GET /api/admin/dashboard/summary)"
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/admin/dashboard/summary" -Method Get -Headers $headers
    Write-Success "✓ 获取驾驶舱数据成功"
    Write-Info "  用户总数：$($response.userCount)"
    Write-Info "  今日报修：$($response.todayRepairCount)"
    Write-Info "  待处理投诉：$($response.pendingComplaintCount)"
    Write-Info "  公告总数：$($response.noticeCount)"
    Write-Info "  活动报名：$($response.activitySignupCount)"
    Write-Info "  平均阅读率：$([math]::Round($response.avgReadRate * 100, 2))%"
    if ($response.userRoleStats) {
        Write-Info "  角色分布："
        foreach ($role in $response.userRoleStats.GetEnumerator()) {
            Write-Info "    - $($role.Key): $($role.Value) 人"
        }
    }
    if ($response.repairTrend) {
        Write-Info "  报修趋势 (近 7 天): $($response.repairTrend.Count) 天数据"
    }
    if ($response.complaintTrend) {
        Write-Info "  投诉趋势 (近 7 天): $($response.complaintTrend.Count) 天数据"
    }
    Add-TestResult -Module "数据驾驶舱" -TestCase "获取驾驶舱数据" -Endpoint "/api/admin/dashboard/summary" -Method "GET" -StatusCode 200 -Result "PASS" -Message "驾驶舱数据完整"
} catch {
    Write-Error_ "✗ 获取驾驶舱数据失败：$($_.Exception.Message)"
    Add-TestResult -Module "数据驾驶舱" -TestCase "获取驾驶舱数据" -Endpoint "/api/admin/dashboard/summary" -Method "GET" -StatusCode $_.Exception.Response.StatusCode.value__ -Result "FAIL" -Message $_.Exception.Message
}

Write-Host ""

# --- 测试用例 10: 社区概况统计 ---
Write-Info "[测试 2.2] 社区概况统计 (GET /api/statistics/community-overview)"
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/statistics/community-overview" -Method Get -Headers $headers
    Write-Success "✓ 获取社区概况成功"
    Write-Info "  总居民数：$($response.totalResidents)"
    Write-Info "  已认证户数：$($response.certifiedHouses)"
    Write-Info "  今日新增报修：$($response.todayNewRepairs)"
    Write-Info "  今日新增快递：$($response.todayNewExpress)"
    Write-Info "  待处理预约：$($response.pendingAppointments)"
    Add-TestResult -Module "数据驾驶舱" -TestCase "社区概况统计" -Endpoint "/api/statistics/community-overview" -Method "GET" -StatusCode 200 -Result "PASS" -Message "社区概况数据完整"
} catch {
    Write-Error_ "✗ 获取社区概况失败：$($_.Exception.Message)"
    Add-TestResult -Module "数据驾驶舱" -TestCase "社区概况统计" -Endpoint "/api/statistics/community-overview" -Method "GET" -StatusCode $_.Exception.Response.StatusCode.value__ -Result "FAIL" -Message $_.Exception.Message
}

Write-Host ""

# --- 测试用例 11: 快递趋势统计 ---
Write-Info "[测试 2.3] 快递趋势统计 (GET /api/statistics/express-trend)"
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/statistics/express-trend" -Method Get -Headers $headers
    Write-Success "✓ 获取快递趋势成功"
    Write-Info "  统计天数：$($response.dates.Count) 天"
    Write-Info "  入库数据点数：$($response.inbound.Count)"
    Write-Info "  取件数据点数：$($response.outbound.Count)"
    Add-TestResult -Module "数据驾驶舱" -TestCase "快递趋势统计" -Endpoint "/api/statistics/express-trend" -Method "GET" -StatusCode 200 -Result "PASS" -Message "快递趋势数据完整"
} catch {
    Write-Error_ "✗ 获取快递趋势失败：$($_.Exception.Message)"
    Add-TestResult -Module "数据驾驶舱" -TestCase "快递趋势统计" -Endpoint "/api/statistics/express-trend" -Method "GET" -StatusCode $_.Exception.Response.StatusCode.value__ -Result "FAIL" -Message $_.Exception.Message
}

Write-Host ""

# --- 测试用例 12: 人口结构分布 ---
Write-Info "[测试 2.4] 人口结构分布 (GET /api/statistics/population-distribution)"
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/statistics/population-distribution" -Method Get -Headers $headers
    Write-Success "✓ 获取人口分布成功"
    if ($response.gender) {
        Write-Info "  男性：$($response.gender.male) 人"
        Write-Info "  女性：$($response.gender.female) 人"
        Write-Info "  未知：$($response.gender.unknown) 人"
    }
    Add-TestResult -Module "数据驾驶舱" -TestCase "人口结构分布" -Endpoint "/api/statistics/population-distribution" -Method "GET" -StatusCode 200 -Result "PASS" -Message "人口分布数据完整"
} catch {
    Write-Error_ "✗ 获取人口分布失败：$($_.Exception.Message)"
    Add-TestResult -Module "数据驾驶舱" -TestCase "人口结构分布" -Endpoint "/api/statistics/population-distribution" -Method "GET" -StatusCode $_.Exception.Response.StatusCode.value__ -Result "FAIL" -Message $_.Exception.Message
}

Write-Host ""

# --- 测试用例 13: 报修完结率 ---
Write-Info "[测试 2.5] 报修完结率 (GET /api/statistics/repair-completion-rate)"
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/statistics/repair-completion-rate" -Method Get -Headers $headers
    Write-Success "✓ 获取报修完结率成功"
    Write-Info "  待处理：$($response.pending) 单"
    Write-Info "  处理中：$($response.processing) 单"
    Write-Info "  已完成：$($response.completed) 单"
    Write-Info "  已取消：$($response.cancelled) 单"
    Write-Info "  完结率：$($response.completionRate)%"
    Add-TestResult -Module "数据驾驶舱" -TestCase "报修完结率" -Endpoint "/api/statistics/repair-completion-rate" -Method "GET" -StatusCode 200 -Result "PASS" -Message "报修完结率数据完整"
} catch {
    Write-Error_ "✗ 获取报修完结率失败：$($_.Exception.Message)"
    Add-TestResult -Module "数据驾驶舱" -TestCase "报修完结率" -Endpoint "/api/statistics/repair-completion-rate" -Method "GET" -StatusCode $_.Exception.Response.StatusCode.value__ -Result "FAIL" -Message $_.Exception.Message
}

Write-Host ""

# --- 测试用例 14: 活动热度 ---
Write-Info "[测试 2.6] 活动热度 (GET /api/statistics/activity-heat)"
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/statistics/activity-heat" -Method Get -Headers $headers
    Write-Success "✓ 获取活动热度成功"
    Write-Info "  活动数量：$($response.activityParticipation.Count)"
    if ($response.activityParticipation) {
        foreach ($activity in $response.activityParticipation.GetEnumerator()) {
            Write-Info "  - $($activity.Key): $($activity.Value) 人报名"
        }
    }
    Add-TestResult -Module "数据驾驶舱" -TestCase "活动热度" -Endpoint "/api/statistics/activity-heat" -Method "GET" -StatusCode 200 -Result "PASS" -Message "活动热度数据完整"
} catch {
    Write-Error_ "✗ 获取活动热度失败：$($_.Exception.Message)"
    Add-TestResult -Module "数据驾驶舱" -TestCase "活动热度" -Endpoint "/api/statistics/activity-heat" -Method "GET" -StatusCode $_.Exception.Response.StatusCode.value__ -Result "FAIL" -Message $_.Exception.Message
}

Write-Host ""

# --- 测试用例 15: 养老服务统计 ---
Write-Info "[测试 2.7] 养老服务统计 (GET /api/statistics/elderly-care)"
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/statistics/elderly-care" -Method Get -Headers $headers
    Write-Success "✓ 获取养老服务统计成功"
    Write-Info "  响应数据：$($response | ConvertTo-Json -Depth 5)"
    Add-TestResult -Module "数据驾驶舱" -TestCase "养老服务统计" -Endpoint "/api/statistics/elderly-care" -Method "GET" -StatusCode 200 -Result "PASS" -Message "养老服务数据完整"
} catch {
    Write-Error_ "✗ 获取养老服务统计失败：$($_.Exception.Message)"
    Add-TestResult -Module "数据驾驶舱" -TestCase "养老服务统计" -Endpoint "/api/statistics/elderly-care" -Method "GET" -StatusCode $_.Exception.Response.StatusCode.value__ -Result "FAIL" -Message $_.Exception.Message
}

Write-Host ""

# ==================== 输出测试报告 ====================
Write-Info "========================================="
Write-Info "   测试报告汇总"
Write-Info "========================================="
Write-Host ""

$passCount = ($testResults | Where-Object { $_.Result -eq "PASS" }).Count
$failCount = ($testResults | Where-Object { $_.Result -eq "FAIL" }).Count
$skipCount = ($testResults | Where-Object { $_.Result -eq "SKIP" }).Count
$totalCount = $testResults.Count

Write-Host "测试用例总数：$totalCount" -ForegroundColor White
Write-Success "通过：$passCount"
Write-Error_ "失败：$failCount"
Write-Warning_ "跳过：$skipCount"
Write-Host ""

# 按模块分组显示结果
Write-Info "【公告管理模块】"
$noticeTests = $testResults | Where-Object { $_.Module -eq "公告管理" }
foreach ($test in $noticeTests) {
    if ($test.Result -eq "PASS") {
        Write-Success "  ✓ $($test.TestCase) - $($test.Message)"
    } elseif ($test.Result -eq "FAIL") {
        Write-Error_ "  ✗ $($test.TestCase) - $($test.Message)"
    } else {
        Write-Warning_ "  ⚠ $($test.TestCase) - $($test.Message)"
    }
}
Write-Host ""

Write-Info "【数据驾驶舱模块】"
$dashboardTests = $testResults | Where-Object { $_.Module -eq "数据驾驶舱" }
foreach ($test in $dashboardTests) {
    if ($test.Result -eq "PASS") {
        Write-Success "  ✓ $($test.TestCase) - $($test.Message)"
    } elseif ($test.Result -eq "FAIL") {
        Write-Error_ "  ✗ $($test.TestCase) - $($test.Message)"
    } else {
        Write-Warning_ "  ⚠ $($test.TestCase) - $($test.Message)"
    }
}
Write-Host ""

# 导出测试报告
$reportPath = "c:\Users\fengchunzhao\Desktop\future-community-system\api-test-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').csv"
$testResults | Export-Csv -Path $reportPath -NoTypeInformation -Encoding UTF8
Write-Info "测试报告已导出至：$reportPath"
Write-Host ""

Write-Info "========================================="
if ($failCount -eq 0) {
    Write-Success "   所有测试通过！✓"
} else {
    Write-Error_ "   部分测试失败，请检查错误信息 ✗"
}
Write-Info "========================================="
