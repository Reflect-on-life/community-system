const http = require('http');

const BASE_URL = 'http://localhost:8080';

// 测试账号（从数据库确认）
const TEST_USERS = [
    { phone: '13800000001', password: '123456', name: '管理员', userId: 1 },
    { phone: '13800000002', password: '123456', name: '物业用户', userId: 2 },
    { phone: '13800000004', password: '123456', name: '居民用户', userId: 4 }
];

let tokens = {
    admin: null,
    resident: null,
    property: null
};

let totalTests = 0;
let passedTests = 0;
const results = [];

function request(method, endpoint, token, body = null) {
    return new Promise((resolve, reject) => {
        const url = new URL(endpoint, BASE_URL);
        const options = {
            hostname: url.hostname,
            port: url.port || 8080,
            path: url.pathname + url.search,
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token ? `Bearer ${token}` : ''
            }
        };

        const req = http.request(options, (res) => {
            let data = '';
            res.on('data', (chunk) => data += chunk);
            res.on('end', () => {
                try {
                    resolve({
                        statusCode: res.statusCode,
                        data: data ? JSON.parse(data) : null,
                        raw: data
                    });
                } catch (e) {
                    resolve({
                        statusCode: res.statusCode,
                        data: null,
                        raw: data
                    });
                }
            });
        });

        req.on('error', reject);
        
        if (body) {
            req.write(JSON.stringify(body));
        }
        req.end();
    });
}

async function login(phone, password) {
    try {
        const result = await request('POST', '/api/auth/login', null, { phone, password });
        if (result.statusCode === 200 && result.data && result.data.token) {
            return result.data.token;
        }
        console.log(`  登录响应：${result.statusCode} - ${result.raw ? result.raw.substring(0, 100) : '无响应'}`);
        return null;
    } catch (error) {
        console.error(`登录失败：${error.message}`);
        return null;
    }
}

async function runTest(name, method, endpoint, token, expectedStatus, body = null, description = '') {
    totalTests++;
    try {
        const result = await request(method, endpoint, token, body);
        const passed = result.statusCode === expectedStatus;
        if (passed) passedTests++;
        
        console.log(`[${passed ? 'PASS' : 'FAIL'}] ${name}`);
        console.log(`  接口：${method} ${endpoint}`);
        console.log(`  预期：${expectedStatus}, 实际：${result.statusCode}`);
        if (description) console.log(`  说明：${description}`);
        if (!passed && result.raw) {
            console.log(`  响应：${result.raw.substring(0, 200)}`);
        }
        console.log('');
        
        results.push({
            test: name,
            method: method,
            endpoint: endpoint,
            expected: expectedStatus,
            actual: result.statusCode,
            passed: passed,
            description: description
        });
        
        return result;
    } catch (error) {
        totalTests--;
        console.log(`[ERROR] ${name}`);
        console.log(`  错误：${error.message}`);
        console.log('');
        results.push({
            test: name,
            method: method,
            endpoint: endpoint,
            expected: expectedStatus,
            actual: 'ERROR',
            passed: false,
            description: error.message
        });
        return null;
    }
}

async function runTests() {
    console.log('========================================');
    console.log('  未来社区服务管理系统 - API 功能测试');
    console.log(`  测试时间：${new Date().toLocaleString('zh-CN')}`);
    console.log('========================================');
    console.log('');
    
    // 第一步：登录获取 token
    console.log('=== 准备阶段：登录获取 Token ===');
    console.log('');
    
    for (const user of TEST_USERS) {
        console.log(`尝试登录：${user.name} (${user.phone})`);
        const token = await login(user.phone, user.password);
        if (token) {
            if (user.userId === 1) {
                tokens.admin = token;
                console.log(`[PASS] ${user.name}登录成功`);
            } else if (user.userId === 4) {
                tokens.resident = token;
                console.log(`[PASS] ${user.name}登录成功`);
            } else if (user.userId === 2) {
                tokens.property = token;
                console.log(`[PASS] ${user.name}登录成功`);
            }
        } else {
            console.log(`[FAIL] ${user.name}登录失败`);
        }
    }
    
    console.log('');
    console.log(`Token 获取情况:`);
    console.log(`  管理员：${tokens.admin ? '✓' : '✗'}`);
    console.log(`  居民：${tokens.resident ? '✓' : '✗'}`);
    console.log(`  物业：${tokens.property ? '✓' : '✗'}`);
    console.log('');
    
    const useResidentToken = tokens.resident || null;
    const usePropertyToken = tokens.property || tokens.admin || null;
    
    if (!useResidentToken || !usePropertyToken) {
        console.log('错误：无法获取必要的测试 token');
        console.log(`  需要居民 token: ${!useResidentToken ? '✗' : '✓'}`);
        console.log(`  需要物业 token: ${!usePropertyToken ? '✗' : '✓'}`);
        return;
    }
    
    console.log('=== 第一部分：房产管理模块测试 ===');
    console.log('');
    
    console.log('--- 测试场景 1: 居民端房产认证 ---');
    console.log('');
    
    await runTest(
        '居民提交房产认证申请',
        'POST',
        '/api/resident-house/apply',
        useResidentToken,
        200,
        { houseId: 1, relation: '业主' },
        '居民用户提交房产认证申请'
    );
    
    await runTest(
        '重复提交认证申请',
        'POST',
        '/api/resident-house/apply',
        useResidentToken,
        200,
        { houseId: 1, relation: '业主' },
        '同一用户对同一房产重复提交申请'
    );
    
    console.log('--- 测试场景 2: 物业端审核功能 ---');
    console.log('');
    
    await runTest(
        '物业获取待审核申请列表',
        'GET',
        '/api/resident-house/pending',
        usePropertyToken,
        200,
        null,
        '获取所有待审核的认证申请'
    );
    
    await runTest(
        '物业审核通过申请',
        'PUT',
        '/api/resident-house/approve?userId=4&houseId=1',
        usePropertyToken,
        200,
        null,
        '审核通过房产认证申请'
    );
    
    await runTest(
        '物业获取已审核记录',
        'GET',
        '/api/resident-house/processed',
        usePropertyToken,
        200,
        null,
        '获取已审核通过的认证记录'
    );
    
    await runTest(
        '物业撤回认证',
        'PUT',
        '/api/resident-house/revoke?userId=4&houseId=1',
        usePropertyToken,
        200,
        null,
        '撤回已通过的认证'
    );
    
    // 创建一个新的申请用于拒绝测试
    await request('POST', '/api/resident-house/apply', useResidentToken, { houseId: 2, relation: '租户' });
    await runTest(
        '物业拒绝认证申请',
        'DELETE',
        '/api/resident-house/reject?userId=4&houseId=2',
        usePropertyToken,
        200,
        null,
        '拒绝房产认证申请'
    );
    
    console.log('--- 测试场景 3: 楼栋和房屋管理 ---');
    console.log('');
    
    await runTest(
        '获取楼栋列表',
        'GET',
        '/api/resident-house/buildings',
        usePropertyToken,
        200,
        null,
        '获取所有楼栋信息'
    );
    
    await runTest(
        '根据楼栋 ID 获取房屋列表',
        'GET',
        '/api/resident-house/houses/1',
        usePropertyToken,
        200,
        null,
        '获取指定楼栋的所有房屋'
    );
    
    await runTest(
        '获取所有房屋列表',
        'GET',
        '/api/resident-house/houses/all',
        usePropertyToken,
        200,
        null,
        '获取所有房屋信息'
    );
    
    await runTest(
        '物业新增房产',
        'POST',
        '/api/resident-house',
        usePropertyToken,
        200,
        { buildingId: 1, houseNo: '1001', status: 0 },
        '新增房产记录'
    );
    
    await runTest(
        '物业修改房产信息',
        'PUT',
        '/api/resident-house/1',
        usePropertyToken,
        200,
        { buildingId: 1, houseNo: '1001', status: 1 },
        '修改房产信息'
    );
    
    await runTest(
        '物业删除房产',
        'DELETE',
        '/api/resident-house/1',
        usePropertyToken,
        200,
        null,
        '删除房产记录'
    );
    
    console.log('--- 测试场景 4: 数据隔离测试 ---');
    console.log('');
    
    await runTest(
        '居民查看自己的房产',
        'GET',
        '/api/resident-house/user/4',
        useResidentToken,
        200,
        null,
        '居民用户查看自己的房产列表'
    );
    
    await runTest(
        '居民查看他人房产 (权限测试)',
        'GET',
        '/api/resident-house/user/1',
        useResidentToken,
        403,
        null,
        '居民用户尝试查看其他用户的房产（应被拒绝）'
    );
    
    await runTest(
        '物业查看所有房产',
        'GET',
        '/api/resident-house/all',
        usePropertyToken,
        200,
        null,
        '物业用户查看全社区所有房产'
    );
    
    await runTest(
        '检查用户认证状态',
        'GET',
        '/api/resident-house/certified?userId=4',
        usePropertyToken,
        200,
        null,
        '检查用户是否已认证房产'
    );
    
    console.log('=== 第二部分：快递服务模块测试 ===');
    console.log('');
    
    console.log('--- 测试场景 1: 快递入库 ---');
    console.log('');
    
    await runTest(
        '物业快递入库',
        'POST',
        '/api/express/inbound',
        usePropertyToken,
        200,
        { trackingNo: 'YT123456789', userId: 4, status: 0 },
        '快递入库，自动生成取件码'
    );
    
    await runTest(
        '重复快递入库',
        'POST',
        '/api/express/inbound',
        usePropertyToken,
        200,
        { trackingNo: 'YT987654321', userId: 4, status: 0 },
        '另一个快递入库'
    );
    
    console.log('--- 测试场景 2: 取件功能测试 ---');
    console.log('');
    
    const listResult = await runTest(
        '获取快递列表',
        'GET',
        '/api/express',
        useResidentToken,
        200,
        null,
        '获取居民的快递列表'
    );
    
    if (listResult && listResult.data && listResult.data.length > 0) {
        const pickupCode = listResult.data[0].pickupCode;
        console.log(`  使用取件码：${pickupCode}`);
        await runTest(
            '居民取件核销',
            'POST',
            `/api/express/pickup/${pickupCode}`,
            useResidentToken,
            200,
            null,
            '使用取件码核销快递'
        );
    } else {
        console.log('[SKIP] 居民取件核销 - 没有可取件的快递');
        console.log('');
        totalTests++;
        results.push({
            test: '居民取件核销',
            method: 'POST',
            endpoint: '/api/express/pickup/{code}',
            expected: 200,
            actual: 'SKIP',
            passed: false,
            description: '没有可取件的快递'
        });
    }
    
    await runTest(
        '无效取件码取件',
        'POST',
        '/api/express/pickup/INVALID999',
        useResidentToken,
        500,
        null,
        '使用无效取件码尝试取件（应返回错误）'
    );
    
    console.log('--- 测试场景 3: 快递列表查询 ---');
    console.log('');
    
    await runTest(
        '居民查看自己的快递列表',
        'GET',
        '/api/express',
        useResidentToken,
        200,
        null,
        '居民用户查看自己的快递'
    );
    
    await runTest(
        '物业查看所有快递列表',
        'GET',
        '/api/express',
        usePropertyToken,
        200,
        null,
        '物业用户查看所有快递'
    );
    
    console.log('--- 测试场景 4: 数据隔离测试 ---');
    console.log('');
    
    // 为另一个用户创建快递
    await request('POST', '/api/express/inbound', usePropertyToken, { trackingNo: 'YT555666777', userId: 1, status: 0 });
    
    const residentExpressResult = await request('GET', '/api/express', useResidentToken);
    let hasOtherUserExpress = false;
    if (residentExpressResult.data) {
        hasOtherUserExpress = residentExpressResult.data.some(e => e.userId !== 4);
    }
    
    totalTests++;
    const dataIsolationPassed = !hasOtherUserExpress;
    if (dataIsolationPassed) passedTests++;
    console.log(`[${dataIsolationPassed ? 'PASS' : 'FAIL'}] 居民数据隔离验证`);
    console.log(`  验证居民用户只能看到自己的快递`);
    if (!dataIsolationPassed) {
        console.log(`  失败：居民看到了其他用户的快递`);
    }
    console.log('');
    results.push({
        test: '居民数据隔离验证',
        method: 'GET',
        endpoint: '/api/express',
        expected: '只能看到自己的快递',
        actual: dataIsolationPassed ? '通过' : '失败',
        passed: dataIsolationPassed,
        description: '验证居民用户只能看到自己的快递'
    });
    
    const propertyExpressResult = await request('GET', '/api/express', usePropertyToken);
    let hasUserId4 = false;
    let hasUserId1 = false;
    if (propertyExpressResult.data) {
        hasUserId4 = propertyExpressResult.data.some(e => e.userId === 4);
        hasUserId1 = propertyExpressResult.data.some(e => e.userId === 1);
    }
    
    totalTests++;
    const propertyDataIsolationPassed = hasUserId4 && hasUserId1;
    if (propertyDataIsolationPassed) passedTests++;
    console.log(`[${propertyDataIsolationPassed ? 'PASS' : 'FAIL'}] 物业数据隔离验证`);
    console.log(`  验证物业用户可以看到所有快递（包括 userId=4 和 userId=1 的快递）`);
    if (!propertyDataIsolationPassed) {
        console.log(`  失败：物业未看到所有快递 (userId=4: ${hasUserId4}, userId=1: ${hasUserId1})`);
    }
    console.log('');
    results.push({
        test: '物业数据隔离验证',
        method: 'GET',
        endpoint: '/api/express',
        expected: '可以看到所有快递',
        actual: propertyDataIsolationPassed ? '通过' : '失败',
        passed: propertyDataIsolationPassed,
        description: '验证物业用户可以看到所有快递'
    });
    
    // 测试总结
    console.log('========================================');
    console.log('  测试执行完成');
    console.log('========================================');
    console.log('');
    console.log('测试统计:');
    console.log(`  总测试用例数：${totalTests}`);
    console.log(`  通过用例数：${passedTests}`);
    console.log(`  失败用例数：${totalTests - passedTests}`);
    const passRate = ((passedTests / totalTests) * 100).toFixed(2);
    console.log(`  通过率：${passRate}%`);
    console.log('');
    
    // 导出测试报告
    const fs = require('fs');
    const reportPath = 'c:\\Users\\fengchunzhao\\Desktop\\future-community-system\\backend\\test-report-final-' + new Date().toISOString().replace(/[:.]/g, '-') + '.json';
    fs.writeFileSync(reportPath, JSON.stringify({
        summary: {
            total: totalTests,
            passed: passedTests,
            failed: totalTests - passedTests,
            passRate: passRate + '%',
            timestamp: new Date().toISOString(),
            tokens: {
                admin: tokens.admin ? '✓' : '✗',
                resident: tokens.resident ? '✓' : '✗',
                property: tokens.property ? '✓' : '✗'
            }
        },
        results: results
    }, null, 2));
    console.log(`测试报告已导出至：${reportPath}`);
    console.log('');
}

runTests().catch(console.error);
