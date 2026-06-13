const https = require('https');

// 百度 AI 配置 —— 请通过环境变量设置，不要硬编码
const APP_ID = process.env.BAIDU_AI_APP_ID || 'your-baidu-app-id';
const API_KEY = process.env.BAIDU_AI_API_KEY || 'your-baidu-api-key';
const SECRET_KEY = process.env.BAIDU_AI_SECRET_KEY || 'your-baidu-secret-key';

// 获取 access_token
function getAccessToken() {
  return new Promise((resolve, reject) => {
    const options = {
      hostname: 'aip.baidubce.com',
      path: `/oauth/2.0/token?grant_type=client_credentials&client_id=${API_KEY}&client_secret=${SECRET_KEY}`,
      method: 'POST'
    };

    const req = https.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => data += chunk);
      res.on('end', () => {
        const token = JSON.parse(data).access_token;
        resolve(token);
      });
    });

    req.on('error', reject);
    req.end();
  });
}
