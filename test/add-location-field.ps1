$env:Path = "C:\Program Files\MySQL\MySQL Server 8.0\bin;" + $env:Path
mysql -u root -proot future_community -e "ALTER TABLE risk_event ADD COLUMN location VARCHAR(100) COMMENT '风险发生位置'"
mysql -u root -proot future_community -e "DESC risk_event"
