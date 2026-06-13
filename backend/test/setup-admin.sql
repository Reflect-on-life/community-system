-- 创建管理员用户
INSERT INTO user (name, phone, password, type, gender, status, created_at) 
VALUES ('系统管理员', '13999999999', '$2a$10$N.zOVGHt9qXJzW3Q4N5Z5.7u5qXJzW3Q4N5Z5.7u5qXJzW3Q4N5Z5', 'admin', 'male', 1, NOW())
ON DUPLICATE KEY UPDATE name = '系统管理员';

-- 分配管理员角色
INSERT INTO user_role (user_id, role_id) 
SELECT id, 1 FROM user WHERE phone = '13999999999'
ON DUPLICATE KEY UPDATE role_id = 1;

-- 验证
SELECT u.id, u.name, u.phone, r.name as role_name 
FROM user u 
JOIN user_role ur ON u.id = ur.user_id 
JOIN role r ON ur.role_id = r.id 
WHERE u.phone = '13999999999';
