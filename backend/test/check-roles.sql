SELECT ur.user_id, ur.role_id, r.name as role_name
FROM user_role ur
JOIN role r ON ur.role_id = r.id
WHERE ur.user_id = 23;
