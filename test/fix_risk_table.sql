-- 修复 risk_event 表缺失字段
-- 请在 MySQL 命令行或数据库管理工具中执行以下 SQL

USE future_community;

-- 添加 GLM-4V 识别结果字段
ALTER TABLE risk_event ADD COLUMN IF NOT EXISTS glm_result TEXT COMMENT 'GLM-4V AI 识别结果';

-- 添加百度 AI 识别标签字段
ALTER TABLE risk_event ADD COLUMN IF NOT EXISTS baidu_labels TEXT COMMENT '百度 AI 识别标签（JSON 数组或逗号分隔）';

-- 添加处理备注字段
ALTER TABLE risk_event ADD COLUMN IF NOT EXISTS remark TEXT COMMENT '处理备注';

-- 添加风险位置字段
ALTER TABLE risk_event ADD COLUMN IF NOT EXISTS location VARCHAR(255) COMMENT '风险位置';

-- 验证表结构
DESC risk_event;
