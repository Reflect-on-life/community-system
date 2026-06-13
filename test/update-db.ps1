$env:Path = "C:\Program Files\MySQL\MySQL Server 8.0\bin;" + $env:Path
mysql -u root -proot future_community -e "ALTER TABLE risk_event ADD COLUMN ai_result TEXT"
mysql -u root -proot future_community -e "ALTER TABLE risk_event ADD COLUMN ai_confidence DOUBLE"
mysql -u root -proot future_community -e "ALTER TABLE risk_event ADD COLUMN remark VARCHAR(500)"
mysql -u root -proot future_community -e "CREATE INDEX idx_status ON risk_event(status)"
mysql -u root -proot future_community -e "CREATE INDEX idx_risk_level ON risk_event(risk_level)"
mysql -u root -proot future_community -e "CREATE INDEX idx_create_time ON risk_event(create_time)"
mysql -u root -proot future_community -e "DESC risk_event"
