@echo off
echo Creating test user...
mysql -u root -proot future_community -e "DELETE FROM user WHERE phone = '13899999999';"
mysql -u root -proot future_community -e "INSERT INTO user (name, phone, password, type, gender, status, created_at) VALUES ('Test User', '13899999999', '$2a$10$N.zOVGHt9qXJzW3Q4N5Z5.7u5qXJzW3Q4N5Z5.7u5qXJzW3Q4N5Z5', 'resident', 'F', 1, NOW());"
mysql -u root -proot future_community -e "INSERT INTO user_role (user_id, role_id) SELECT id, 4 FROM user WHERE phone = '13899999999';"
echo User created!
mysql -u root -proot future_community -e "SELECT u.id, u.name, u.phone, r.name as role_name FROM user u JOIN user_role ur ON u.id = ur.user_id JOIN role r ON ur.role_id = r.id WHERE u.phone = '13899999999';"
