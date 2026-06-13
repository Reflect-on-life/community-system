const axios = require('axios');

async function testLogin() {
    const users = [
        { phone: '13800000001', password: '123456', role: 'admin' },
        { phone: '13800000002', password: '123456', role: 'property' },
        { phone: '13800000004', password: '123456', role: 'resident' },
        { phone: '13800000005', password: '123456', role: 'property_staff' }
    ];
    
    for (const user of users) {
        try {
            const response = await axios.post('http://localhost:8080/api/auth/login', {
                phone: user.phone,
                password: user.password
            });
            
            console.log(`${user.role} (${user.phone}) 登录成功:`, response.data.token ? '有 token' : '无 token');
        } catch (error) {
            console.log(`${user.role} (${user.phone}) 登录失败:`, error.response?.data?.message || error.message);
        }
    }
}

testLogin();
