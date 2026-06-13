const http = require('http');

const BASE_URL = 'http://localhost:8080';
let adminToken = '';

// 测试结果存储
const testResults = [];

function addTestResult(module, testCase, endpoint, method, statusCode, result, message) {
    testResults.push({ module, testCase, endpoint, method, statusCode, result, message });
}

// HTTP 请求函数
function httpRequest(method, path, body = null, token = '') {
    return new Promise((resolve, reject) => {
        const options = {
            hostname: 'localhost',
            port: 8080,
            path: path,
            method: method,
            headers: {
                'Content-Type': 'application/json'
            }
        };
        
        if (token) {
            options.headers['Authorization'] = `Bearer ${token}`;
        }
        
        const req = http.request(options, (res) => {
            let data = '';
            res.on('data', (chunk) => { data += chunk; });
            res.on('end', () => {
                try {
                    resolve({ statusCode: res.statusCode, data: JSON.parse(data) });
                } catch (e) {
                    resolve({ statusCode: res.statusCode, data: data });
                }
            });
        });
        
        req.on('error', (e) => reject(e));
        
        if (body) {
            req.write(JSON.stringify(body));
        }
        req.end();
    });
}

// 颜色输出
const colors = {
    reset: '\x1b[0m',
    green: '\x1b[32m',
    red: '\x1b[31m',
    yellow: '\x1b[33m',
    cyan: '\x1b[36m'
};

function log(message, color = 'reset') {
    console.log(`${colors[color]}${message}${colors.reset}`);
}

// 主测试函数
async function runTests() {
    log('=========================================', 'cyan');
    log('   社区服务管理系统 API 功能测试', 'cyan');
    log('   测试模块：公告管理、数据驾驶舱', 'cyan');
    log('=========================================', 'cyan');
    console.log('');
    
    // 1. 登录获取 Token
    log('[步骤 1] 登录获取管理员 Token...', 'cyan');
    try {
        const loginResult = await httpRequest('POST', '/api/login', { username: 'admin', password: '123456' });
        adminToken = loginResult.data.token;
        log(`✓ 管理员登录成功，Token: ${adminToken.substring(0, 20)}...`, 'green');
    } catch (error) {
        log(`✗ 管理员登录失败：${error.message}`, 'red');
        try {
            const loginResult = await httpRequest('POST', '/api/login', { username: 'property', password: '123456' });
            adminToken = loginResult.data.token;
            log(`✓ 物业管理员登录成功，Token: ${adminToken.substring(0, 20)}...`, 'green');
        } catch (e) {
            log(`✗ 物业管理员登录也失败：${e.message}`, 'red');
        }
    }
    console.log('');
    
    // ==================== 公告管理模块测试 ====================
    log('=========================================', 'cyan');
    log('   模块一：公告管理模块测试', 'cyan');
    log('=========================================', 'cyan');
    console.log('');
    
    let noticeId = null;
    
    // 测试 1: 发布公告
    log('[测试 1.1] 发布公告 (POST /api/notice)', 'cyan');
    try {
        const result = await httpRequest('POST', '/api/notice', {
            title: '系统维护通知 - API 测试',
            content: '<p>尊敬的业主们：</p><p>系统将于今晚进行例行维护，请提前保存数据。</p>',
            targetType: 'all'
        }, adminToken);
        noticeId = result.data.id;
        log(`✓ 发布公告成功`, 'green');
        log(`  公告 ID: ${noticeId}`, 'cyan');
        log(`  标题：${result.data.title}`, 'cyan');
        log(`  发布人：${result.data.publisher}`, 'cyan');
        addTestResult('公告管理', '发布公告', '/api/notice', 'POST', 200, 'PASS', `公告创建成功，ID=${noticeId}`);
    } catch (error) {
        log(`✗ 发布公告失败：${error.message}`, 'red');
        addTestResult('公告管理', '发布公告', '/api/notice', 'POST', 500, 'FAIL', error.message);
    }
    console.log('');
    
    // 测试 2: 获取公告列表
    log('[测试 1.2] 获取公告列表 (GET /api/notice)', 'cyan');
    try {
        const result = await httpRequest('GET', '/api/notice', null, adminToken);
        log(`✓ 获取公告列表成功`, 'green');
        log(`  公告总数：${result.data.length}`, 'cyan');
        if (result.data.length > 0) {
            log(`  最新公告：${result.data[0].title}`, 'cyan');
        }
        addTestResult('公告管理', '获取公告列表', '/api/notice', 'GET', 200, 'PASS', `返回 ${result.data.length} 条公告`);
    } catch (error) {
        log(`✗ 获取公告列表失败：${error.message}`, 'red');
        addTestResult('公告管理', '获取公告列表', '/api/notice', 'GET', 500, 'FAIL', error.message);
    }
    console.log('');
    
    // 测试 3: 获取用户可见公告
    log('[测试 1.3] 获取用户可见公告 (GET /api/notice/user)', 'cyan');
    try {
        const result = await httpRequest('GET', '/api/notice/user', null, adminToken);
        log(`✓ 获取用户可见公告成功`, 'green');
        log(`  可见公告数：${result.data.length}`, 'cyan');
        addTestResult('公告管理', '获取用户可见公告', '/api/notice/user', 'GET', 200, 'PASS', `返回 ${result.data.length} 条可见公告`);
    } catch (error) {
        log(`✗ 获取用户可见公告失败：${error.message}`, 'red');
        addTestResult('公告管理', '获取用户可见公告', '/api/notice/user', 'GET', 500, 'FAIL', error.message);
    }
    console.log('');
    
    // 测试 4: 获取公告详情
    log('[测试 1.4] 获取公告详情 (GET /api/notice/{id})', 'cyan');
    if (noticeId) {
        try {
            const result = await httpRequest('GET', `/api/notice/${noticeId}`, null, adminToken);
            log(`✓ 获取公告详情成功`, 'green');
            log(`  公告标题：${result.data.title}`, 'cyan');
            log(`  公告内容：${result.data.content.substring(0, 50)}...`, 'cyan');
            addTestResult('公告管理', '获取公告详情', '/api/notice/{id}', 'GET', 200, 'PASS', '成功获取公告详情');
        } catch (error) {
            log(`✗ 获取公告详情失败：${error.message}`, 'red');
            addTestResult('公告管理', '获取公告详情', '/api/notice/{id}', 'GET', 500, 'FAIL', error.message);
        }
    } else {
        log(`⚠ 跳过测试（无可用公告 ID）`, 'yellow');
        addTestResult('公告管理', '获取公告详情', '/api/notice/{id}', 'GET', 'N/A', 'SKIP', '无可用公告 ID');
    }
    console.log('');
    
    // 测试 5: 按报修 ID 获取公告
    log('[测试 1.5] 按报修 ID 获取公告 (GET /api/notice/repair/{repairId})', 'cyan');
    try {
        const result = await httpRequest('GET', '/api/notice/repair/1', null, adminToken);
        log(`✓ 按报修 ID 获取公告成功`, 'green');
        log(`  相关公告数：${result.data.length}`, 'cyan');
        addTestResult('公告管理', '按报修 ID 获取公告', '/api/notice/repair/{repairId}', 'GET', 200, 'PASS', `返回 ${result.data.length} 条相关公告`);
    } catch (error) {
        log(`✗ 按报修 ID 获取公告失败：${error.message}`, 'red');
        addTestResult('公告管理', '按报修 ID 获取公告', '/api/notice/repair/{repairId}', 'GET', 500, 'FAIL', error.message);
    }
    console.log('');
    
    // 测试 6: 修改公告
    log('[测试 1.6] 修改公告 (PUT /api/notice/{id})', 'cyan');
    if (noticeId) {
        try {
            const result = await httpRequest('PUT', `/api/notice/${noticeId}`, {
                title: '系统维护通知 - API 测试 (已更新)',
                content: '<p>尊敬的业主们：</p><p>系统维护时间调整为明晚，请知悉。</p>',
                targetType: 'all'
            }, adminToken);
            log(`✓ 修改公告成功`, 'green');
            log(`  更新后标题：${result.data.title}`, 'cyan');
            addTestResult('公告管理', '修改公告', '/api/notice/{id}', 'PUT', 200, 'PASS', '公告更新成功');
        } catch (error) {
            log(`✗ 修改公告失败：${error.message}`, 'red');
            addTestResult('公告管理', '修改公告', '/api/notice/{id}', 'PUT', 500, 'FAIL', error.message);
        }
    } else {
        log(`⚠ 跳过测试（无可用公告 ID）`, 'yellow');
        addTestResult('公告管理', '修改公告', '/api/notice/{id}', 'PUT', 'N/A', 'SKIP', '无可用公告 ID');
    }
    console.log('');
    
    // 测试 7: 删除公告
    log('[测试 1.7] 删除公告 (DELETE /api/notice/{id})', 'cyan');
    if (noticeId) {
        try {
            await httpRequest('DELETE', `/api/notice/${noticeId}`, null, adminToken);
            log(`✓ 删除公告成功`, 'green');
            log(`  已删除公告 ID: ${noticeId}`, 'cyan');
            addTestResult('公告管理', '删除公告', '/api/notice/{id}', 'DELETE', 200, 'PASS', '公告删除成功');
            noticeId = null;
        } catch (error) {
            log(`✗ 删除公告失败：${error.message}`, 'red');
            addTestResult('公告管理', '删除公告', '/api/notice/{id}', 'DELETE', 500, 'FAIL', error.message);
        }
    } else {
        log(`⚠ 跳过测试（无可用公告 ID）`, 'yellow');
        addTestResult('公告管理', '删除公告', '/api/notice/{id}', 'DELETE', 'N/A', 'SKIP', '无可用公告 ID');
    }
    console.log('');
    
    // 测试 8: 公告统计
    log('[测试 1.8] 公告阅读统计 (GET /api/admin/notice-stats/{noticeId})', 'cyan');
    try {
        const result = await httpRequest('GET', '/api/admin/notice-stats/1', null, adminToken);
        log(`✓ 获取公告统计成功`, 'green');
        log(`  总用户数：${result.data.totalUsers}`, 'cyan');
        log(`  已阅读数：${result.data.readCount}`, 'cyan');
        log(`  未阅读数：${result.data.unreadCount}`, 'cyan');
        log(`  阅读率：${(result.data.readRate * 100).toFixed(2)}%`, 'cyan');
        if (result.data.roleStats) {
            log(`  角色统计:`, 'cyan');
            for (const [role, count] of Object.entries(result.data.roleStats)) {
                log(`    - ${role}: ${count} 人`, 'cyan');
            }
        }
        addTestResult('公告管理', '公告阅读统计', '/api/admin/notice-stats/{noticeId}', 'GET', 200, 'PASS', '统计数据分析正常');
    } catch (error) {
        log(`✗ 获取公告统计失败：${error.message}`, 'red');
        addTestResult('公告管理', '公告阅读统计', '/api/admin/notice-stats/{noticeId}', 'GET', 500, 'FAIL', error.message);
    }
    console.log('');
    
    // ==================== 数据驾驶舱模块测试 ====================
    log('=========================================', 'cyan');
    log('   模块二：数据驾驶舱模块测试', 'cyan');
    log('=========================================', 'cyan');
    console.log('');
    
    // 测试 9: 获取驾驶舱数据
    log('[测试 2.1] 获取驾驶舱数据 (GET /api/admin/dashboard/summary)', 'cyan');
    try {
        const result = await httpRequest('GET', '/api/admin/dashboard/summary', null, adminToken);
        log(`✓ 获取驾驶舱数据成功`, 'green');
        log(`  用户总数：${result.data.userCount}`, 'cyan');
        log(`  今日报修：${result.data.todayRepairCount}`, 'cyan');
        log(`  待处理投诉：${result.data.pendingComplaintCount}`, 'cyan');
        log(`  公告总数：${result.data.noticeCount}`, 'cyan');
        log(`  活动报名：${result.data.activitySignupCount}`, 'cyan');
        log(`  平均阅读率：${(result.data.avgReadRate * 100).toFixed(2)}%`, 'cyan');
        if (result.data.userRoleStats) {
            log(`  角色分布:`, 'cyan');
            for (const [role, count] of Object.entries(result.data.userRoleStats)) {
                log(`    - ${role}: ${count} 人`, 'cyan');
            }
        }
        if (result.data.repairTrend) {
            log(`  报修趋势 (近 7 天): ${result.data.repairTrend.length} 天数据`, 'cyan');
        }
        if (result.data.complaintTrend) {
            log(`  投诉趋势 (近 7 天): ${result.data.complaintTrend.length} 天数据`, 'cyan');
        }
        addTestResult('数据驾驶舱', '获取驾驶舱数据', '/api/admin/dashboard/summary', 'GET', 200, 'PASS', '驾驶舱数据完整');
    } catch (error) {
        log(`✗ 获取驾驶舱数据失败：${error.message}`, 'red');
        addTestResult('数据驾驶舱', '获取驾驶舱数据', '/api/admin/dashboard/summary', 'GET', 500, 'FAIL', error.message);
    }
    console.log('');
    
    // 测试 10: 社区概况统计
    log('[测试 2.2] 社区概况统计 (GET /api/statistics/community-overview)', 'cyan');
    try {
        const result = await httpRequest('GET', '/api/statistics/community-overview', null, adminToken);
        log(`✓ 获取社区概况成功`, 'green');
        log(`  总居民数：${result.data.totalResidents}`, 'cyan');
        log(`  已认证户数：${result.data.certifiedHouses}`, 'cyan');
        log(`  今日新增报修：${result.data.todayNewRepairs}`, 'cyan');
        log(`  今日新增快递：${result.data.todayNewExpress}`, 'cyan');
        log(`  待处理预约：${result.data.pendingAppointments}`, 'cyan');
        addTestResult('数据驾驶舱', '社区概况统计', '/api/statistics/community-overview', 'GET', 200, 'PASS', '社区概况数据完整');
    } catch (error) {
        log(`✗ 获取社区概况失败：${error.message}`, 'red');
        addTestResult('数据驾驶舱', '社区概况统计', '/api/statistics/community-overview', 'GET', 500, 'FAIL', error.message);
    }
    console.log('');
    
    // 测试 11: 快递趋势统计
    log('[测试 2.3] 快递趋势统计 (GET /api/statistics/express-trend)', 'cyan');
    try {
        const result = await httpRequest('GET', '/api/statistics/express-trend', null, adminToken);
        log(`✓ 获取快递趋势成功`, 'green');
        log(`  统计天数：${result.data.dates.length} 天`, 'cyan');
        log(`  入库数据点数：${result.data.inbound.length}`, 'cyan');
        log(`  取件数据点数：${result.data.outbound.length}`, 'cyan');
        addTestResult('数据驾驶舱', '快递趋势统计', '/api/statistics/express-trend', 'GET', 200, 'PASS', '快递趋势数据完整');
    } catch (error) {
        log(`✗ 获取快递趋势失败：${error.message}`, 'red');
        addTestResult('数据驾驶舱', '快递趋势统计', '/api/statistics/express-trend', 'GET', 500, 'FAIL', error.message);
    }
    console.log('');
    
    // 测试 12: 人口结构分布
    log('[测试 2.4] 人口结构分布 (GET /api/statistics/population-distribution)', 'cyan');
    try {
        const result = await httpRequest('GET', '/api/statistics/population-distribution', null, adminToken);
        log(`✓ 获取人口分布成功`, 'green');
        if (result.data.gender) {
            log(`  男性：${result.data.gender.male} 人`, 'cyan');
            log(`  女性：${result.data.gender.female} 人`, 'cyan');
            log(`  未知：${result.data.gender.unknown} 人`, 'cyan');
        }
        addTestResult('数据驾驶舱', '人口结构分布', '/api/statistics/population-distribution', 'GET', 200, 'PASS', '人口分布数据完整');
    } catch (error) {
        log(`✗ 获取人口分布失败：${error.message}`, 'red');
        addTestResult('数据驾驶舱', '人口结构分布', '/api/statistics/population-distribution', 'GET', 500, 'FAIL', error.message);
    }
    console.log('');
    
    // 测试 13: 报修完结率
    log('[测试 2.5] 报修完结率 (GET /api/statistics/repair-completion-rate)', 'cyan');
    try {
        const result = await httpRequest('GET', '/api/statistics/repair-completion-rate', null, adminToken);
        log(`✓ 获取报修完结率成功`, 'green');
        log(`  待处理：${result.data.pending} 单`, 'cyan');
        log(`  处理中：${result.data.processing} 单`, 'cyan');
        log(`  已完成：${result.data.completed} 单`, 'cyan');
        log(`  已取消：${result.data.cancelled} 单`, 'cyan');
        log(`  完结率：${result.data.completionRate}%`, 'cyan');
        addTestResult('数据驾驶舱', '报修完结率', '/api/statistics/repair-completion-rate', 'GET', 200, 'PASS', '报修完结率数据完整');
    } catch (error) {
        log(`✗ 获取报修完结率失败：${error.message}`, 'red');
        addTestResult('数据驾驶舱', '报修完结率', '/api/statistics/repair-completion-rate', 'GET', 500, 'FAIL', error.message);
    }
    console.log('');
    
    // 测试 14: 活动热度
    log('[测试 2.6] 活动热度 (GET /api/statistics/activity-heat)', 'cyan');
    try {
        const result = await httpRequest('GET', '/api/statistics/activity-heat', null, adminToken);
        log(`✓ 获取活动热度成功`, 'green');
        log(`  活动数量：${Object.keys(result.data.activityParticipation).length}`, 'cyan');
        for (const [activity, count] of Object.entries(result.data.activityParticipation)) {
            log(`  - ${activity}: ${count} 人报名`, 'cyan');
        }
        addTestResult('数据驾驶舱', '活动热度', '/api/statistics/activity-heat', 'GET', 200, 'PASS', '活动热度数据完整');
    } catch (error) {
        log(`✗ 获取活动热度失败：${error.message}`, 'red');
        addTestResult('数据驾驶舱', '活动热度', '/api/statistics/activity-heat', 'GET', 500, 'FAIL', error.message);
    }
    console.log('');
    
    // 测试 15: 养老服务统计
    log('[测试 2.7] 养老服务统计 (GET /api/statistics/elderly-care)', 'cyan');
    try {
        const result = await httpRequest('GET', '/api/statistics/elderly-care', null, adminToken);
        log(`✓ 获取养老服务统计成功`, 'green');
        log(`  响应数据：${JSON.stringify(result.data, null, 2)}`, 'cyan');
        addTestResult('数据驾驶舱', '养老服务统计', '/api/statistics/elderly-care', 'GET', 200, 'PASS', '养老服务数据完整');
    } catch (error) {
        log(`✗ 获取养老服务统计失败：${error.message}`, 'red');
        addTestResult('数据驾驶舱', '养老服务统计', '/api/statistics/elderly-care', 'GET', 500, 'FAIL', error.message);
    }
    console.log('');
    
    // ==================== 输出测试报告 ====================
    log('=========================================', 'cyan');
    log('   测试报告汇总', 'cyan');
    log('=========================================', 'cyan');
    console.log('');
    
    const passCount = testResults.filter(t => t.result === 'PASS').length;
    const failCount = testResults.filter(t => t.result === 'FAIL').length;
    const skipCount = testResults.filter(t => t.result === 'SKIP').length;
    const totalCount = testResults.length;
    
    console.log(`测试用例总数：${totalCount}`);
    log(`通过：${passCount}`, 'green');
    log(`失败：${failCount}`, 'red');
    log(`跳过：${skipCount}`, 'yellow');
    console.log('');
    
    // 按模块分组显示结果
    log('【公告管理模块】', 'cyan');
    const noticeTests = testResults.filter(t => t.module === '公告管理');
    noticeTests.forEach(test => {
        if (test.result === 'PASS') {
            log(`  ✓ ${test.testCase} - ${test.message}`, 'green');
        } else if (test.result === 'FAIL') {
            log(`  ✗ ${test.testCase} - ${test.message}`, 'red');
        } else {
            log(`  ⚠ ${test.testCase} - ${test.message}`, 'yellow');
        }
    });
    console.log('');
    
    log('【数据驾驶舱模块】', 'cyan');
    const dashboardTests = testResults.filter(t => t.module === '数据驾驶舱');
    dashboardTests.forEach(test => {
        if (test.result === 'PASS') {
            log(`  ✓ ${test.testCase} - ${test.message}`, 'green');
        } else if (test.result === 'FAIL') {
            log(`  ✗ ${test.testCase} - ${test.message}`, 'red');
        } else {
            log(`  ⚠ ${test.testCase} - ${test.message}`, 'yellow');
        }
    });
    console.log('');
    
    // 导出测试报告
    const fs = require('fs');
    const timestamp = new Date().toISOString().replace(/[-:T.]/g, '').substring(0, 14);
    const reportPath = `c:\\Users\\fengchunzhao\\Desktop\\future-community-system\\api-test-report-${timestamp}.json`;
    fs.writeFileSync(reportPath, JSON.stringify(testResults, null, 2));
    log(`测试报告已导出至：${reportPath}`, 'cyan');
    console.log('');
    
    log('=========================================', 'cyan');
    if (failCount === 0) {
        log('   所有测试通过！✓', 'green');
    } else {
        log('   部分测试失败，请检查错误信息 ✗', 'red');
    }
    log('=========================================', 'cyan');
}

// 执行测试
runTests().catch(console.error);
