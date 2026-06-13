package com.example.futurecommunity.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

/**
 * 活动表数据库初始化器
 * 自动创建 activity 相关表
 */
@Component
public class ActivityTableInitializer implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== 开始初始化活动模块数据库表 ===");
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            // 检查并创建 activity 表
            if (!tableExists(metaData, "activity")) {
                System.out.println("正在创建 activity 表...");
                createActivityTable(conn);
                System.out.println("已创建 activity 表");
            } else {
                System.out.println("activity 表已存在");
            }
            
            // 检查并创建 activity_signup 表
            if (!tableExists(metaData, "activity_signup")) {
                System.out.println("正在创建 activity_signup 表...");
                createActivitySignupTable(conn);
                System.out.println("已创建 activity_signup 表");
            } else {
                System.out.println("activity_signup 表已存在");
            }
            
            // 检查并创建 activity_photo 表
            if (!tableExists(metaData, "activity_photo")) {
                System.out.println("正在创建 activity_photo 表...");
                createActivityPhotoTable(conn);
                System.out.println("已创建 activity_photo 表");
            } else {
                System.out.println("activity_photo 表已存在");
            }
            
            // 检查并创建 activity_comment 表
            if (!tableExists(metaData, "activity_comment")) {
                System.out.println("正在创建 activity_comment 表...");
                createActivityCommentTable(conn);
                System.out.println("已创建 activity_comment 表");
            } else {
                System.out.println("activity_comment 表已存在");
            }

            // 检查并创建 activity_checkin 表
            if (!tableExists(metaData, "activity_checkin")) {
                System.out.println("正在创建 activity_checkin 表...");
                createActivityCheckinTable(conn);
                System.out.println("已创建 activity_checkin 表");
            } else {
                System.out.println("activity_checkin 表已存在");
            }
            
            System.out.println("=== 活动模块数据库表初始化完成 ===");
        } catch (Exception e) {
            System.err.println("初始化活动模块数据库表失败：" + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private boolean tableExists(DatabaseMetaData metaData, String tableName) throws Exception {
        ResultSet tables = metaData.getTables(null, null, tableName, new String[]{"TABLE"});
        boolean exists = tables.next();
        tables.close();
        return exists;
    }

    private void createActivityTable(Connection conn) throws Exception {
        String sql = """
            CREATE TABLE activity (
                id bigint NOT NULL AUTO_INCREMENT COMMENT '活动 ID，主键，自增',
                title varchar(100) DEFAULT NULL COMMENT '活动标题',
                description text COMMENT '活动描述',
                poster_url varchar(255) DEFAULT NULL COMMENT '活动海报 URL',
                start_time datetime DEFAULT NULL COMMENT '活动开始时间',
                end_time datetime DEFAULT NULL COMMENT '活动结束时间',
                location varchar(100) DEFAULT NULL COMMENT '活动地点',
                quota int DEFAULT NULL COMMENT '活动名额限制',
                enrolled_count int DEFAULT 0 COMMENT '已报名人数',
                status tinyint DEFAULT 0 COMMENT '活动状态：0-未开始，1-进行中，2-已结束',
                created_at datetime DEFAULT NULL COMMENT '创建时间',
                updated_at datetime DEFAULT NULL COMMENT '更新时间',
                PRIMARY KEY (id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动表'
        """;
        try (var stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private void createActivitySignupTable(Connection conn) throws Exception {
        String sql = """
            CREATE TABLE activity_signup (
                id bigint NOT NULL AUTO_INCREMENT COMMENT '报名 ID，主键，自增',
                activity_id bigint DEFAULT NULL COMMENT '活动 ID，关联 activity 表',
                user_id bigint DEFAULT NULL COMMENT '用户 ID，关联 user 表',
                status varchar(20) DEFAULT NULL COMMENT '报名状态：pending-待确认，confirmed-已确认，cancelled-已取消',
                signup_time datetime DEFAULT NULL COMMENT '报名时间',
                PRIMARY KEY (id),
                INDEX idx_activity_id (activity_id),
                INDEX idx_user_id (user_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动报名表'
        """;
        try (var stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private void createActivityPhotoTable(Connection conn) throws Exception {
        String sql = """
            CREATE TABLE activity_photo (
                id bigint NOT NULL AUTO_INCREMENT COMMENT '照片 ID，主键，自增',
                activity_id bigint DEFAULT NULL COMMENT '活动 ID，关联 activity 表',
                user_id bigint DEFAULT NULL COMMENT '上传者 ID，关联 user 表',
                url varchar(255) DEFAULT NULL COMMENT '照片 URL 地址',
                uploaded_at datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
                PRIMARY KEY (id),
                INDEX idx_activity_id (activity_id),
                INDEX idx_user_id (user_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动照片表'
        """;
        try (var stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private void createActivityCommentTable(Connection conn) throws Exception {
        String sql = """
            CREATE TABLE activity_comment (
                id bigint NOT NULL AUTO_INCREMENT COMMENT '评论 ID，主键，自增',
                activity_id bigint DEFAULT NULL COMMENT '活动 ID，关联 activity 表',
                user_id bigint DEFAULT NULL COMMENT '用户 ID，关联 user 表',
                content varchar(255) DEFAULT NULL COMMENT '评论内容',
                rating int DEFAULT NULL COMMENT '评分，1-5 分',
                created_at datetime DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
                PRIMARY KEY (id),
                INDEX idx_activity_id (activity_id),
                INDEX idx_user_id (user_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动评价表'
        """;
        try (var stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private void createActivityCheckinTable(Connection conn) throws Exception {
        String sql = """
            CREATE TABLE activity_checkin (
                id bigint NOT NULL AUTO_INCREMENT COMMENT '签到 ID，主键，自增',
                activity_id bigint DEFAULT NULL COMMENT '活动 ID，关联 activity 表',
                user_id bigint DEFAULT NULL COMMENT '用户 ID，关联 user 表',
                status tinyint DEFAULT 0 COMMENT '签到状态：0-未签到，1-已签到',
                checkin_time datetime DEFAULT NULL COMMENT '签到时间',
                created_at datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                PRIMARY KEY (id),
                INDEX idx_activity_id (activity_id),
                INDEX idx_user_id (user_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动签到表'
        """;
        try (var stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

}
