-- 查询所有 property 类型的用户
SELECT id, name, phone, type, status FROM users WHERE type = 'property' ORDER BY id;

-- 查询维修技能关系
SELECT tsr.user_id, u.name, ts.id as skill_id, ts.name as skill_name 
FROM technician_skill_relation tsr
LEFT JOIN users u ON tsr.user_id = u.id
LEFT JOIN technician_skill ts ON tsr.skill_id = ts.id
ORDER BY tsr.user_id;
