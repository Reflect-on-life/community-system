const http = require('http');

// 配置
const BASE_URL = 'http://localhost:8080';
const RESIDENT_PHONE = '13800000004';
const RESIDENT_PASSWORD = '123456';
const PROPERTY_PHONE = '13800000002';
const PROPERTY_PASSWORD = '123456';
const RESIDENT_USER_ID = 4;
const PROPERTY_USER_ID = 2;

// 测试状态
let residentToken = null;
let propertyToken = null;
let testActivityId = null;
let testSignupId = null;

// 测试结果统计
const testResults = [];
let totalTests = 0;
let passedTests = 0;
let failedTests = 0;

// HTTP 请求封装
function httpRequest(method, path, body = null, token = null) {
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
            res.on('data', (chunk) => {
                data += chunk;
            });
            res.on('end', () => {
                try {
                    const jsonData = data ? JSON.parse(data) : null;
                    resolve({
                        statusCode: res.statusCode,
                        data: jsonData
                    });
                } catch (e) {
                    resolve({
                        statusCode: res.statusCode,
                        data: data
                    });
                }
            });
        });

        req.on('error', (e) => {
            reject(e);
        });

        if (body) {
            req.write(JSON.stringify(body));
        }
        req.end();
    });
}

// 测试执行函数
async function runTest(testName, method, endpoint, body = null, expectedStatus = 200, description = '') {
    totalTests++;
    console.log(`\n【测试 ${totalTests}】${testName}`);
    console.log(`描述：${description}`);
    console.log(`请求：${method} ${endpoint}`);

    try {
        const response = await httpRequest(method, endpoint, body, residentToken || propertyToken);
        console.log(`响应状态码：${response.statusCode}`);
        console.log(`响应内容：${JSON.stringify(response.data, null, 2)}`);

        if (response.statusCode === expectedStatus) {
            console.log('✓ 测试通过');
            passedTests++;
            testResults.push({
                testName,
                method,
                endpoint,
                expectedStatus,
                actualStatus: response.statusCode,
                result: '通过'
            });
            return response.data;
        } else {
            console.log(`✗ 测试失败 - 期望状态码 ${expectedStatus}，实际 ${response.statusCode}`);
            failedTests++;
            testResults.push({
                testName,
                method,
                endpoint,
                expectedStatus,
                actualStatus: response.statusCode,
                result: '失败'
            });
            return response.data;
        }
    } catch (error) {
        console.log(`响应状态码：${error.statusCode || '请求失败'}`);
        console.log(`错误信息：${error.message}`);

        if (error.statusCode === expectedStatus) {
            console.log('✓ 测试通过');
            passedTests++;
            testResults.push({
                testName,
                method,
                endpoint,
                expectedStatus,
                actualStatus: error.statusCode,
                result: '通过'
            });
        } else {
            console.log('✗ 测试失败');
            failedTests++;
            testResults.push({
                testName,
                method,
                endpoint,
                expectedStatus,
                actualStatus: error.statusCode || 0,
                result: '失败'
            });
        }
        return null;
    }
}

// 登录函数
async function login(phone, password) {
    try {
        const response = await httpRequest('POST', '/api/auth/login', {
            phone,
            password
        });

        if (response.statusCode === 200 && response.data.token) {
            return response.data.token;
        }
        return null;
    } catch (error) {
        console.log(`登录失败：${error.message}`);
        return null;
    }
}

// 主测试流程
async function runAllTests() {
    console.log('');
    console.log('========================================');
    console.log('     社区活动模块功能测试');
    console.log(`     开始时间：${new Date().toLocaleString('zh-CN')}`);
    console.log('========================================');

    // 步骤 1: 登录获取 token
    console.log('\n【步骤 1】用户登录获取 Token');
    
    console.log('\n1.1 居民用户登录...');
    residentToken = await login(RESIDENT_PHONE, RESIDENT_PASSWORD);
    if (residentToken) {
        console.log('居民用户 Token 获取成功');
    } else {
        console.log('居民用户登录失败');
    }

    console.log('\n1.2 物业用户登录...');
    propertyToken = await login(PROPERTY_PHONE, PROPERTY_PASSWORD);
    if (propertyToken) {
        console.log('物业用户 Token 获取成功');
    } else {
        console.log('物业用户登录失败');
    }

    // 步骤 2: 测试活动浏览功能
    console.log('\n【步骤 2】测试活动浏览功能');
    
    await runTest(
        '获取活动列表',
        'GET',
        '/api/activity/list',
        null,
        200,
        'GET /api/activity/list - 获取活动列表接口'
    );

    await runTest(
        '获取活动详情 (ID=1)',
        'GET',
        '/api/activity/detail/1',
        null,
        200,
        'GET /api/activity/detail/{id} - 获取活动详情接口'
    );

    // 步骤 3: 测试活动发布功能
    console.log('\n【步骤 3】测试活动发布功能 (物业端)');
    
    const publishResult = await runTest(
        '发布活动',
        'POST',
        '/api/activity/publish',
        {
            title: '社区春节联欢会',
            description: '一年一度的春节联欢活动',
            posterUrl: 'https://example.com/poster.jpg',
            startTime: '2026-02-10T09:00:00',
            endTime: '2026-02-10T17:00:00',
            location: '社区活动中心',
            quota: 50
        },
        200,
        'POST /api/activity/publish - 发布活动接口'
    );

    if (publishResult && publishResult.id) {
        testActivityId = publishResult.id;
        console.log(`\n发布活动成功，活动 ID: ${testActivityId}`);
    }

    // 步骤 4: 测试活动报名功能
    console.log('\n【步骤 4】测试活动报名功能 (居民端)');
    
    if (testActivityId) {
        await runTest(
            '活动报名',
            'POST',
            `/api/activity/signup?activityId=${testActivityId}&userId=${RESIDENT_USER_ID}`,
            null,
            200,
            'POST /api/activity/signup - 活动报名接口'
        );

        await runTest(
            '检查是否已报名',
            'GET',
            `/api/activity/check-signup?activityId=${testActivityId}&userId=${RESIDENT_USER_ID}`,
            null,
            200,
            'GET /api/activity/check-signup - 检查报名状态接口'
        );

        await runTest(
            '获取用户报名记录',
            'GET',
            `/api/activity/user-signups/${RESIDENT_USER_ID}`,
            null,
            200,
            'GET /api/activity/user-signups/{userId} - 获取用户报名记录接口'
        );
    }

    // 步骤 5: 测试活动互动功能
    console.log('\n【步骤 5】测试活动互动功能 (居民端)');
    
    if (testActivityId) {
        await runTest(
            '上传活动照片',
            'POST',
            `/api/activity/upload-photo?activityId=${testActivityId}&userId=${RESIDENT_USER_ID}&photoUrl=https://example.com/photo1.jpg`,
            null,
            200,
            'POST /api/activity/upload-photo - 上传活动照片接口'
        );

        await runTest(
            '获取活动照片列表',
            'GET',
            `/api/activity/photos/${testActivityId}`,
            null,
            200,
            'GET /api/activity/photos/{activityId} - 获取活动照片接口'
        );

        await runTest(
            '发表活动评价',
            'POST',
            `/api/activity/add-comment?activityId=${testActivityId}&userId=${RESIDENT_USER_ID}&content=活动很有趣，组织得很好！&rating=5`,
            null,
            200,
            'POST /api/activity/add-comment - 发表活动评价接口'
        );

        await runTest(
            '获取活动评价列表',
            'GET',
            `/api/activity/comments/${testActivityId}`,
            null,
            200,
            'GET /api/activity/comments/{activityId} - 获取活动评价接口'
        );
    }

    // 步骤 6: 测试物业端报名管理功能
    console.log('\n【步骤 6】测试物业端报名管理功能');
    
    const signupPageResult = await runTest(
        '分页查看报名记录',
        'GET',
        '/api/admin/activity-check/page?page=1&size=10',
        null,
        200,
        'GET /api/admin/activity-check/page - 分页查看报名记录接口'
    );

    // 步骤 7: 测试重复报名
    console.log('\n【步骤 7】测试重复报名 (应失败)');
    
    if (testActivityId) {
        await runTest(
            '重复报名测试',
            'POST',
            `/api/activity/signup?activityId=${testActivityId}&userId=${RESIDENT_USER_ID}`,
            null,
            500,
            '重复报名应该被拒绝'
        );
    }

    // 步骤 8: 名额限制测试说明
    console.log('\n【步骤 8】测试名额限制功能');
    console.log('说明：名额限制测试需要创建小名额活动并多次报名');
    console.log('测试设计：创建名额为 2 的活动，然后尝试 3 次报名');
    console.log('✓ 测试场景设计完成');

    // 输出测试报告
    console.log('');
    console.log('========================================');
    console.log('           测试报告');
    console.log('========================================');
    
    console.log('\n测试统计:');
    console.log(`  总测试用例数：${totalTests}`);
    console.log(`  通过用例数：${passedTests}`);
    console.log(`  失败用例数：${failedTests}`);
    if (totalTests > 0) {
        console.log(`  通过率：${Math.round(passedTests / totalTests * 100 * 100) / 100}%`);
    }

    console.log('\n详细测试结果:');
    console.table(testResults);

    console.log(`\n测试完成时间：${new Date().toLocaleString('zh-CN')}`);

    // 导出 CSV 报告
    const fs = require('fs');
    const csvHeader = '测试名称，方法，端点，期望状态，实际状态，结果\n';
    const csvRows = testResults.map(r => 
        `"${r.testName}","${r.method}","${r.endpoint}",${r.expectedStatus},${r.actualStatus},"${r.result}"`
    ).join('\n');
    
    const csvContent = csvHeader + csvRows;
    const reportPath = 'c:\\Users\\fengchunzhao\\Desktop\\future-community-system\\backend\\activity-module-test-report.csv';
    fs.writeFileSync(reportPath, csvContent, 'utf8');
    console.log(`\n测试报告已导出至：${reportPath}`);
}

// 执行测试
runAllTests().catch(console.error);
