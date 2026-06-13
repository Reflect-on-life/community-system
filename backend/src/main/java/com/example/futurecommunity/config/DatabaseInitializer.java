package com.example.futurecommunity.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            // 检查 risk_event 表是否存在
            if (!tableExists(metaData, "risk_event")) {
                return; // 表不存在，跳过
            }
            
            // 检查并添加缺失的字段
            addColumnIfNotExists(conn, "risk_event", "glm_result", "TEXT");
            addColumnIfNotExists(conn, "risk_event", "baidu_labels", "TEXT");
            addColumnIfNotExists(conn, "risk_event", "remark", "TEXT");
            addColumnIfNotExists(conn, "risk_event", "location", "VARCHAR(255)");
            
            System.out.println("数据库表结构检查完成");
        }
    }

    private boolean tableExists(DatabaseMetaData metaData, String tableName) throws Exception {
        ResultSet tables = metaData.getTables(null, null, tableName, new String[]{"TABLE"});
        boolean exists = tables.next();
        tables.close();
        return exists;
    }

    private boolean columnExists(DatabaseMetaData metaData, String tableName, String columnName) throws Exception {
        ResultSet columns = metaData.getColumns(null, null, tableName, columnName);
        boolean exists = columns.next();
        columns.close();
        return exists;
    }

    private void addColumnIfNotExists(Connection conn, String tableName, String columnName, String columnType) throws Exception {
        DatabaseMetaData metaData = conn.getMetaData();
        if (!columnExists(metaData, tableName, columnName)) {
            try (var stmt = conn.createStatement()) {
                String sql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType;
                stmt.executeUpdate(sql);
                System.out.println("已添加字段：" + columnName + " 到表：" + tableName);
            }
        }
    }
}
