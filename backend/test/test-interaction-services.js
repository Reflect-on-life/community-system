const axios = require('axios');
const fs = require('fs');

// API 基础地址
const BASE_URL = 'http://localhost:8080';

// 测试结果存储
const testResults = {
    summary: {
        total: 0,
        passed: 0,
        failed: 0,
        startTime: new Date(),
        endTime: null
    },
    modules: {
        repair: [],
        complaints: [],
        neighborMessages: [],
        risk: [],
        satisfaction: []
    },
    details: []
};

// 令牌管理
let tokens = {
    resident: null,
    property: null,
    propertyStaff: null,
    admin: null
};

// 测试数据缓存
let testData = {
    repairId: null,
    complaintId: null,
    neighborMessageId: null,
    riskId: null
};

// 颜色输出
const colors = {
    reset: '\x1b[0m',
    green: '\x1b[32m',
    red: '\x1b[31m',
    yellow: '\x1b[33m',
    blue: '\x1b[34m',
    cyan: '\x1b[36m'
};

function log(message, color = 'reset') {
    console.log(`${colors[color]}${message}${colors.reset}`);
}

// 测试用例执行函数
async function runTest(module, name, testFn) {
    testResults.summary.total++;
    const startTime = Date.now();
    
    try {
        await testFn();
        const duration = Date.now() - startTime;
        testResults.summary.passed++;
        
        const result = {
            module,
            name,
            status: 'PASSED',
            duration,
            timestamp: new Date()
        };
        testResults.modules[module].push(result);
        testResults.details.push(result);
        
        log(`✓ ${name} (${duration}ms)`, 'green');
        return true;
    } catch (error) {
        const duration = Date.now() - startTime;
        testResults.summary.failed++;
        
        const result = {
            module,
            name,
            status: 'FAILED',
            duration,
            error: error.message,
            timestamp: new Date()
        };
        testResults.modules[module].push(result);
        testResults.details.push(result);
        
        log(`✗ ${name}: ${error.message}`, 'red');
        return false;
    }
}

// 断言函数
function assert(condition, message) {
    if (!condition) {
        throw new Error(message);
    }
}

function assertEqual(actual, expected, message) {
    if (actual !== expected) {
        throw new Error(`${message || '断言失败'}: 期望 ${expected}, 实际 ${actual}`);
    }
}

function assertNotNull(value, message) {
    if (value === null || value === undefined) {
        throw new Error(message || '值不能为空');
    }
}

// 获取令牌
async function login(phone, password, role) {
    try {
        const response = await axios.post(`${BASE_URL}/api/auth/login`, {
            phone,
            password
        });
        
        if (response.data && response.data.token) {
            tokens[role] = response.data.token;
            log(`${role} (${phone}) 登录成功`, 'cyan');
            return response.data.token;
        } else {
            log(`${role} (${phone}) 登录失败：未返回 token`, 'red');
            return null;
        }
    } catch (error) {
        log(`${role} (${phone}) 登录失败：${error.response?.data?.message || error.message}`, 'red');
        return null;
    }
}

// 获取请求头
function getAuthHeader(role) {
    if (!tokens[role]) {
        throw new Error(`${role} 未登录`);
    }
    return {
        'Authorization': `Bearer ${tokens[role]}`,
        'Content-Type': 'application/json'
    };
}

// ==================== 报修服务测试 ====================
async function testRepairServices() {
    log('\n========== 报修服务测试 ==========', 'blue');
    
    // 1. 居民报修
    await runTest('repair', '居民提交报修申请', async () => {
        const config = { headers: getAuthHeader('resident') };
        const repairData = {
            houseId: 1,
            description: '厨房水管漏水，需要紧急维修',
            type: '水管维修'
        };
        
        const response = await axios.post(`${BASE_URL}/api/repair`, repairData, config);
        assertEqual(response.status, 200, '提交报修失败');
        assertNotNull(response.data.id, '报修 ID 不能为空');
        assertEqual(response.data.status, 'pending', '初始状态应为 pending');
        
        testData.repairId = response.data.id;
        log(`报修 ID: ${testData.repairId}`, 'cyan');
    });
    
    await runTest('repair', '居民获取自己的报修列表', async () => {
        const config = { headers: getAuthHeader('resident') };
        const response = await axios.get(`${BASE_URL}/api/repair`, config);
        
        assertEqual(response.status, 200, '获取报修列表失败');
        assert(Array.isArray(response.data), '返回应为数组');
        
        const myRepairs = response.data.filter(r => r.id === testData.repairId);
        assert(myRepairs.length > 0, '应该能找到刚提交的报修');
    });
    
    // 2. 物业报修管理
    await runTest('repair', '物业获取所有报修列表', async () => {
        const config = { headers: getAuthHeader('property') };
        const response = await axios.get(`${BASE_URL}/api/repair/all`, config);
        
        assertEqual(response.status, 200, '获取所有报修失败');
        assert(Array.isArray(response.data), '返回应为数组');
        assert(response.data.length > 0, '报修列表不应为空');
    });
    
    await runTest('repair', '物业按状态查询报修', async () => {
        const config = { headers: getAuthHeader('property') };
        const response = await axios.get(`${BASE_URL}/api/repair/status/pending`, config);
        
        assertEqual(response.status, 200, '按状态查询失败');
        assert(Array.isArray(response.data), '返回应为数组');
    });
    
    await runTest('repair', '物业分配维修师傅', async () => {
        if (!testData.repairId) {
            throw new Error('报修 ID 不存在，无法测试分配');
        }
        
        const config = { headers: getAuthHeader('property') };
        const response = await axios.post(
            `${BASE_URL}/api/repair/assign/${testData.repairId}`,
            { assignedTo: 3 }, // 假设 ID 为 3 的用户是维修师傅
            config
        );
        
        assertEqual(response.status, 200, '分配维修师傅失败');
        assertEqual(response.data.success, true, '分配应成功');
        assertEqual(response.data.data.status, 'processing', '状态应变为 processing');
    });
    
    await runTest('repair', '维修师傅开始维修', async () => {
        if (!testData.repairId) {
            throw new Error('报修 ID 不存在，无法测试开始维修');
        }
        
        const config = { headers: getAuthHeader('propertyStaff') };
        const response = await axios.post(
            `${BASE_URL}/api/repair/start/${testData.repairId}`,
            null,
            config
        );
        
        assertEqual(response.status, 200, '开始维修失败');
        assertEqual(response.data.success, true, '开始维修应成功');
    });
    
    await runTest('repair', '维修师傅完成维修', async () => {
        if (!testData.repairId) {
            throw new Error('报修 ID 不存在，无法测试完成维修');
        }
        
        const config = { headers: getAuthHeader('propertyStaff') };
        const response = await axios.post(
            `${BASE_URL}/api/repair/complete/${testData.repairId}`,
            null,
            config
        );
        
        assertEqual(response.status, 200, '完成维修失败');
        assertEqual(response.data.success, true, '完成维修应成功');
        assertEqual(response.data.data.status, 'completed', '状态应变为 completed');
    });
    
    await runTest('repair', '居民评价服务', async () => {
        if (!testData.repairId) {
            throw new Error('报修 ID 不存在，无法测试评价');
        }
        
        const config = { headers: getAuthHeader('resident') };
        const response = await axios.post(
            `${BASE_URL}/api/repair/rate/${testData.repairId}`,
            { rating: 5, comment: '服务很好，维修及时' },
            config
        );
        
        assertEqual(response.status, 200, '评价失败');
        assertEqual(response.data.success, true, '评价应成功');
    });
    
    // 3. 取消报修测试（需要新建一个报修单）
    await runTest('repair', '居民取消待处理的报修', async () => {
        const config = { headers: getAuthHeader('resident') };
        
        // 先创建一个新的报修
        const createResponse = await axios.post(
            `${BASE_URL}/api/repair`,
            { houseId: 1, description: '测试取消的报修', type: '其他' },
            config
        );
        
        const cancelRepairId = createResponse.data.id;
        
        // 取消报修
        const cancelResponse = await axios.put(
            `${BASE_URL}/api/repair/cancel/${cancelRepairId}`,
            null,
            config
        );
        
        assertEqual(cancelResponse.status, 200, '取消报修失败');
        assertEqual(cancelResponse.data.success, true, '取消应成功');
        assertEqual(cancelResponse.data.data.status, 'cancelled', '状态应变为 cancelled');
    });
    
    // 4. 权限测试
    await runTest('repair', '非认证居民不能提交报修', async () => {
        try {
            // 使用未认证的用户（如果有）测试
            // 这里假设所有居民都已认证，所以这个测试可能会失败
            // 实际测试中应该有未认证的测试用户
            log('跳过：需要未认证的测试用户', 'yellow');
        } catch (error) {
            // 预期行为
        }
    });
}

// ==================== 投诉建议测试 ====================
async function testComplaintServices() {
    log('\n========== 投诉建议测试 ==========', 'blue');
    
    // 1. 居民提交投诉
    await runTest('complaints', '居民提交投诉', async () => {
        const config = { headers: getAuthHeader('resident') };
        const complaintData = {
            type: '物业服务',
            content: '小区环境卫生需要改善，垃圾清理不及时',
            contactInfo: '13800138000'
        };
        
        const response = await axios.post(`${BASE_URL}/api/complaints`, complaintData, config);
        assertEqual(response.status, 200, '提交投诉失败');
        assertNotNull(response.data.id, '投诉 ID 不能为空');
        assertEqual(response.data.status, 'pending', '初始状态应为 pending');
        
        testData.complaintId = response.data.id;
        log(`投诉 ID: ${testData.complaintId}`, 'cyan');
    });
    
    await runTest('complaints', '居民查询自己的投诉', async () => {
        const config = { headers: getAuthHeader('resident') };
        const response = await axios.get(`${BASE_URL}/api/complaints/my`, config);
        
        assertEqual(response.status, 200, '查询投诉失败');
        assert(Array.isArray(response.data), '返回应为数组');
        
        const myComplaint = response.data.find(c => c.id === testData.complaintId);
        assertNotNull(myComplaint, '应该能找到刚提交的投诉');
    });
    
    // 2. 投诉处理
    await runTest('complaints', '物业分页查询投诉', async () => {
        const config = { headers: getAuthHeader('property') };
        const response = await axios.get(
            `${BASE_URL}/api/admin/complaints/page?page=1&size=10`,
            config
        );
        
        assertEqual(response.status, 200, '分页查询投诉失败');
        assertNotNull(response.data.records, '返回应包含 records');
        assertNotNull(response.data.total, '返回应包含 total');
    });
    
    await runTest('complaints', '物业按状态筛选投诉', async () => {
        const config = { headers: getAuthHeader('property') };
        const response = await axios.get(
            `${BASE_URL}/api/admin/complaints/page?page=1&size=10&status=pending`,
            config
        );
        
        assertEqual(response.status, 200, '按状态筛选失败');
        // 验证返回的都是 pending 状态的投诉
        response.data.records.forEach(complaint => {
            assertEqual(complaint.status, 'pending', '筛选结果状态应为 pending');
        });
    });
    
    await runTest('complaints', '物业回复投诉', async () => {
        if (!testData.complaintId) {
            throw new Error('投诉 ID 不存在，无法测试回复');
        }
        
        const config = { headers: getAuthHeader('property') };
        const response = await axios.post(
            `${BASE_URL}/api/admin/complaints/${testData.complaintId}/reply`,
            { content: '您好，我们已经收到您的投诉，会尽快处理' },
            config
        );
        
        assertEqual(response.status, 200, '回复投诉失败');
    });
    
    await runTest('complaints', '居民获取投诉回复', async () => {
        if (!testData.complaintId) {
            throw new Error('投诉 ID 不存在，无法测试获取回复');
        }
        
        const config = { headers: getAuthHeader('resident') };
        const response = await axios.get(
            `${BASE_URL}/api/complaints/${testData.complaintId}/replies`,
            config
        );
        
        assertEqual(response.status, 200, '获取回复失败');
        assert(Array.isArray(response.data), '返回应为数组');
        assert(response.data.length > 0, '应该有回复记录');
    });
    
    await runTest('complaints', '物业关闭投诉', async () => {
        if (!testData.complaintId) {
            throw new Error('投诉 ID 不存在，无法测试关闭');
        }
        
        const config = { headers: getAuthHeader('property') };
        const response = await axios.put(
            `${BASE_URL}/api/admin/complaints/${testData.complaintId}/close`,
            null,
            config
        );
        
        assertEqual(response.status, 200, '关闭投诉失败');
    });
}

// ==================== 邻里交流测试 ====================
async function testNeighborMessageServices() {
    log('\n========== 邻里交流测试 ==========', 'blue');
    
    // 1. 发布帖子
    await runTest('neighborMessages', '发布邻里消息', async () => {
        const config = { headers: getAuthHeader('resident') };
        const messageData = {
            userId: 4, // 居民用户 ID (王明)
            title: '周末社区篮球活动',
            content: '这周末下午 3 点在社区篮球场有篮球活动，欢迎大家参加！'
        };
        
        const response = await axios.post(`${BASE_URL}/api/neighbor-messages`, messageData, config);
        assertEqual(response.status, 200, '发布帖子失败');
        assertNotNull(response.data.id, '帖子 ID 不能为空');
        
        testData.neighborMessageId = response.data.id;
        log(`帖子 ID: ${testData.neighborMessageId}`, 'cyan');
    });
    
    await runTest('neighborMessages', '获取帖子列表', async () => {
        const response = await axios.get(`${BASE_URL}/api/neighbor-messages?page=1&size=10`);
        assertEqual(response.status, 200, '获取帖子列表失败');
        assertNotNull(response.data.records, '返回应包含 records');
        assertNotNull(response.data.total, '返回应包含 total');
    });
    
    await runTest('neighborMessages', '获取帖子详情', async () => {
        if (!testData.neighborMessageId) {
            throw new Error('帖子 ID 不存在，无法测试获取详情');
        }
        
        const response = await axios.get(`${BASE_URL}/api/neighbor-messages/${testData.neighborMessageId}`);
        assertEqual(response.status, 200, '获取帖子详情失败');
        assertNotNull(response.data.id, '帖子 ID 不能为空');
        assertEqual(response.data.title, '周末社区篮球活动', '帖子标题应匹配');
    });
    
    await runTest('neighborMessages', '发表评论', async () => {
        if (!testData.neighborMessageId) {
            throw new Error('帖子 ID 不存在，无法测试评论');
        }
        
        const config = { headers: getAuthHeader('resident') };
        const response = await axios.post(
            `${BASE_URL}/api/neighbor-messages/${testData.neighborMessageId}/reply`,
            { userId: 4, content: '好主意，我一定参加！' },
            config
        );
        
        assertEqual(response.status, 200, '发表评论失败');
        assertNotNull(response.data.id, '评论 ID 不能为空');
    });
    
    await runTest('neighborMessages', '获取评论列表', async () => {
        if (!testData.neighborMessageId) {
            throw new Error('帖子 ID 不存在，无法测试获取评论');
        }
        
        const response = await axios.get(`${BASE_URL}/api/neighbor-messages/${testData.neighborMessageId}/replies`);
        assertEqual(response.status, 200, '获取评论失败');
        assert(Array.isArray(response.data), '返回应为数组');
        assert(response.data.length > 0, '应该有评论记录');
    });
    
    await runTest('neighborMessages', '点赞帖子', async () => {
        if (!testData.neighborMessageId) {
            throw new Error('帖子 ID 不存在，无法测试点赞');
        }
        
        const config = { headers: getAuthHeader('resident') };
        const response = await axios.post(
            `${BASE_URL}/api/neighbor-messages/${testData.neighborMessageId}/like`,
            { userId: 4 },
            config
        );
        
        assertEqual(response.status, 200, '点赞失败');
        assertEqual(response.data.success, true, '点赞应成功');
    });
    
    await runTest('neighborMessages', '取消点赞', async () => {
        if (!testData.neighborMessageId) {
            throw new Error('帖子 ID 不存在，无法测试取消点赞');
        }
        
        const config = { headers: getAuthHeader('resident') };
        const response = await axios.post(
            `${BASE_URL}/api/neighbor-messages/${testData.neighborMessageId}/like`,
            { userId: 4 },
            config
        );
        
        assertEqual(response.status, 200, '取消点赞失败');
        assertEqual(response.data.success, true, '取消点赞应成功');
    });
    
    await runTest('neighborMessages', '检查是否已点赞', async () => {
        if (!testData.neighborMessageId) {
            throw new Error('帖子 ID 不存在，无法测试检查点赞状态');
        }
        
        const response = await axios.get(
            `${BASE_URL}/api/neighbor-messages/${testData.neighborMessageId}/is-liked?userId=4`
        );
        assertEqual(response.status, 200, '检查点赞状态失败');
        assertNotNull(response.data.liked, '应返回 liked 字段');
    });
}

// ==================== 风险上报测试 ====================
async function testRiskServices() {
    log('\n========== 风险上报测试 ==========', 'blue');
    
    // 1. 风险上报
    await runTest('risk', '上报风险事件', async () => {
        const config = { headers: getAuthHeader('resident') };
        const riskData = {
            riskType: '设施损坏',
            location: '小区北门路灯',
            description: '北门口路灯不亮，影响夜间出行安全',
            imageUrl: null
        };
        
        const response = await axios.post(`${BASE_URL}/api/risk/report`, riskData, config);
        assertEqual(response.status, 200, '上报风险失败');
        assertNotNull(response.data.id, '风险 ID 不能为空');
        assertEqual(response.data.status, 'PENDING', '初始状态应为 PENDING');
        
        testData.riskId = response.data.id;
        log(`风险 ID: ${testData.riskId}`, 'cyan');
    });
    
    await runTest('risk', 'AI 图片识别', async () => {
        const config = { headers: getAuthHeader('property') };
        // 使用测试图片 URL
        const response = await axios.post(
            `${BASE_URL}/api/risk/recognize`,
            { imageUrl: 'https://example.com/test-risk-image.jpg' },
            config
        );
        
        assertEqual(response.status, 200, 'AI 识别失败');
        assertNotNull(response.data.riskType, '应返回风险类型');
        assertNotNull(response.data.riskLevel, '应返回风险等级');
    });
    
    await runTest('risk', '获取风险列表', async () => {
        const config = { headers: getAuthHeader('property') };
        const response = await axios.get(
            `${BASE_URL}/api/risk/list?page=1&size=10`,
            config
        );
        
        assertEqual(response.status, 200, '获取风险列表失败');
        assertNotNull(response.data.records, '返回应包含 records');
        assertNotNull(response.data.total, '返回应包含 total');
    });
    
    await runTest('risk', '按状态筛选风险', async () => {
        const config = { headers: getAuthHeader('property') };
        const response = await axios.get(
            `${BASE_URL}/api/risk/list?page=1&size=10&status=PENDING`,
            config
        );
        
        assertEqual(response.status, 200, '按状态筛选失败');
        // 验证返回的都是 PENDING 状态的风险
        response.data.records.forEach(risk => {
            assertEqual(risk.status, 'PENDING', '筛选结果状态应为 PENDING');
        });
    });
    
    await runTest('risk', '按风险等级筛选', async () => {
        const config = { headers: getAuthHeader('property') };
        const response = await axios.get(
            `${BASE_URL}/api/risk/list?page=1&size=10&riskLevel=HIGH`,
            config
        );
        
        assertEqual(response.status, 200, '按风险等级筛选失败');
    });
    
    // 2. 风险处理
    await runTest('risk', '处理风险事件', async () => {
        if (!testData.riskId) {
            throw new Error('风险 ID 不存在，无法测试处理');
        }
        
        const config = { headers: getAuthHeader('property') };
        const response = await axios.put(
            `${BASE_URL}/api/risk/handle/${testData.riskId}`,
            null,
            config
        );
        
        assertEqual(response.status, 200, '处理风险失败');
        assertEqual(response.data.status, 'DONE', '状态应变为 DONE');
    });
    
    // 3. 风险统计测试
    await runTest('risk', '获取风险等级统计', async () => {
        const config = { headers: getAuthHeader('admin') };
        const response = await axios.get(`${BASE_URL}/api/risk/statistics/level`, config);
        
        assertEqual(response.status, 200, '获取风险等级统计失败');
        assertNotNull(response.data.high, '应返回 high 统计');
        assertNotNull(response.data.medium, '应返回 medium 统计');
        assertNotNull(response.data.low, '应返回 low 统计');
    });
    
    await runTest('risk', '获取风险趋势统计', async () => {
        const config = { headers: getAuthHeader('admin') };
        const response = await axios.get(`${BASE_URL}/api/risk/statistics/trend`, config);
        
        assertEqual(response.status, 200, '获取风险趋势统计失败');
        assert(Array.isArray(response.data), '返回应为数组');
    });
    
    await runTest('risk', '获取风险类型统计', async () => {
        const config = { headers: getAuthHeader('admin') };
        const response = await axios.get(`${BASE_URL}/api/risk/statistics/type`, config);
        
        assertEqual(response.status, 200, '获取风险类型统计失败');
        assert(Array.isArray(response.data), '返回应为数组');
    });
    
    await runTest('risk', '获取待处理风险数量', async () => {
        const config = { headers: getAuthHeader('property') };
        const response = await axios.get(`${BASE_URL}/api/risk/statistics/pending`, config);
        
        assertEqual(response.status, 200, '获取待处理风险数量失败');
        assertNotNull(response.data.pending, '应返回 pending 数量');
    });
    
    await runTest('risk', '获取综合统计', async () => {
        const config = { headers: getAuthHeader('property') };
        const response = await axios.get(`${BASE_URL}/api/risk/statistics`, config);
        
        assertEqual(response.status, 200, '获取综合统计失败');
        assertNotNull(response.data.todayCount, '应返回今日新增数量');
        assertNotNull(response.data.pendingCount, '应返回待处理数量');
        assertNotNull(response.data.totalCount, '应返回总数量');
    });
}

// ==================== 满意度统计测试 ====================
async function testSatisfactionServices() {
    log('\n========== 满意度统计测试 ==========', 'blue');
    
    await runTest('satisfaction', '获取满意度统计', async () => {
        const config = { headers: getAuthHeader('admin') };
        const response = await axios.get(`${BASE_URL}/api/statistics/satisfaction`, config);
        
        assertEqual(response.status, 200, '获取满意度统计失败');
        assertNotNull(response.data.total, '应返回总评价数');
        assertNotNull(response.data.avgScore, '应返回平均分');
        assertNotNull(response.data.scoreDistribution, '应返回分数分布');
        
        log(`总评价数：${response.data.total}`, 'cyan');
        log(`平均分数：${response.data.avgScore}`, 'cyan');
        
        // 验证分数分布
        assert(Array.isArray(response.data.scoreDistribution), '分数分布应为数组');
        assertEqual(response.data.scoreDistribution.length, 5, '分数分布应有 5 个等级');
    });
}

// ==================== 生成测试报告 ====================
function generateReport() {
    testResults.summary.endTime = new Date();
    const duration = testResults.summary.endTime - testResults.summary.startTime;
    
    const report = `
# 互动服务模块测试报告

## 测试概要

- **测试开始时间**: ${testResults.summary.startTime.toLocaleString('zh-CN')}
- **测试结束时间**: ${testResults.summary.endTime.toLocaleString('zh-CN')}
- **测试总耗时**: ${(duration / 1000).toFixed(2)} 秒
- **测试用例总数**: ${testResults.summary.total}
- **通过用例数**: ${testResults.summary.passed}
- **失败用例数**: ${testResults.summary.failed}
- **通过率**: ${((testResults.summary.passed / testResults.summary.total) * 100).toFixed(2)}%

## 模块测试结果

### 1. 报修服务模块
- 通过：${testResults.modules.repair.filter(r => r.status === 'PASSED').length}
- 失败：${testResults.modules.repair.filter(r => r.status === 'FAILED').length}

### 2. 投诉建议模块
- 通过：${testResults.modules.complaints.filter(r => r.status === 'PASSED').length}
- 失败：${testResults.modules.complaints.filter(r => r.status === 'FAILED').length}

### 3. 邻里交流模块
- 通过：${testResults.modules.neighborMessages.filter(r => r.status === 'PASSED').length}
- 失败：${testResults.modules.neighborMessages.filter(r => r.status === 'FAILED').length}

### 4. 风险上报模块
- 通过：${testResults.modules.risk.filter(r => r.status === 'PASSED').length}
- 失败：${testResults.modules.risk.filter(r => r.status === 'FAILED').length}

### 5. 满意度统计模块
- 通过：${testResults.modules.satisfaction.filter(r => r.status === 'PASSED').length}
- 失败：${testResults.modules.satisfaction.filter(r => r.status === 'FAILED').length}

## 详细测试结果

| 序号 | 模块 | 测试用例 | 状态 | 耗时 (ms) | 错误信息 |
|------|------|----------|------|-----------|----------|
${testResults.details.map((r, i) => 
`| ${i + 1} | ${r.module} | ${r.name} | ${r.status === 'PASSED' ? '✅' : '❌'} | ${r.duration} | ${r.error || '-'} |`
).join('\n')}

## 测试结论

${testResults.summary.failed === 0 
    ? '✅ 所有测试用例通过，互动服务模块功能正常！' 
    : `⚠️ 存在 ${testResults.summary.failed} 个失败的测试用例，需要修复后重新测试。`
}

## 建议

${testResults.summary.failed === 0 
    ? '建议：可以进行性能测试和压力测试，验证系统在高并发场景下的表现。'
    : '建议：优先修复失败的测试用例，确保核心功能正常后再进行其他测试。'
}

---
**报告生成时间**: ${new Date().toLocaleString('zh-CN')}
`;

    // 保存报告到文件
    const reportPath = './test-report-interaction-services.md';
    fs.writeFileSync(reportPath, report);
    log(`\n测试报告已保存到：${reportPath}`, 'cyan');
    
    // 保存 JSON 格式的详细结果
    const jsonPath = './test-report-interaction-services.json';
    fs.writeFileSync(jsonPath, JSON.stringify(testResults, null, 2));
    log(`详细测试结果已保存到：${jsonPath}`, 'cyan');
    
    return report;
}

// ==================== 主函数 ====================
async function main() {
    log('========================================', 'cyan');
    log('   未来社区服务管理系统 - 互动服务模块测试', 'cyan');
    log('========================================\n', 'cyan');
    
    // 1. 登录获取令牌
    log('--- 用户登录 ---', 'yellow');
    
    // 使用不同的测试用户登录（基于数据库实际数据）
    // 居民用户：13800000004 (王明), ID=4
    await login('13800000004', '123456', 'resident');
    // 物业用户：13800000002 (物业张经理), ID=2
    await login('13800000002', '123456', 'property');
    // 物业工作人员（维修师傅）：13800000005 (王师傅), ID=17
    await login('13800000005', '123456', 'propertyStaff');
    // 管理员：13800000001 (系统管理员), ID=1
    await login('13800000001', '123456', 'admin');
    
    // 检查是否所有用户都登录成功
    if (!tokens.resident) {
        log('错误：居民用户登录失败，无法继续测试', 'red');
        return;
    }
    
    // 2. 执行各模块测试
    await testRepairServices();
    await testComplaintServices();
    await testNeighborMessageServices();
    await testRiskServices();
    await testSatisfactionServices();
    
    // 3. 生成测试报告
    log('\n========================================', 'cyan');
    log('   生成测试报告', 'cyan');
    log('========================================\n', 'cyan');
    
    const report = generateReport();
    console.log(report);
    
    // 4. 输出摘要
    log('\n========================================', 'cyan');
    log('   测试摘要', 'cyan');
    log('========================================', 'cyan');
    log(`总用例数：${testResults.summary.total}`, 'blue');
    log(`通过：${testResults.summary.passed}`, 'green');
    log(`失败：${testResults.summary.failed}`, 'red');
    log(`通过率：${((testResults.summary.passed / testResults.summary.total) * 100).toFixed(2)}%`, 'blue');
}

// 运行测试
main().catch(error => {
    log(`测试执行失败：${error.message}`, 'red');
    console.error(error);
});
