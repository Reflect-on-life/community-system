-- 检查当前角色
SELECT * FROM user_role WHERE user_id = 23;

-- 删除现有角色
DELETE FROM user_role WHERE user_id = 23;

-- 检查删除后
SELECT * FROM user_role WHERE user_id = 23;

-- 添加管理员角色
INSERT INTO user_role (user_id, role_id) VALUES (23, 1);

-- 最终验证
SELECT * FROM user_role WHERE user_id = 23;
