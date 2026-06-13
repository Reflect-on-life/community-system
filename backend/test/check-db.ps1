Write-Host "Checking database tables..."

# Check role table
Write-Host "`n=== Roles Table ==="
mysql -u root -proot future_community -e "SELECT * FROM role;" 2>&1 | Write-Host

Write-Host "`n=== User Role Table ==="
mysql -u root -proot future_community -e "SELECT * FROM user_role WHERE user_id = 23;" 2>&1 | Write-Host

Write-Host "`n=== Checking operation_log table ==="
mysql -u root -proot future_community -e "SHOW TABLES LIKE 'operation_log';" 2>&1 | Write-Host
mysql -u root -proot future_community -e "DESC operation_log;" 2>&1 | Write-Host
