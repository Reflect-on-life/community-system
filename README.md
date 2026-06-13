# 🏘️ 未来社区服务管理系统

基于 Spring Boot + Vue 3 的智慧社区综合管理平台，集成 AI 视觉识别、风险事件监测、健康管理、活动组织等功能。

## ✨ 核心功能

- **🔍 AI 安防监控** — 摄像头实时行为识别，支持摔倒检测、火灾识别、打架斗殴、垃圾堆积等 7 类风险
- **🎛️ 多模型可切换** — 三套 AI 方案自由选择：原版(GLM-4V+百度AI) / 省钱(YOLOv11+Qwen-VL) / 最强(YOLOv11+Gemini 2.5 Flash)
- **⚠️ 风险事件管理** — 自动创建风险工单、实时 WebSocket 告警推送、事件处理闭环
- **🏥 健康管理** — 居民健康档案、体检预约、慢病管理、健康宣教
- **📋 社区服务** — 报修工单、快递管理、活动组织、邻里互动、通知公告
- **📊 数据大屏** — 物业 Dashboard、满意度统计、养老数据看板

## 🛠️ 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.0 + MyBatis-Plus 3.5 |
| 前端框架 | Vue 3 + Vite + Element Plus |
| 数据库 | MySQL 8.0 + Redis |
| AI 视觉 | GLM-4V / Qwen-VL / Gemini 2.5 Flash + YOLOv11 |
| 实时通信 | WebSocket (STOMP) |
| 安全认证 | Spring Security + JWT |

## 🚀 快速启动

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.8+

### 1. 克隆项目

```bash
git clone https://github.com/your-username/future-community-system.git
cd future-community-system
```

### 2. 配置环境变量

```bash
cp .env.example .env
# 编辑 .env 填入你的 API Key
```

需要配置的 API Key：

| 变量 | 说明 | 获取地址 |
|------|------|----------|
| `BAIDU_AI_*` | 百度 AI 图像分类 | [百度智能云](https://console.bce.baidu.com/ai/) |
| `GLM_API_KEY` | 智谱 GLM-4V | [智谱开放平台](https://open.bigmodel.cn/) |
| `QWEN_VL_API_KEY` | 通义千问视觉（可选） | [阿里云 DashScope](https://dashscope.aliyun.com/) |
| `GEMINI_API_KEY` | Google Gemini（可选） | [Google AI Studio](https://aistudio.google.com/) |

> 💡 至少配置百度 AI + 智谱 GLM 即可运行**原版模式**，其他两个方案按需配置。

### 3. 初始化数据库

```bash
mysql -u root -p < test/future_community.sql
```

### 4. 启动后端

```bash
cd backend
mvn spring-boot:run
```

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173

### 6.（可选）启动 YOLO 检测服务

使用省钱版或最强版方案时，需要额外启动 YOLO 本地检测服务：

```bash
pip install ultralytics flask pillow
python test/yolo_service.py
```

## 🎛️ 模型方案切换

系统提供三套 AI 模型组合，在摄像头监控页面顶部可实时切换：

| 方案 | 检测层 | VLM 视觉理解 | 特点 |
|------|--------|-------------|------|
| **原版** (ORIGINAL) | 百度 AI | GLM-4V | 默认方案，稳定可靠 |
| **最省钱** (ECONOMY) | YOLOv11 本地 | Qwen-VL | 国产 VLM，成本最低 |
| **效果最好** (PREMIUM) | YOLOv11 本地 | Gemini 2.5 Flash | 最强视觉理解 |

## 📁 项目结构

```
future-community-system/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/.../
│   │   ├── config/             # 配置（含模型预设策略）
│   │   ├── controller/         # REST API 控制器
│   │   ├── service/            # 服务层（含 AI 识别）
│   │   ├── model/              # 数据模型
│   │   └── mapper/             # MyBatis Mapper
│   └── pom.xml
├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── views/              # 页面组件
│   │   ├── api/                # API 封装
│   │   └── router/             # 路由
│   └── package.json
├── test/                       # 测试脚本 & SQL
├── .env.example                # 环境变量模板
└── .gitignore
```

## 📄 License

MIT © 2025
