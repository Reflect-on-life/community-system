-- 手动创建 activity 表
USE future_community;

-- 活动表
CREATE TABLE IF NOT EXISTS `activity`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '活动 ID，主键，自增',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '活动标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '活动描述',
  `poster_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '活动海报 URL',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '活动开始时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '活动结束时间',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '活动地点',
  `quota` int(0) NULL DEFAULT NULL COMMENT '活动名额限制',
  `enrolled_count` int(0) NULL DEFAULT 0 COMMENT '已报名人数',
  `status` tinyint(0) NULL DEFAULT 0 COMMENT '活动状态：0-未开始，1-进行中，2-已结束',
  `created_at` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动表';

-- 活动报名表
CREATE TABLE IF NOT EXISTS `activity_signup`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '报名 ID，主键，自增',
  `activity_id` bigint(0) NULL DEFAULT NULL COMMENT '活动 ID，关联 activity 表',
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '用户 ID，关联 user 表',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '报名状态：pending-待确认，confirmed-已确认，cancelled-已取消',
  `signup_time` datetime(0) NULL DEFAULT NULL COMMENT '报名时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_activity_id` (`activity_id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动报名表';

-- 活动照片表
CREATE TABLE IF NOT EXISTS `activity_photo`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '照片 ID，主键，自增',
  `activity_id` bigint(0) NULL DEFAULT NULL COMMENT '活动 ID，关联 activity 表',
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '上传者 ID，关联 user 表',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '照片 URL 地址',
  `uploaded_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '上传时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_activity_id` (`activity_id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动照片表';

-- 活动评价表
CREATE TABLE IF NOT EXISTS `activity_comment`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '评论 ID，主键，自增',
  `activity_id` bigint(0) NULL DEFAULT NULL COMMENT '活动 ID，关联 activity 表',
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '用户 ID，关联 user 表',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评论内容',
  `rating` int(0) NULL DEFAULT NULL COMMENT '评分，1-5 分',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '评论时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_activity_id` (`activity_id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动评价表';

-- 插入测试数据
INSERT INTO `activity` (`title`, `description`, `poster_url`, `start_time`, `end_time`, `location`, `quota`, `enrolled_count`, `status`, `created_at`, `updated_at`) 
VALUES 
('社区周末瑜伽', '欢迎各位邻居参加本周六的公益瑜伽课。', 'https://example.com/poster/yoga.jpg', '2026-03-15 15:00:00', '2026-03-15 17:00:00', '社区健身中心', 50, 0, 0, NOW(), NOW()),
('春季健康体检', '社区组织免费体检活动，欢迎报名。', 'https://example.com/poster/health.jpg', '2026-03-20 09:00:00', '2026-03-20 17:00:00', '社区医院', 100, 0, 0, NOW(), NOW());

-- 查看表结构
SHOW TABLES LIKE 'activity%';
