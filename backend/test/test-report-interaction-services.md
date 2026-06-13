
# 互动服务模块测试报告

## 测试概要

- **测试开始时间**: 2026/3/7 16:09:56
- **测试结束时间**: 2026/3/7 16:09:57
- **测试总耗时**: 0.58 秒
- **测试用例总数**: 37
- **通过用例数**: 28
- **失败用例数**: 9
- **通过率**: 75.68%

## 模块测试结果

### 1. 报修服务模块
- 通过：7
- 失败：3

### 2. 投诉建议模块
- 通过：7
- 失败：0

### 3. 邻里交流模块
- 通过：4
- 失败：4

### 4. 风险上报模块
- 通过：9
- 失败：2

### 5. 满意度统计模块
- 通过：1
- 失败：0

## 详细测试结果

| 序号 | 模块 | 测试用例 | 状态 | 耗时 (ms) | 错误信息 |
|------|------|----------|------|-----------|----------|
| 1 | repair | 居民提交报修申请 | ✅ | 16 | - |
| 2 | repair | 居民获取自己的报修列表 | ✅ | 12 | - |
| 3 | repair | 物业获取所有报修列表 | ✅ | 12 | - |
| 4 | repair | 物业按状态查询报修 | ✅ | 11 | - |
| 5 | repair | 物业分配维修师傅 | ✅ | 20 | - |
| 6 | repair | 维修师傅开始维修 | ❌ | 21 | Request failed with status code 500 |
| 7 | repair | 维修师傅完成维修 | ❌ | 23 | Request failed with status code 500 |
| 8 | repair | 居民评价服务 | ❌ | 24 | Request failed with status code 500 |
| 9 | repair | 居民取消待处理的报修 | ✅ | 35 | - |
| 10 | repair | 非认证居民不能提交报修 | ✅ | 0 | - |
| 11 | complaints | 居民提交投诉 | ✅ | 17 | - |
| 12 | complaints | 居民查询自己的投诉 | ✅ | 8 | - |
| 13 | complaints | 物业分页查询投诉 | ✅ | 8 | - |
| 14 | complaints | 物业按状态筛选投诉 | ✅ | 9 | - |
| 15 | complaints | 物业回复投诉 | ✅ | 15 | - |
| 16 | complaints | 居民获取投诉回复 | ✅ | 9 | - |
| 17 | complaints | 物业关闭投诉 | ✅ | 15 | - |
| 18 | neighborMessages | 发布邻里消息 | ✅ | 14 | - |
| 19 | neighborMessages | 获取帖子列表 | ❌ | 2 | Request failed with status code 403 |
| 20 | neighborMessages | 获取帖子详情 | ❌ | 4 | Request failed with status code 403 |
| 21 | neighborMessages | 发表评论 | ✅ | 19 | - |
| 22 | neighborMessages | 获取评论列表 | ❌ | 4 | Request failed with status code 403 |
| 23 | neighborMessages | 点赞帖子 | ✅ | 24 | - |
| 24 | neighborMessages | 取消点赞 | ✅ | 19 | - |
| 25 | neighborMessages | 检查是否已点赞 | ❌ | 4 | Request failed with status code 403 |
| 26 | risk | 上报风险事件 | ❌ | 24 | Request failed with status code 500 |
| 27 | risk | AI 图片识别 | ✅ | 27 | - |
| 28 | risk | 获取风险列表 | ✅ | 7 | - |
| 29 | risk | 按状态筛选风险 | ✅ | 8 | - |
| 30 | risk | 按风险等级筛选 | ✅ | 9 | - |
| 31 | risk | 处理风险事件 | ❌ | 0 | 风险 ID 不存在，无法测试处理 |
| 32 | risk | 获取风险等级统计 | ✅ | 8 | - |
| 33 | risk | 获取风险趋势统计 | ✅ | 7 | - |
| 34 | risk | 获取风险类型统计 | ✅ | 8 | - |
| 35 | risk | 获取待处理风险数量 | ✅ | 9 | - |
| 36 | risk | 获取综合统计 | ✅ | 15 | - |
| 37 | satisfaction | 获取满意度统计 | ✅ | 4 | - |

## 测试结论

⚠️ 存在 9 个失败的测试用例，需要修复后重新测试。

## 建议

建议：优先修复失败的测试用例，确保核心功能正常后再进行其他测试。

---
**报告生成时间**: 2026/3/7 16:09:57
