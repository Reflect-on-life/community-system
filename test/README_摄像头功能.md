# 摄像头行为识别功能使用说明

> **版本**：V1.0  
> **更新日期**：2026-04-07  
> **技术栈**：SpringBoot 3.x + Vue 3 + 百度云 AI + WebSocket

---

## 📋 功能概述

本功能实现了基于百度云 AI 的实时行为识别系统，能够：

1. ✅ 连接本地摄像头进行实时监控
2. ✅ 自动识别打架、发传单等异常行为
3. ✅ 实时告警推送（WebSocket）
4. ✅ 告警记录查询和管理
5. ✅ 截图保存和查看

---

## 🚀 快速启动

### 前置条件

1. **Java 环境**：JDK 17+
2. **Node.js 环境**：Node.js 16+
3. **MySQL 数据库**：MySQL 8.0+
4. **百度云账号**：已申请人体分析 API 权限

### 1. 数据库初始化

```bash
# 1. 登录 MySQL
mysql -u root -p

# 2. 选择数据库
USE future_community;

# 3. 执行 SQL 脚本
SOURCE backend/src/main/resources/schema-alarm-record.sql;

# 4. 验证表创建成功
SHOW TABLES LIKE 'alarm_record';
```

### 2. 启动后端服务

```bash
# 进入后端目录
cd backend

# 启动服务
mvn spring-boot:run
```

**验证**：访问 `http://localhost:8080/api/alarm/list` 返回 JSON 即成功

### 3. 启动前端服务

```bash
# 进入前端目录
cd frontend

# 安装依赖（首次运行）
npm install

# 启动服务
npm run dev
```

**验证**：访问 `http://localhost:5173` 能打开页面即成功

---

## 📖 功能使用

### 1. 摄像头实时监控

**访问地址**：`http://localhost:5173/camera-monitor`

**操作步骤**：
1. 点击"开始监控"按钮
2. 允许浏览器访问摄像头
3. 系统自动每 1 秒识别一次
4. 发现异常行为自动弹窗告警

**功能说明**：
- 📹 实时视频流预览
- 🔍 自动行为识别（打架/发传单）
- 🔔 告警弹窗 + 音效
- 📋 告警历史列表
- 🔄 支持切换摄像头

### 2. 告警历史查询

**访问地址**：`http://localhost:5173/alarm/history`

**功能说明**：
- 🔍 按告警类型筛选
- 📅 按时间范围筛选
- 📊 分页浏览（10/20/50 条/页）
- 👁️ 截图预览（点击放大）
- ✅ 标记已读/批量已读

---

## ⚙️ 配置说明

### application.yml 配置项

```yaml
# 百度云 AI 配置
baidu:
  ai:
    appId: 你的 APP_ID
    apiKey: 你的 API_KEY
    secretKey: 你的 SECRET_KEY

# 行为识别检测参数配置
detection:
  interval-ms: 1000              # 前端采帧间隔（毫秒）
  fighting-iou-threshold: 0.1    # 打架判断 IoU 阈值
  min-confidence: 0.6            # 最低置信度过滤

# 告警图片保存路径
upload:
  alarm-image-path: c:/Users/你的用户名/Desktop/future-community-system/backend/upload/monitor
```

### 配置项说明

| 配置项 | 默认值 | 说明 | 调整建议 |
|--------|--------|------|---------|
| `detection.interval-ms` | 1000 | 前端采帧间隔（毫秒） | 百度 API QPS=1，建议≥1000ms |
| `detection.fighting-iou-threshold` | 0.1 | 打架判断 IoU 阈值 | 越小越敏感，建议 0.05-0.2 |
| `detection.min-confidence` | 0.6 | 最低置信度过滤 | 越大越严格，建议 0.5-0.8 |
| `upload.alarm-image-path` | - | 告警图片保存路径 | 确保目录可写 |

---

## ⚠️ 注意事项

### 1. 百度 API 限制

**免费额度**：
- QPS 限制：1 次/秒
- 日限额：500 次/天

**解决方案**：
- ✅ 前端采帧间隔设置为 1000ms 以上
- ✅ 后端已实现 QPS 限流
- ✅ 并发请求自动跳过

### 2. 摄像头权限

**首次使用需要授权**：
- 浏览器会弹出权限请求对话框
- 点击"允许"才能访问摄像头
- 如误点"拒绝"，需在浏览器设置中重新允许

**Chrome 权限设置**：
```
设置 → 隐私和安全 → 网站设置 → 摄像头
```

### 3. HTTPS 要求

**开发环境**：
- ✅ `http://localhost` 可以使用摄像头

**生产环境**：
- ⚠️ 必须使用 HTTPS 才能访问摄像头

### 4. 图片访问

**确保静态资源映射正确**：
```yaml
# WebConfig.java 已配置
/monitor/** → backend/upload/monitor/
```

**前端访问图片**：
```
http://localhost:8080/monitor/alarm_20260407_143022_1234.jpg
```

### 5. 跨域配置

**已配置的跨域来源**：
- ✅ `http://localhost:5173`（Vite 默认端口）
- ✅ `http://localhost:3000`（其他前端端口）

**生产环境建议**：
```java
.allowedOriginPatterns("https://your-domain.com")
```

---

## 🧪 测试场景

### 场景 1：正常监控

1. 打开摄像头监控页面
2. 点击"开始监控"
3. 观察视频流是否正常
4. 检查状态显示"监控中"

### 场景 2：行为识别测试

**测试打架行为**：
1. 在摄像头前放置两个人体模型（或两人靠近）
2. 模拟重叠动作
3. 观察是否弹出"打架"告警
4. 检查告警列表是否更新

**测试发传单行为**：
1. 在摄像头前模拟两人手部交叉动作
2. 观察是否弹出"发传单"告警

### 场景 3：告警查询测试

1. 打开告警历史页面
2. 选择告警类型"打架"
3. 选择时间范围
4. 点击"查询"
5. 验证筛选结果

### 场景 4：WebSocket 测试

1. 打开浏览器开发者工具 → Network → WS
2. 点击"开始监控"
3. 检查 WebSocket 连接是否建立
4. 触发告警
5. 检查是否收到 `/topic/alarm` 消息

---

## 🐛 常见问题

### Q1: 摄像头无法打开

**可能原因**：
- 浏览器不支持
- 权限被拒绝
- 摄像头被其他程序占用

**解决方案**：
1. 检查浏览器版本（Chrome 80+）
2. 在浏览器设置中允许摄像头权限
3. 关闭其他使用摄像头的程序

### Q2: 识别失败

**可能原因**：
- 百度 API Key 配置错误
- 网络问题
- QPS 超限

**解决方案**：
1. 检查 `application.yml` 中百度配置
2. 检查网络连接
3. 查看后端日志确认错误信息

### Q3: 告警图片无法访问

**可能原因**：
- 静态资源映射配置错误
- 图片保存路径不存在
- 跨域配置问题

**解决方案**：
1. 检查 `WebConfig.java` 配置
2. 确保 `upload.alarm-image-path` 目录存在
3. 检查浏览器控制台是否有跨域错误

### Q4: WebSocket 连接失败

**可能原因**：
- 后端 WebSocket 未启动
- 端口被占用
- 防火墙阻止

**解决方案**：
1. 检查后端日志确认 WebSocket 已启动
2. 检查 8080 端口是否被占用
3. 关闭防火墙或添加例外

---

## 📊 性能优化建议

### 1. 降低识别频率

```yaml
# 调整为 1.5 秒识别一次
detection:
  interval-ms: 1500
```

### 2. 调整识别阈值

```yaml
# 提高打架判断阈值（降低误报）
detection:
  fighting-iou-threshold: 0.15
  
# 提高置信度过滤（降低误报）
detection:
  min-confidence: 0.7
```

### 3. 优化图片质量

```javascript
// CameraMonitor.vue 中调整压缩质量
const base64Image = canvas.toDataURL('image/jpeg', 0.6) // 降低质量
```

---

## 📞 技术支持

**问题反馈**：
- 查看后端日志：`backend/logs/`
- 查看前端控制台：F12 → Console
- 检查数据库连接
- 验证百度 API 配置

**日志位置**：
- 后端日志：`backend/src/main/resources/logs/`
- 前端日志：浏览器 Console

---

## 📝 更新日志

### V1.0 (2026-04-07)

- ✅ 初始版本发布
- ✅ 支持打架行为识别
- ✅ 支持发传单行为识别
- ✅ WebSocket 实时告警推送
- ✅ 告警历史查询和管理
- ✅ 截图保存和查看

---

*文档创建时间：2026-04-07*  
*版本：V1.0*  
*最后更新：2026-04-07*
