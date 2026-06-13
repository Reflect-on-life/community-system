/*
 Navicat Premium Data Transfer

 Source Server         : 我的MySQL
 Source Server Type    : MySQL
 Source Server Version : 80043
 Source Host           : localhost:3306
 Source Schema         : future_community

 Target Server Type    : MySQL
 Target Server Version : 80043
 File Encoding         : 65001

 Date: 15/04/2026 16:06:52
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity
-- ----------------------------
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `poster_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `start_time` datetime(0) NULL DEFAULT NULL,
  `end_time` datetime(0) NULL DEFAULT NULL,
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `quota` int(0) NULL DEFAULT NULL,
  `enrolled_count` int(0) NULL DEFAULT 0,
  `status` tinyint(0) NULL DEFAULT 0,
  `created_at` datetime(0) NULL DEFAULT NULL,
  `updated_at` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 571 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity
-- ----------------------------
INSERT INTO `activity` VALUES (566, '社区周末瑜伽课', '欢迎各位邻居参加本周六的公益瑜伽课，专业教练指导，适合各年龄段。请自带瑜伽垫。', 'https://images.unsplash.com/photo-1544367563-12123d8965cd?w=800&h=600&fit=crop&q=80', '2026-03-15 09:00:00', '2026-03-15 10:30:00', '社区健身中心一楼', 30, 12, 0, '2026-04-15 14:58:50', '2026-04-15 14:58:50');
INSERT INTO `activity` VALUES (567, '春季亲子运动会', '春季亲子运动会开始报名啦！有趣味接力、拔河比赛等项目，欢迎家长带小朋友一起参加。', 'https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=800&h=600&fit=crop&q=80', '2026-03-16 14:00:00', '2026-03-16 17:00:00', '社区中心广场', 50, 28, 0, '2026-04-15 14:58:50', '2026-04-15 14:58:50');
INSERT INTO `activity` VALUES (568, '健康养生讲座', '邀请知名中医专家为大家讲解春季养生知识，现场还有免费把脉问诊。', 'https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=800&h=600&fit=crop&q=80', '2026-03-18 19:00:00', '2026-03-18 20:30:00', '社区会议室', 100, 67, 0, '2026-04-15 14:58:50', '2026-04-15 14:58:50');
INSERT INTO `activity` VALUES (569, '社区读书分享会', '每月一度的读书分享会，本期主题：我最喜欢的一本书。欢迎大家带上自己的好书来分享。', 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=800&h=600&fit=crop&q=80', '2026-03-22 15:00:00', '2026-03-22 17:00:00', '社区图书馆', 40, 15, 0, '2026-04-15 14:58:50', '2026-04-15 14:58:50');
INSERT INTO `activity` VALUES (570, '美食制作大赛', '社区美食制作大赛开始报名！展示你的厨艺，赢取精美奖品。', 'https://images.unsplash.com/photo-1556910103-1c02745a30bf?w=800&h=600&fit=crop&q=80', '2026-03-25 10:00:00', '2026-03-25 12:00:00', '社区活动中心', 20, 18, 0, '2026-04-15 14:58:50', '2026-04-15 14:58:50');

-- ----------------------------
-- Table structure for activity_checkin
-- ----------------------------
DROP TABLE IF EXISTS `activity_checkin`;
CREATE TABLE `activity_checkin`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '签到 ID，主键，自增',
  `activity_id` bigint(0) NULL DEFAULT NULL COMMENT '活动 ID，关联 activity 表',
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '用户 ID，关联 user 表',
  `status` tinyint(0) NULL DEFAULT 0 COMMENT '签到状态：0-未签到，1-已签到',
  `checkin_time` datetime(0) NULL DEFAULT NULL COMMENT '签到时间',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_activity_id`(`activity_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动签到表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity_checkin
-- ----------------------------

-- ----------------------------
-- Table structure for activity_comment
-- ----------------------------
DROP TABLE IF EXISTS `activity_comment`;
CREATE TABLE `activity_comment`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '评论ID，主键，自增',
  `activity_id` bigint(0) NULL DEFAULT NULL COMMENT '活动ID，关联activity表',
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '用户ID，关联user表',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评论内容',
  `rating` int(0) NULL DEFAULT NULL COMMENT '评分，1-5分',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '评论时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `activity_id`(`activity_id`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE,
  CONSTRAINT `activity_comment_ibfk_1` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `activity_comment_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动评论表，存储活动评价信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity_comment
-- ----------------------------

-- ----------------------------
-- Table structure for activity_photo
-- ----------------------------
DROP TABLE IF EXISTS `activity_photo`;
CREATE TABLE `activity_photo`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '照片ID，主键，自增',
  `activity_id` bigint(0) NULL DEFAULT NULL COMMENT '活动ID，关联activity表',
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '上传者ID，关联user表',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '照片URL地址',
  `uploaded_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '上传时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `activity_id`(`activity_id`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE,
  CONSTRAINT `activity_photo_ibfk_1` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `activity_photo_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动照片表，存储活动照片信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity_photo
-- ----------------------------

-- ----------------------------
-- Table structure for activity_signup
-- ----------------------------
DROP TABLE IF EXISTS `activity_signup`;
CREATE TABLE `activity_signup`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '报名ID，主键，自增',
  `activity_id` bigint(0) NULL DEFAULT NULL COMMENT '活动ID，关联activity表',
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '用户ID，关联user表',
  `signup_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '报名时间',
  `status` enum('pending','confirmed','cancelled') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'pending' COMMENT '报名状态：pending-待确认，confirmed-已确认，cancelled-已取消',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `activity_id`(`activity_id`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE,
  CONSTRAINT `activity_signup_ibfk_1` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `activity_signup_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动报名表，存储活动报名信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity_signup
-- ----------------------------

-- ----------------------------
-- Table structure for alarm_record
-- ----------------------------
DROP TABLE IF EXISTS `alarm_record`;
CREATE TABLE `alarm_record`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `alarm_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `confidence` double NOT NULL DEFAULT 0,
  `image_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `alarm_time` datetime(0) NOT NULL,
  `is_read` tinyint(0) NOT NULL DEFAULT 0,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_alarm_time`(`alarm_time`) USING BTREE,
  INDEX `idx_alarm_type`(`alarm_type`) USING BTREE,
  INDEX `idx_is_read`(`is_read`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of alarm_record
-- ----------------------------

-- ----------------------------
-- Table structure for appointment
-- ----------------------------
DROP TABLE IF EXISTS `appointment`;
CREATE TABLE `appointment`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '预约ID，主键，自增',
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '用户ID，关联user表',
  `doctor_id` bigint(0) NULL DEFAULT NULL COMMENT '医生ID，关联user表',
  `type` enum('checkup','vaccine','other') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '预约类型：checkup-体检，vaccine-疫苗，other-其他',
  `appointment_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'HEALTH_CHECK' COMMENT '预约类型：HEALTH_CHECK-健康检查，VACCINE-疫苗接种',
  `date` datetime(0) NULL DEFAULT NULL COMMENT '预约时间',
  `status` enum('pending','confirmed','completed','cancelled') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'pending' COMMENT '预约状态：pending-待确认，confirmed-已确认，completed-已完成，cancelled-已取消',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE,
  INDEX `doctor_id`(`doctor_id`) USING BTREE,
  CONSTRAINT `appointment_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `appointment_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预约表，存储医疗预约信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of appointment
-- ----------------------------
INSERT INTO `appointment` VALUES (1, 4, 3, 'checkup', 'HEALTH_CHECK', '2026-02-10 09:00:00', 'completed', '2026-01-30 16:03:43');
INSERT INTO `appointment` VALUES (2, 4, 3, 'checkup', 'HEALTH_CHECK', '2026-02-11 11:18:47', 'cancelled', '2026-02-11 11:18:46');
INSERT INTO `appointment` VALUES (3, 4, 3, 'other', 'HEALTH_CHECK', '2026-02-11 11:39:58', 'cancelled', '2026-02-11 11:39:58');
INSERT INTO `appointment` VALUES (4, 4, 3, 'checkup', 'HEALTH_CHECK', '2026-02-11 11:40:35', 'cancelled', '2026-02-11 11:40:34');
INSERT INTO `appointment` VALUES (5, 17, 3, 'checkup', 'HEALTH_CHECK', '2026-02-18 16:15:21', 'confirmed', '2026-02-18 16:15:21');
INSERT INTO `appointment` VALUES (6, 11, 5, 'checkup', 'HEALTH_CHECK', '2026-02-23 12:16:38', 'pending', '2026-02-23 12:16:37');
INSERT INTO `appointment` VALUES (7, 4, 1, NULL, 'HEALTH_CHECK', NULL, 'pending', '2026-03-07 15:15:51');

-- ----------------------------
-- Table structure for building
-- ----------------------------
DROP TABLE IF EXISTS `building`;
CREATE TABLE `building`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '楼栋ID，主键，自增',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '楼栋名称，如1号楼',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '楼栋地址',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '楼栋表，存储社区楼栋信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of building
-- ----------------------------
INSERT INTO `building` VALUES (1, '1号楼', '未来街区A区1号楼');
INSERT INTO `building` VALUES (2, '2号楼', '未来街区A区2号楼');

-- ----------------------------
-- Table structure for chronic_disease_type
-- ----------------------------
DROP TABLE IF EXISTS `chronic_disease_type`;
CREATE TABLE `chronic_disease_type`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '慢性病类型ID，主键，自增',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '慢性病类型名称',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '慢性病类型描述',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '慢性病类型表，存储标准化的慢性病分类' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chronic_disease_type
-- ----------------------------
INSERT INTO `chronic_disease_type` VALUES (1, '高血压', '以体循环动脉血压（收缩压和/或舒张压）增高为主要特征的慢性疾病', '2026-02-24 14:00:00');
INSERT INTO `chronic_disease_type` VALUES (2, '糖尿病', '一组以高血糖为特征的代谢性疾病，主要分为1型和2型', '2026-02-24 14:00:00');
INSERT INTO `chronic_disease_type` VALUES (3, '冠心病', '冠状动脉粥样硬化性心脏病，由于冠状动脉血管发生动脉粥样硬化病变而引起血管腔狭窄或阻塞', '2026-02-24 14:00:00');
INSERT INTO `chronic_disease_type` VALUES (4, '脑卒中', '脑血管意外，由于脑部血管突然破裂或因血管阻塞导致血液不能流入大脑而引起脑组织损伤的一组疾病', '2026-02-24 14:00:00');
INSERT INTO `chronic_disease_type` VALUES (5, '慢性阻塞性肺疾病', '一种具有气流阻塞特征的慢性支气管炎和（或）肺气肿，可进一步发展为肺心病和呼吸衰竭', '2026-02-24 14:00:00');
INSERT INTO `chronic_disease_type` VALUES (6, '慢性肾病', '各种原因引起的慢性肾脏结构和功能障碍，持续时间超过3个月', '2026-02-24 14:00:00');
INSERT INTO `chronic_disease_type` VALUES (7, '骨质疏松', '一种以骨量低下，骨微结构损坏，导致骨脆性增加，易发生骨折为特征的全身性骨病', '2026-02-24 14:00:00');
INSERT INTO `chronic_disease_type` VALUES (8, '其他', '其他未明确分类的慢性疾病', '2026-02-24 14:00:00');

-- ----------------------------
-- Table structure for complaint
-- ----------------------------
DROP TABLE IF EXISTS `complaint`;
CREATE TABLE `complaint`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '投诉ID，主键，自增',
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '用户ID，关联user表',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '投诉内容',
  `status` enum('pending','replied','closed') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'pending' COMMENT '投诉状态：pending-待回复，replied-已回复，closed-已关闭',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '投诉表，存储投诉信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of complaint
-- ----------------------------
INSERT INTO `complaint` VALUES (1, 4, '最近晚上10点以后总有人在楼下跳广场舞，噪音很大，希望能协调一下。', 'replied', '2026-01-30 16:03:43');
INSERT INTO `complaint` VALUES (2, 4, '小区环境卫生需要改善，垃圾清理不及时', 'closed', '2026-03-07 16:09:34');
INSERT INTO `complaint` VALUES (3, 4, '小区环境卫生需要改善，垃圾清理不及时', 'closed', '2026-03-07 16:09:57');

-- ----------------------------
-- Table structure for complaint_reply
-- ----------------------------
DROP TABLE IF EXISTS `complaint_reply`;
CREATE TABLE `complaint_reply`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '回复ID，主键，自增',
  `complaint_id` bigint(0) NULL DEFAULT NULL COMMENT '投诉ID，关联complaint表',
  `reply_user_id` bigint(0) NULL DEFAULT NULL COMMENT '回复人ID，关联user表',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '回复内容',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '回复时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `complaint_id`(`complaint_id`) USING BTREE,
  INDEX `reply_user_id`(`reply_user_id`) USING BTREE,
  CONSTRAINT `complaint_reply_ibfk_1` FOREIGN KEY (`complaint_id`) REFERENCES `complaint` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `complaint_reply_ibfk_2` FOREIGN KEY (`reply_user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '投诉回复表，存储投诉回复信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of complaint_reply
-- ----------------------------
INSERT INTO `complaint_reply` VALUES (1, 1, 2, '收到您的反馈，我们会安排安保人员在相关时段加强巡逻并进行劝导。', '2026-01-30 16:03:43');
INSERT INTO `complaint_reply` VALUES (2, 1, 2, '好的，我们会去找组织者协商解决', '2026-02-23 10:58:43');
INSERT INTO `complaint_reply` VALUES (3, 2, 2, '您好，我们已经收到您的投诉，会尽快处理', '2026-03-07 16:09:34');
INSERT INTO `complaint_reply` VALUES (4, 3, 2, '您好，我们已经收到您的投诉，会尽快处理', '2026-03-07 16:09:57');

-- ----------------------------
-- Table structure for dict
-- ----------------------------
DROP TABLE IF EXISTS `dict`;
CREATE TABLE `dict`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '字典ID，主键，自增',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典类型',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典编码',
  `value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典值',
  `description` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典描述',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数据字典表，存储系统字典数据' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dict
-- ----------------------------
INSERT INTO `dict` VALUES (1, 'REPAIR_TYPE', 'WATER', '水暖维修', '包含龙头、管道等');
INSERT INTO `dict` VALUES (2, 'REPAIR_TYPE', 'ELECTRIC', '电路维修', '包含灯具、开关等');
INSERT INTO `dict` VALUES (3, 'ACTIVITY_TYPE', 'SPORT', '体育运动', '球类、瑜伽等');
INSERT INTO `dict` VALUES (4, 'NOTICE_TYPE', 'REPAIR_ASSIGNMENT', '报修分配', '报修分配通知');
INSERT INTO `dict` VALUES (5, 'NOTICE_TYPE', 'REPAIR_IN_PROGRESS', '维修中', '维修进度通知');
INSERT INTO `dict` VALUES (6, 'NOTICE_TYPE', 'REPAIR_COMPLETED', '维修完成', '维修完成通知');

-- ----------------------------
-- Table structure for doctor_schedule
-- ----------------------------
DROP TABLE IF EXISTS `doctor_schedule`;
CREATE TABLE `doctor_schedule`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '排班ID，主键，自增',
  `doctor_id` bigint(0) NULL DEFAULT NULL COMMENT '医生ID，关联user表',
  `schedule_date` date NULL DEFAULT NULL COMMENT '排班日期',
  `start_time` time(0) NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` time(0) NULL DEFAULT NULL COMMENT '结束时间',
  `status` enum('available','unavailable') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'available' COMMENT '状态：available-可预约，unavailable-不可预约',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `doctor_id`(`doctor_id`) USING BTREE,
  CONSTRAINT `doctor_schedule_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 213 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生排班表，存储医生的排班信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of doctor_schedule
-- ----------------------------
INSERT INTO `doctor_schedule` VALUES (1, 3, '2026-02-01', '09:00:00', '12:00:00', 'unavailable', '2026-01-30 16:03:43');
INSERT INTO `doctor_schedule` VALUES (2, 3, '2026-02-01', '14:00:00', '17:00:00', 'unavailable', '2026-01-30 16:03:43');
INSERT INTO `doctor_schedule` VALUES (3, 3, '2026-02-02', '09:00:00', '12:00:00', 'unavailable', '2026-01-30 16:03:43');
INSERT INTO `doctor_schedule` VALUES (4, 3, '2026-02-02', '14:00:00', '17:00:00', 'unavailable', '2026-01-30 16:03:43');
INSERT INTO `doctor_schedule` VALUES (5, 3, '2026-02-03', '09:00:00', '12:00:00', 'unavailable', '2026-01-30 16:03:43');
INSERT INTO `doctor_schedule` VALUES (6, 3, '2026-02-03', '14:00:00', '17:00:00', 'available', '2026-01-30 16:03:43');
INSERT INTO `doctor_schedule` VALUES (7, 3, '2026-02-04', '09:00:00', '12:00:00', 'available', '2026-02-11 13:06:31');
INSERT INTO `doctor_schedule` VALUES (8, 3, '2026-02-04', '14:00:00', '17:00:00', 'available', '2026-02-11 13:06:31');
INSERT INTO `doctor_schedule` VALUES (9, 3, '2026-02-05', '09:00:00', '12:00:00', 'available', '2026-02-11 13:06:31');
INSERT INTO `doctor_schedule` VALUES (10, 3, '2026-02-06', '14:00:00', '17:00:00', 'unavailable', '2026-02-11 13:06:31');
INSERT INTO `doctor_schedule` VALUES (11, 5, '2026-02-04', '09:00:00', '12:00:00', 'unavailable', '2026-02-11 13:08:54');
INSERT INTO `doctor_schedule` VALUES (12, 5, '2026-02-04', '14:00:00', '17:00:00', 'available', '2026-02-11 13:08:54');
INSERT INTO `doctor_schedule` VALUES (13, 5, '2026-02-05', '09:00:00', '12:00:00', 'available', '2026-02-11 13:08:54');
INSERT INTO `doctor_schedule` VALUES (14, 5, '2026-02-06', '14:00:00', '17:00:00', 'unavailable', '2026-02-11 13:08:54');
INSERT INTO `doctor_schedule` VALUES (15, 6, '2026-02-04', '09:00:00', '12:00:00', 'available', '2026-02-11 13:08:54');
INSERT INTO `doctor_schedule` VALUES (16, 6, '2026-02-05', '14:00:00', '17:00:00', 'available', '2026-02-11 13:08:54');
INSERT INTO `doctor_schedule` VALUES (17, 6, '2026-02-06', '09:00:00', '12:00:00', 'available', '2026-02-11 13:08:54');
INSERT INTO `doctor_schedule` VALUES (18, 6, '2026-02-07', '09:00:00', '12:00:00', 'unavailable', '2026-02-11 13:08:54');
INSERT INTO `doctor_schedule` VALUES (81, 3, '2026-02-17', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (82, 3, '2026-02-17', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (83, 3, '2026-02-18', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (84, 3, '2026-02-19', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (85, 3, '2026-02-20', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (86, 3, '2026-02-21', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (87, 3, '2026-02-22', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (88, 3, '2026-02-23', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (89, 3, '2026-02-24', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (90, 3, '2026-02-25', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (91, 3, '2026-02-26', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (92, 3, '2026-02-27', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (93, 3, '2026-02-28', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (94, 3, '2026-03-01', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (95, 3, '2026-03-02', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (96, 3, '2026-03-02', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (97, 3, '2026-03-03', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (98, 3, '2026-03-04', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (99, 3, '2026-03-05', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (100, 3, '2026-03-06', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (101, 3, '2026-03-07', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (102, 3, '2026-03-08', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (103, 3, '2026-03-09', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (104, 3, '2026-03-10', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (105, 3, '2026-03-11', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (106, 3, '2026-03-12', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (107, 3, '2026-03-13', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (108, 3, '2026-03-14', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (109, 3, '2026-03-15', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (110, 3, '2026-03-16', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (111, 3, '2026-03-17', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (112, 3, '2026-03-18', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (113, 3, '2026-03-19', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (114, 3, '2026-03-20', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (115, 3, '2026-03-21', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (116, 3, '2026-03-22', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (117, 3, '2026-03-23', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (118, 3, '2026-03-24', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (119, 3, '2026-03-25', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (120, 3, '2026-03-26', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (121, 3, '2026-03-27', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (122, 3, '2026-03-28', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (123, 3, '2026-03-29', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (124, 3, '2026-03-30', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (125, 3, '2026-03-31', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (126, 5, '2026-02-17', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (127, 5, '2026-02-18', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (128, 5, '2026-02-19', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (129, 5, '2026-02-20', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (130, 5, '2026-02-21', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (131, 5, '2026-02-22', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (132, 5, '2026-02-23', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (133, 5, '2026-02-24', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (134, 5, '2026-02-25', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (135, 5, '2026-02-26', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (136, 5, '2026-02-27', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (137, 5, '2026-02-28', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (138, 5, '2026-03-01', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (139, 5, '2026-03-02', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (140, 5, '2026-03-02', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (141, 5, '2026-03-03', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (142, 5, '2026-03-04', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (143, 5, '2026-03-05', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (144, 5, '2026-03-06', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (145, 5, '2026-03-07', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (146, 5, '2026-03-08', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (147, 5, '2026-03-09', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (148, 5, '2026-03-10', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (149, 5, '2026-03-11', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (150, 5, '2026-03-12', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (151, 5, '2026-03-13', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (152, 5, '2026-03-14', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (153, 5, '2026-03-15', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (154, 5, '2026-03-16', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (155, 5, '2026-03-17', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (156, 5, '2026-03-18', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (157, 5, '2026-03-19', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (158, 5, '2026-03-20', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (159, 5, '2026-03-21', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (160, 5, '2026-03-22', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (161, 5, '2026-03-23', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (162, 5, '2026-03-24', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (163, 5, '2026-03-25', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (164, 5, '2026-03-26', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (165, 5, '2026-03-27', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (166, 5, '2026-03-28', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (167, 5, '2026-03-29', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (168, 5, '2026-03-30', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (169, 5, '2026-03-31', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (170, 6, '2026-02-17', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (171, 6, '2026-02-18', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (172, 6, '2026-02-19', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (173, 6, '2026-02-20', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (174, 6, '2026-02-21', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (175, 6, '2026-02-22', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (176, 6, '2026-02-23', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (177, 6, '2026-02-24', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (178, 6, '2026-02-25', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (179, 6, '2026-02-26', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (180, 6, '2026-02-27', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (181, 6, '2026-02-28', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (182, 6, '2026-03-01', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (183, 6, '2026-03-02', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (184, 6, '2026-03-02', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (185, 6, '2026-03-03', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (186, 6, '2026-03-04', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (187, 6, '2026-03-05', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (188, 6, '2026-03-06', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (189, 6, '2026-03-07', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (190, 6, '2026-03-08', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (191, 6, '2026-03-09', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (192, 6, '2026-03-10', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (193, 6, '2026-03-11', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (194, 6, '2026-03-12', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (195, 6, '2026-03-13', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (196, 6, '2026-03-14', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (197, 6, '2026-03-15', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (198, 6, '2026-03-16', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (199, 6, '2026-03-17', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (200, 6, '2026-03-18', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (201, 6, '2026-03-19', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (202, 6, '2026-03-20', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (203, 6, '2026-03-21', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (204, 6, '2026-03-22', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (205, 6, '2026-03-23', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (206, 6, '2026-03-24', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (207, 6, '2026-03-25', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (208, 6, '2026-03-26', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (209, 6, '2026-03-27', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (210, 6, '2026-03-28', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (211, 6, '2026-03-29', '14:00:00', '17:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (212, 6, '2026-03-30', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');
INSERT INTO `doctor_schedule` VALUES (213, 6, '2026-03-31', '09:00:00', '12:00:00', 'available', '2026-02-17 13:10:18');

-- ----------------------------
-- Table structure for express
-- ----------------------------
DROP TABLE IF EXISTS `express`;
CREATE TABLE `express`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '快递ID，主键，自增',
  `tracking_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '快递单号',
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '收件人ID，关联user表',
  `pickup_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '取件码，用于取件验证',
  `status` enum('in','notified','picked','out') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'in' COMMENT '快递状态：in-已入库，notified-已通知，picked-已取件，out-已出库',
  `received_at` datetime(0) NULL DEFAULT NULL COMMENT '入库时间',
  `picked_at` datetime(0) NULL DEFAULT NULL COMMENT '取件时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE,
  CONSTRAINT `express_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '快递表，存储快递信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of express
-- ----------------------------
INSERT INTO `express` VALUES (1, 'SF123456789', 4, 'A-1024', 'picked', '2026-01-30 16:03:43', '2026-01-30 17:44:22');
INSERT INTO `express` VALUES (2, 'JD987654321', 4, 'B-2048', 'picked', '2026-01-25 10:00:00', '2026-01-25 14:30:00');
INSERT INTO `express` VALUES (3, 'YT987654321', 7, 'C-3072', 'in', '2026-02-13 09:00:00', NULL);
INSERT INTO `express` VALUES (4, 'ZT654321987', 8, 'D-4096', 'notified', '2026-02-13 10:30:00', NULL);
INSERT INTO `express` VALUES (5, 'YD456789123', 9, 'E-5120', 'picked', '2026-02-12 14:00:00', '2026-02-12 16:30:00');
INSERT INTO `express` VALUES (6, 'EMS789456123', 10, 'F-6144', 'in', '2026-02-13 11:15:00', NULL);
INSERT INTO `express` VALUES (7, 'STO321654987', 11, 'G-7168', 'notified', '2026-02-13 14:30:00', NULL);
INSERT INTO `express` VALUES (8, 'YJ987321654', 12, 'H-8192', 'picked', '2026-02-11 10:00:00', '2026-02-11 15:20:00');
INSERT INTO `express` VALUES (9, 'KD654987321', 13, 'I-9216', 'in', '2026-02-13 15:45:00', NULL);
INSERT INTO `express` VALUES (10, 'TT321987654', 14, 'J-0240', 'notified', '2026-02-13 08:30:00', NULL);
INSERT INTO `express` VALUES (11, 'YT123456789', 4, '108900', 'in', '2026-03-07 15:07:27', NULL);
INSERT INTO `express` VALUES (12, 'YT987654321', 4, '704414', 'in', '2026-03-07 15:07:27', NULL);
INSERT INTO `express` VALUES (13, 'YT555666777', 1, '232720', 'in', '2026-03-07 15:07:27', NULL);
INSERT INTO `express` VALUES (14, 'SF123456790', 4, '864012', 'in', '2026-03-20 16:57:57', NULL);

-- ----------------------------
-- Table structure for health_education
-- ----------------------------
DROP TABLE IF EXISTS `health_education`;
CREATE TABLE `health_education`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '宣教标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '宣教内容',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `creator_id` bigint(0) NULL DEFAULT NULL COMMENT '创建者ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `creator_id`(`creator_id`) USING BTREE,
  CONSTRAINT `fk_health_education_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '健康宣教表，存储医生发布的健康宣教信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of health_education
-- ----------------------------

-- ----------------------------
-- Table structure for health_record
-- ----------------------------
DROP TABLE IF EXISTS `health_record`;
CREATE TABLE `health_record`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '健康档案ID，主键，自增',
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '用户ID，关联user表',
  `doctor_id` bigint(0) NULL DEFAULT NULL COMMENT '医生ID，关联user表',
  `chronic_disease_type_id` bigint(0) NULL DEFAULT NULL COMMENT '慢性病类型ID，关联chronic_disease_type表',
  `chronic_disease_other` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '其他慢性病信息，当类型为其他时使用',
  `height` decimal(5, 2) NULL DEFAULT NULL COMMENT '身高，单位：cm',
  `weight` decimal(5, 2) NULL DEFAULT NULL COMMENT '体重，单位：kg',
  `bmi` decimal(4, 2) NULL DEFAULT NULL COMMENT 'BMI指数',
  `blood_pressure` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '血压，如：120/80',
  `blood_sugar` decimal(5, 2) NULL DEFAULT NULL COMMENT '血糖，单位：mmol/L',
  `doctor_suggestion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '医生总结建议',
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '医生备注',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE,
  INDEX `doctor_id`(`doctor_id`) USING BTREE,
  INDEX `chronic_disease_type_id`(`chronic_disease_type_id`) USING BTREE,
  CONSTRAINT `health_record_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `health_record_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `health_record_ibfk_3` FOREIGN KEY (`chronic_disease_type_id`) REFERENCES `chronic_disease_type` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '健康档案表，存储居民健康信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of health_record
-- ----------------------------
INSERT INTO `health_record` VALUES (1, 4, 3, NULL, NULL, 175.00, 68.00, 22.20, '120/80', 5.20, '基础体检结果良好，注意加强体育锻炼。', NULL, '2026-01-30 16:03:43');
INSERT INTO `health_record` VALUES (2, 4, 3, NULL, NULL, 175.00, 67.50, 22.04, '118/78', 5.00, '按时吃药，切记荤腥', NULL, '2026-02-11 12:45:35');
INSERT INTO `health_record` VALUES (3, 11, 5, 1, NULL, 168.00, 75.00, 26.57, '145/95', 5.80, '建议低盐饮食，定期监测血压', '血压偏高，需要定期复查', '2026-02-11 12:58:24');
INSERT INTO `health_record` VALUES (4, 12, 6, 2, NULL, 158.00, 62.00, 24.80, '130/85', 7.20, '空腹血糖偏高，需规律用药', '糖尿病II型，建议控制饮食', '2026-02-11 12:58:24');
INSERT INTO `health_record` VALUES (5, 13, 5, 8, '关节炎', 170.00, 70.00, 24.22, '125/82', 5.50, '天气变化时注意保暖', '膝关节关节炎，建议避免剧烈运动', '2026-02-11 12:58:24');
INSERT INTO `health_record` VALUES (6, 14, 6, NULL, NULL, 165.00, 58.00, 21.30, '115/75', 4.90, '身体状况良好', NULL, '2026-02-11 12:58:24');
INSERT INTO `health_record` VALUES (7, 7, 5, NULL, NULL, 140.00, 35.00, 17.86, '100/65', 4.80, '儿童体检正常', '生长发育良好', '2026-02-11 12:58:24');
INSERT INTO `health_record` VALUES (8, 8, 6, 8, '贫血', 160.00, 48.00, 18.75, '110/70', 5.10, '建议补铁，多休息', '轻度贫血，建议多食用含铁食物', '2026-02-11 12:58:24');

-- ----------------------------
-- Table structure for health_reminder
-- ----------------------------
DROP TABLE IF EXISTS `health_reminder`;
CREATE TABLE `health_reminder`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '提醒标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '提醒内容',
  `chronic_disease_type_id` bigint(0) NULL DEFAULT NULL COMMENT '目标慢病类型ID，关联chronic_disease_type表',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `creator_id` bigint(0) NULL DEFAULT NULL COMMENT '创建者ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `creator_id`(`creator_id`) USING BTREE,
  INDEX `chronic_disease_type_id`(`chronic_disease_type_id`) USING BTREE,
  CONSTRAINT `fk_health_reminder_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_health_reminder_disease_type` FOREIGN KEY (`chronic_disease_type_id`) REFERENCES `chronic_disease_type` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '健康提醒表，存储医生发布的慢病提醒信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of health_reminder
-- ----------------------------

-- ----------------------------
-- Table structure for house
-- ----------------------------
DROP TABLE IF EXISTS `house`;
CREATE TABLE `house`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '房屋ID，主键，自增',
  `building_id` bigint(0) NULL DEFAULT NULL COMMENT '所属楼栋ID，关联building表',
  `house_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '房号，如101室',
  `owner_id` bigint(0) NULL DEFAULT NULL COMMENT '业主ID，关联user表',
  `status` tinyint(0) NULL DEFAULT 1 COMMENT '房屋状态：0-不可用，1-可用',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `building_id`(`building_id`) USING BTREE,
  INDEX `owner_id`(`owner_id`) USING BTREE,
  CONSTRAINT `house_ibfk_1` FOREIGN KEY (`building_id`) REFERENCES `building` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `house_ibfk_2` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '房屋表，存储房屋信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of house
-- ----------------------------
INSERT INTO `house` VALUES (1, 1, '1001', 4, 1);
INSERT INTO `house` VALUES (2, 1, '102室', NULL, 0);
INSERT INTO `house` VALUES (3, 2, '101室', NULL, 0);
INSERT INTO `house` VALUES (4, 2, '102室', NULL, 0);
INSERT INTO `house` VALUES (5, 1, '103室', NULL, 0);
INSERT INTO `house` VALUES (6, 2, '103室', 8, 0);
INSERT INTO `house` VALUES (7, 1, '201室', 9, 1);
INSERT INTO `house` VALUES (8, 1, '202室', 10, 1);
INSERT INTO `house` VALUES (9, 2, '201室', 11, 1);
INSERT INTO `house` VALUES (10, 2, '202室', 12, 1);
INSERT INTO `house` VALUES (11, 1, '301室', 13, 1);
INSERT INTO `house` VALUES (12, 1, '302室', 14, 1);
INSERT INTO `house` VALUES (13, 2, '301室', NULL, 1);
INSERT INTO `house` VALUES (14, 2, '302室', NULL, 1);
INSERT INTO `house` VALUES (15, 1, '1001', NULL, 0);

-- ----------------------------
-- Table structure for neighbor_message
-- ----------------------------
DROP TABLE IF EXISTS `neighbor_message`;
CREATE TABLE `neighbor_message`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '消息ID，主键，自增',
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '用户ID，关联user表',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '帖子标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '消息内容',
  `reply_count` int(0) NULL DEFAULT 0 COMMENT '评论数',
  `like_count` int(0) NULL DEFAULT 0 COMMENT '点赞数',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE,
  CONSTRAINT `neighbor_message_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '邻里消息表，存储邻里交流信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of neighbor_message
-- ----------------------------
INSERT INTO `neighbor_message` VALUES (1, 4, '物业联系方式', '有人知道物业的电话吗？刚搬进来还没存。', 1, 2, '2026-01-30 16:03:43');
INSERT INTO `neighbor_message` VALUES (3, 2, '新年快乐', '初一🌸㊗您万象更新🌸\n初二✨㊗您福气满堂✨\n初三💡㊗您智慧通达💡\n初四🍎㊗您平平安安🍎\n初五💰㊗您财源广进💰\n初六🏃㊗您马不停蹄🏃\n初七🎂㊗您人寿年丰🎂\n初八🌊㊗您财源滚滚🌊\n初九👼㊗您吉星高照👼\n初十🌈㊗您十全十美🌈\n十一🌿㊗您百业兴旺🌿\n十二🎇㊗您前程似锦🎇\n十三💑㊗您合家欢乐💑\n十四💪㊗您身强体健💪\n十五🏮㊗您花好月圆🏮\n祝您2026马年策马扬鞭，好运连连，幸福美满，发发发！[發][發][發]', 1, 1, '2026-02-17 12:51:44');
INSERT INTO `neighbor_message` VALUES (4, 4, '周末社区篮球活动', '这周末下午 3 点在社区篮球场有篮球活动，欢迎大家参加！', 1, 0, '2026-03-07 16:09:34');
INSERT INTO `neighbor_message` VALUES (5, 4, '周末社区篮球活动', '这周末下午 3 点在社区篮球场有篮球活动，欢迎大家参加！', 1, 0, '2026-03-07 16:09:57');
INSERT INTO `neighbor_message` VALUES (6, 4, '111', '111111', 0, 0, '2026-03-27 13:48:01');

-- ----------------------------
-- Table structure for neighbor_message_like
-- ----------------------------
DROP TABLE IF EXISTS `neighbor_message_like`;
CREATE TABLE `neighbor_message_like`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '点赞ID，主键，自增',
  `message_id` bigint(0) NOT NULL COMMENT '消息ID，关联neighbor_message表',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID，关联user表',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_user`(`message_id`, `user_id`) USING BTREE COMMENT '唯一索引，确保一个用户只能对一个消息点赞一次',
  INDEX `message_id`(`message_id`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE,
  CONSTRAINT `neighbor_message_like_ibfk_1` FOREIGN KEY (`message_id`) REFERENCES `neighbor_message` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `neighbor_message_like_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '邻里消息点赞表，存储用户对消息的点赞记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of neighbor_message_like
-- ----------------------------
INSERT INTO `neighbor_message_like` VALUES (1, 1, 4, '2026-02-17 12:50:57');
INSERT INTO `neighbor_message_like` VALUES (2, 3, 4, '2026-02-17 12:51:58');
INSERT INTO `neighbor_message_like` VALUES (3, 1, 2, '2026-02-23 10:56:09');

-- ----------------------------
-- Table structure for neighbor_message_reply
-- ----------------------------
DROP TABLE IF EXISTS `neighbor_message_reply`;
CREATE TABLE `neighbor_message_reply`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '评论ID，主键，自增',
  `message_id` bigint(0) NULL DEFAULT NULL COMMENT '消息ID，关联neighbor_message表',
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '用户ID，关联user表',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评论内容',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `message_id`(`message_id`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE,
  CONSTRAINT `neighbor_message_reply_ibfk_1` FOREIGN KEY (`message_id`) REFERENCES `neighbor_message` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `neighbor_message_reply_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '邻里消息评论表，存储评论信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of neighbor_message_reply
-- ----------------------------
INSERT INTO `neighbor_message_reply` VALUES (1, 1, 1, '回复王小明：13800000002 是张经理的电话，您可以联系他。', '2026-01-30 16:03:43');
INSERT INTO `neighbor_message_reply` VALUES (2, 3, 4, '新年快乐', '2026-02-17 12:52:02');
INSERT INTO `neighbor_message_reply` VALUES (3, 4, 4, '好主意，我一定参加！', '2026-03-07 16:09:34');
INSERT INTO `neighbor_message_reply` VALUES (4, 5, 4, '好主意，我一定参加！', '2026-03-07 16:09:57');

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '公告ID，主键，自增',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '公告内容',
  `target_type` enum('all','resident','property','doctor','technician') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标类型：all-全员，resident-居民，property-物业，doctor-医生，technician-维修师傅',
  `created_by` bigint(0) NULL DEFAULT NULL COMMENT '创建人ID，关联user表',
  `repair_id` bigint(0) NULL DEFAULT NULL COMMENT '关联的报修ID',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `created_by`(`created_by`) USING BTREE,
  INDEX `repair_id`(`repair_id`) USING BTREE,
  CONSTRAINT `notice_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '公告表，存储系统公告信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notice
-- ----------------------------
INSERT INTO `notice` VALUES (1, '关于春节期间加强安保工作的通知', '春节将至，请各位邻居出门锁好门窗...', 'all', 1, NULL, '2026-01-30 16:03:43');
INSERT INTO `notice` VALUES (2, '社区卫生站流感疫苗接种通知', '请有需要的居民联系李医生进行预约...', 'resident', 3, NULL, '2026-01-30 16:03:43');
INSERT INTO `notice` VALUES (4, '新年快乐', '初一🌸㊗您万象更新🌸\n初二✨㊗您福气满堂✨\n初三💡㊗您智慧通达💡\n初四🍎㊗您平平安安🍎\n初五💰㊗您财源广进💰\n初六🏃㊗您马不停蹄🏃\n初七🎂㊗您人寿年丰🎂\n初八🌊㊗您财源滚滚🌊\n初九👼㊗您吉星高照👼\n初十🌈㊗您十全十美🌈\n十一🌿㊗您百业兴旺🌿\n十二🎇㊗您前程似锦🎇\n十三💑㊗您合家欢乐💑\n十四💪㊗您身强体健💪\n十五🏮㊗您花好月圆🏮\n物业全体工作人员祝您2026马年策马扬鞭，好运连连，幸福美满，发发发！', 'resident', 2, NULL, '2026-02-18 11:06:47');
INSERT INTO `notice` VALUES (7, '维修完成通知', '您的报修申请已完成，请对服务进行评价', 'all', 17, 2, '2026-02-18 15:25:03');
INSERT INTO `notice` VALUES (8, '【紧急风险预警】', '发现高风险事件：老人摔倒', 'property', 1, NULL, '2026-03-06 18:52:33');
INSERT INTO `notice` VALUES (9, '报修分配通知', '您的报修申请已被分配给维修师傅：李医生', 'all', 2, 5, '2026-03-07 16:09:34');
INSERT INTO `notice` VALUES (10, '报修分配通知', '您的报修申请已被分配给维修师傅：李医生', 'all', 2, 7, '2026-03-07 16:09:57');
INSERT INTO `notice` VALUES (11, '【紧急风险预警】', '发现高风险事件：火灾风险', 'property', 1, NULL, '2026-03-08 10:49:57');
INSERT INTO `notice` VALUES (12, '【紧急风险预警】', '发现高风险事件：火灾风险', 'property', 1, NULL, '2026-03-08 10:52:51');
INSERT INTO `notice` VALUES (13, '报修分配通知', '您的报修申请已被分配给维修师傅：张师傅', 'all', 2, 3, '2026-03-08 15:32:09');
INSERT INTO `notice` VALUES (14, '【紧急风险预警】', '发现高风险事件：老人摔倒', 'property', 1, NULL, '2026-03-15 15:23:30');
INSERT INTO `notice` VALUES (15, '【紧急风险预警】', '发现高风险事件：老人摔倒', 'property', 1, NULL, '2026-03-20 13:49:23');
INSERT INTO `notice` VALUES (16, '【紧急风险预警】', '发现高风险事件：老人摔倒', 'property', 1, NULL, '2026-03-20 15:23:40');
INSERT INTO `notice` VALUES (17, '报修派单通知', '您的报修申请已被分配给维修师傅：王师傅，请等待接单', 'all', 2, 9, '2026-03-25 11:01:41');
INSERT INTO `notice` VALUES (18, '维修接单通知', '维修师傅已确认接单，将尽快为您处理', 'all', 17, 9, '2026-03-25 12:11:18');
INSERT INTO `notice` VALUES (19, '维修竣工通知', '您的报修申请已完成，维修师傅已提交竣工报告。请对服务进行评价', 'all', 17, 9, '2026-03-25 12:11:20');
INSERT INTO `notice` VALUES (20, '【紧急风险预警】', '发现高风险事件：老人摔倒', 'property', 1, NULL, '2026-03-27 13:45:23');
INSERT INTO `notice` VALUES (21, '【紧急风险预警】', '发现高风险事件：老人摔倒', 'property', 1, NULL, '2026-04-07 18:59:24');
INSERT INTO `notice` VALUES (22, '【紧急风险预警】', '发现高风险事件：火灾风险', 'property', 1, NULL, '2026-04-10 16:09:55');

-- ----------------------------
-- Table structure for notice_read
-- ----------------------------
DROP TABLE IF EXISTS `notice_read`;
CREATE TABLE `notice_read`  (
  `notice_id` bigint(0) NOT NULL COMMENT '公告ID，关联notice表',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID，关联user表',
  `read_at` datetime(0) NULL DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`notice_id`, `user_id`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE,
  CONSTRAINT `notice_read_ibfk_1` FOREIGN KEY (`notice_id`) REFERENCES `notice` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `notice_read_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '公告阅读记录表，存储公告阅读状态' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notice_read
-- ----------------------------
INSERT INTO `notice_read` VALUES (1, 4, '2026-01-30 16:03:43');

-- ----------------------------
-- Table structure for operation_log
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '日志ID，主键，自增',
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '用户ID，关联user表',
  `action` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作动作',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '操作详情',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求方法',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求IP',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作状态（SUCCESS/FAIL）',
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户角色',
  `cost_time` bigint(0) NULL DEFAULT NULL COMMENT '接口耗时（毫秒）',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE,
  CONSTRAINT `operation_log_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 671 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作日志表，存储用户操作记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of operation_log
-- ----------------------------
INSERT INTO `operation_log` VALUES (1, 2, '审核房产绑定', '物业张经理审核通过了居民王小明的 101室 绑定申请', 'POST', '192.168.1.100', 'SUCCESS', 'PROPERTY', 100, '2026-01-30 16:03:43');
INSERT INTO `operation_log` VALUES (483, 17, '报修acceptRepair', '管理员报修acceptRepair了ID: 9的信息', 'POST /api/repair/accept/9', '127.0.0.1', 'SUCCESS', 'ADMIN', 13, '2026-03-25 12:11:18');
INSERT INTO `operation_log` VALUES (484, 17, '报修completeRepair', '管理员报修completeRepair了ID: 9的信息', 'POST /api/repair/complete/9', '127.0.0.1', 'SUCCESS', 'ADMIN', 10, '2026-03-25 12:11:20');
INSERT INTO `operation_log` VALUES (485, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 1, '2026-03-25 12:22:11');
INSERT INTO `operation_log` VALUES (486, NULL, '认证登录', '管理员认证登录的信息', 'POST /api/auth/login', '127.0.0.1', 'SUCCESS', NULL, 11, '2026-03-25 12:22:11');
INSERT INTO `operation_log` VALUES (487, NULL, 'FileuploadAvatar', '管理员FileuploadAvatar的信息', 'POST /api/file/upload/avatar', '127.0.0.1', 'SUCCESS', NULL, 5, '2026-03-25 12:26:45');
INSERT INTO `operation_log` VALUES (488, 17, 'ProfileupdateProfile', '管理员ProfileupdateProfile的信息', 'PUT /api/user/profile', '127.0.0.1', 'SUCCESS', 'ADMIN', 7, '2026-03-25 12:26:45');
INSERT INTO `operation_log` VALUES (489, 17, 'ProfileupdateProfile', '管理员ProfileupdateProfile的信息', 'PUT /api/user/profile', '127.0.0.1', 'SUCCESS', 'ADMIN', 9, '2026-03-25 12:26:47');
INSERT INTO `operation_log` VALUES (490, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 1, '2026-03-25 12:38:33');
INSERT INTO `operation_log` VALUES (491, NULL, '认证登录', '管理员认证登录的信息', 'POST /api/auth/login', '127.0.0.1', 'SUCCESS', NULL, 29, '2026-03-25 12:38:34');
INSERT INTO `operation_log` VALUES (492, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 0, '2026-03-25 12:53:10');
INSERT INTO `operation_log` VALUES (493, NULL, '认证登录', '管理员认证登录的信息', 'POST /api/auth/login', '127.0.0.1', 'SUCCESS', NULL, 9, '2026-03-25 12:53:11');
INSERT INTO `operation_log` VALUES (494, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 2, '2026-03-26 13:42:09');
INSERT INTO `operation_log` VALUES (495, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 1, '2026-03-26 13:46:05');
INSERT INTO `operation_log` VALUES (496, NULL, '认证登录', '管理员认证登录的信息', 'POST /api/auth/login', '127.0.0.1', 'SUCCESS', NULL, 58, '2026-03-26 13:46:06');
INSERT INTO `operation_log` VALUES (497, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 0, '2026-03-26 14:36:29');
INSERT INTO `operation_log` VALUES (498, NULL, '认证登录', '管理员认证登录的信息', 'POST /api/auth/login', '127.0.0.1', 'SUCCESS', NULL, 13, '2026-03-26 14:36:30');
INSERT INTO `operation_log` VALUES (499, 4, 'ProfileupdateUserThemePreference', '用户ProfileupdateUserThemePreference了用户对象的信息', 'PUT /api/user/theme', '127.0.0.1', 'SUCCESS', 'RESIDENT', 12, '2026-03-26 14:38:18');
INSERT INTO `operation_log` VALUES (500, 4, 'ProfileupdateUserThemePreference', '用户ProfileupdateUserThemePreference了用户对象的信息', 'PUT /api/user/theme', '127.0.0.1', 'SUCCESS', 'RESIDENT', 5, '2026-03-26 14:38:24');
INSERT INTO `operation_log` VALUES (501, 4, 'ProfileupdateUserThemePreference', '用户ProfileupdateUserThemePreference了用户对象的信息', 'PUT /api/user/theme', '127.0.0.1', 'SUCCESS', 'RESIDENT', 5, '2026-03-26 14:38:25');
INSERT INTO `operation_log` VALUES (502, 4, 'ProfileupdateUserThemePreference', '用户ProfileupdateUserThemePreference了用户对象的信息', 'PUT /api/user/theme', '127.0.0.1', 'SUCCESS', 'RESIDENT', 3, '2026-03-26 14:38:26');
INSERT INTO `operation_log` VALUES (503, 4, 'ProfileupdateUserThemePreference', '用户ProfileupdateUserThemePreference了用户对象的信息', 'PUT /api/user/theme', '127.0.0.1', 'SUCCESS', 'RESIDENT', 1, '2026-03-26 14:38:28');
INSERT INTO `operation_log` VALUES (504, 4, 'ProfileupdateUserThemePreference', '用户ProfileupdateUserThemePreference了用户对象的信息', 'PUT /api/user/theme', '127.0.0.1', 'SUCCESS', 'RESIDENT', 7, '2026-03-26 14:39:06');
INSERT INTO `operation_log` VALUES (505, 4, 'ProfileupdateUserThemePreference', '用户ProfileupdateUserThemePreference了用户对象的信息', 'PUT /api/user/theme', '127.0.0.1', 'SUCCESS', 'RESIDENT', 6, '2026-03-26 14:39:08');
INSERT INTO `operation_log` VALUES (506, 4, 'ProfileupdateUserThemePreference', '用户ProfileupdateUserThemePreference了用户对象的信息', 'PUT /api/user/theme', '127.0.0.1', 'SUCCESS', 'RESIDENT', 8, '2026-03-26 14:39:09');
INSERT INTO `operation_log` VALUES (507, 4, 'ProfileupdateUserThemePreference', '用户ProfileupdateUserThemePreference了用户对象的信息', 'PUT /api/user/theme', '127.0.0.1', 'SUCCESS', 'RESIDENT', 5, '2026-03-26 14:39:14');
INSERT INTO `operation_log` VALUES (508, 4, 'ProfileupdateUserThemePreference', '用户ProfileupdateUserThemePreference了用户对象的信息', 'PUT /api/user/theme', '127.0.0.1', 'SUCCESS', 'RESIDENT', 6, '2026-03-26 14:39:17');
INSERT INTO `operation_log` VALUES (509, 4, 'ProfileupdateUserThemePreference', '用户ProfileupdateUserThemePreference了用户对象的信息', 'PUT /api/user/theme', '127.0.0.1', 'SUCCESS', 'RESIDENT', 5, '2026-03-26 14:39:19');
INSERT INTO `operation_log` VALUES (510, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 0, '2026-03-26 14:41:41');
INSERT INTO `operation_log` VALUES (511, NULL, '认证登录', '管理员认证登录的信息', 'POST /api/auth/login', '127.0.0.1', 'SUCCESS', NULL, 5, '2026-03-26 14:41:42');
INSERT INTO `operation_log` VALUES (512, 3, '预约confirmAppointment', '医生预约confirmAppointment了ID: 5的信息', 'PUT /api/appointment/confirm/5', '127.0.0.1', 'SUCCESS', 'DOCTOR', 13, '2026-03-26 16:26:06');
INSERT INTO `operation_log` VALUES (513, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 3, '2026-03-26 16:55:47');
INSERT INTO `operation_log` VALUES (514, NULL, '认证登录', '管理员认证登录的信息', 'POST /api/auth/login', '127.0.0.1', 'SUCCESS', NULL, 17, '2026-03-26 16:55:47');
INSERT INTO `operation_log` VALUES (515, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 1, '2026-03-27 13:37:53');
INSERT INTO `operation_log` VALUES (516, NULL, '认证登录', '管理员认证登录的信息', 'POST /api/auth/login', '127.0.0.1', 'SUCCESS', NULL, 113, '2026-03-27 13:37:54');
INSERT INTO `operation_log` VALUES (517, 2, 'FileuploadFile', '物业FileuploadFile的信息', 'POST /api/file/upload', '127.0.0.1', 'SUCCESS', 'PROPERTY', 5, '2026-03-27 13:41:59');
INSERT INTO `operation_log` VALUES (518, 2, '风险事件AI识别', '物业风险事件AI识别的信息', 'POST /api/risk/recognize', '127.0.0.1', 'SUCCESS', 'PROPERTY', 10645, '2026-03-27 13:42:10');
INSERT INTO `operation_log` VALUES (519, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 0, '2026-03-27 13:43:17');
INSERT INTO `operation_log` VALUES (520, NULL, '认证登录', '管理员认证登录的信息', 'POST /api/auth/login', '127.0.0.1', 'SUCCESS', NULL, 12, '2026-03-27 13:43:20');
INSERT INTO `operation_log` VALUES (521, 2, 'FileuploadFile', '物业FileuploadFile的信息', 'POST /api/file/upload', '127.0.0.1', 'SUCCESS', 'PROPERTY', 4, '2026-03-27 13:45:05');
INSERT INTO `operation_log` VALUES (522, 2, '风险事件AI识别', '物业风险事件AI识别的信息', 'POST /api/risk/recognize', '127.0.0.1', 'SUCCESS', 'PROPERTY', 8435, '2026-03-27 13:45:13');
INSERT INTO `operation_log` VALUES (523, 2, '风险事件上报风险', '物业风险事件上报风险的信息', 'POST /api/risk/report', '127.0.0.1', 'SUCCESS', 'PROPERTY', 9031, '2026-03-27 13:45:23');
INSERT INTO `operation_log` VALUES (524, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 0, '2026-03-27 13:47:12');
INSERT INTO `operation_log` VALUES (525, NULL, '认证登录', '管理员认证登录的信息', 'POST /api/auth/login', '127.0.0.1', 'SUCCESS', NULL, 5, '2026-03-27 13:47:13');
INSERT INTO `operation_log` VALUES (526, 4, '邻里消息createMessage', '用户邻里消息createMessage的信息', 'POST /api/neighbor-messages', '127.0.0.1', 'SUCCESS', 'RESIDENT', 8, '2026-03-27 13:48:01');
INSERT INTO `operation_log` VALUES (527, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 0, '2026-03-27 13:49:00');
INSERT INTO `operation_log` VALUES (528, NULL, '认证登录', '管理员认证登录的信息', 'POST /api/auth/login', '127.0.0.1', 'SUCCESS', NULL, 9, '2026-03-27 13:49:00');
INSERT INTO `operation_log` VALUES (529, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 1, '2026-03-27 13:52:10');
INSERT INTO `operation_log` VALUES (530, NULL, '认证登录', '管理员认证登录的信息', 'POST /api/auth/login', '127.0.0.1', 'SUCCESS', NULL, 7, '2026-03-27 13:52:11');
INSERT INTO `operation_log` VALUES (531, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 0, '2026-03-27 13:53:18');
INSERT INTO `operation_log` VALUES (532, NULL, '认证登录', '管理员认证登录的信息', 'POST /api/auth/login', '127.0.0.1', 'SUCCESS', NULL, 8, '2026-03-27 13:53:18');
INSERT INTO `operation_log` VALUES (533, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 1, '2026-03-27 13:53:40');
INSERT INTO `operation_log` VALUES (534, NULL, '认证登录', '管理员认证登录的信息', 'POST /api/auth/login', '127.0.0.1', 'SUCCESS', NULL, 11, '2026-03-27 13:53:41');
INSERT INTO `operation_log` VALUES (535, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 1, '2026-03-27 15:19:45');
INSERT INTO `operation_log` VALUES (536, NULL, '认证登录', '管理员认证登录的信息', 'POST /api/auth/login', '127.0.0.1', 'SUCCESS', NULL, 7, '2026-03-27 15:19:46');
INSERT INTO `operation_log` VALUES (537, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 0, '2026-03-27 15:29:46');
INSERT INTO `operation_log` VALUES (538, NULL, '认证登录', '管理员认证登录的信息', 'POST /api/auth/login', '127.0.0.1', 'SUCCESS', NULL, 7, '2026-03-27 15:29:47');
INSERT INTO `operation_log` VALUES (539, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 0, '2026-03-27 16:27:48');
INSERT INTO `operation_log` VALUES (540, NULL, '认证登录', '管理员认证登录的信息', 'POST /api/auth/login', '127.0.0.1', 'SUCCESS', NULL, 8, '2026-03-27 16:27:49');
INSERT INTO `operation_log` VALUES (541, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 2, '2026-04-07 15:42:26');
INSERT INTO `operation_log` VALUES (542, NULL, '认证登录', '管理员认证登录的信息', 'POST /api/auth/login', '127.0.0.1', 'SUCCESS', NULL, 81, '2026-04-07 15:42:27');
INSERT INTO `operation_log` VALUES (543, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 664, '2026-04-07 17:05:22');
INSERT INTO `operation_log` VALUES (544, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-07 17:05:23');
INSERT INTO `operation_log` VALUES (545, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 62, '2026-04-07 17:05:24');
INSERT INTO `operation_log` VALUES (546, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 44, '2026-04-07 17:05:25');
INSERT INTO `operation_log` VALUES (547, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-07 17:05:26');
INSERT INTO `operation_log` VALUES (548, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 44, '2026-04-07 17:05:27');
INSERT INTO `operation_log` VALUES (549, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 1, '2026-04-07 17:05:28');
INSERT INTO `operation_log` VALUES (550, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 43, '2026-04-07 17:05:29');
INSERT INTO `operation_log` VALUES (551, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-07 17:05:30');
INSERT INTO `operation_log` VALUES (552, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 187, '2026-04-07 17:12:22');
INSERT INTO `operation_log` VALUES (553, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 1, '2026-04-07 17:12:23');
INSERT INTO `operation_log` VALUES (554, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 46, '2026-04-07 17:12:24');
INSERT INTO `operation_log` VALUES (555, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-07 17:12:25');
INSERT INTO `operation_log` VALUES (556, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 45, '2026-04-07 17:12:26');
INSERT INTO `operation_log` VALUES (557, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 43, '2026-04-07 17:12:27');
INSERT INTO `operation_log` VALUES (558, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-07 17:12:28');
INSERT INTO `operation_log` VALUES (559, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 52, '2026-04-07 17:12:29');
INSERT INTO `operation_log` VALUES (560, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-07 17:12:30');
INSERT INTO `operation_log` VALUES (561, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 324, '2026-04-07 17:12:39');
INSERT INTO `operation_log` VALUES (562, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-07 17:12:40');
INSERT INTO `operation_log` VALUES (563, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 43, '2026-04-07 17:12:41');
INSERT INTO `operation_log` VALUES (564, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 64, '2026-04-07 17:12:42');
INSERT INTO `operation_log` VALUES (565, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 1, '2026-04-07 17:12:43');
INSERT INTO `operation_log` VALUES (566, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 52, '2026-04-07 17:12:44');
INSERT INTO `operation_log` VALUES (567, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 50, '2026-04-07 17:12:45');
INSERT INTO `operation_log` VALUES (568, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-07 17:12:46');
INSERT INTO `operation_log` VALUES (569, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 53, '2026-04-07 17:12:47');
INSERT INTO `operation_log` VALUES (570, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 1, '2026-04-07 17:12:48');
INSERT INTO `operation_log` VALUES (571, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 47, '2026-04-07 17:12:49');
INSERT INTO `operation_log` VALUES (572, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-07 17:12:50');
INSERT INTO `operation_log` VALUES (573, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 69, '2026-04-07 17:12:51');
INSERT INTO `operation_log` VALUES (574, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-07 17:12:52');
INSERT INTO `operation_log` VALUES (575, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 42, '2026-04-07 17:12:53');
INSERT INTO `operation_log` VALUES (576, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-07 17:12:54');
INSERT INTO `operation_log` VALUES (577, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 42, '2026-04-07 17:12:55');
INSERT INTO `operation_log` VALUES (578, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 283, '2026-04-07 17:54:00');
INSERT INTO `operation_log` VALUES (579, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-07 17:54:01');
INSERT INTO `operation_log` VALUES (580, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 59, '2026-04-07 17:54:02');
INSERT INTO `operation_log` VALUES (581, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 63, '2026-04-07 17:54:03');
INSERT INTO `operation_log` VALUES (582, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-07 17:54:04');
INSERT INTO `operation_log` VALUES (583, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 60, '2026-04-07 17:54:05');
INSERT INTO `operation_log` VALUES (584, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 59, '2026-04-07 17:54:06');
INSERT INTO `operation_log` VALUES (585, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-07 17:54:07');
INSERT INTO `operation_log` VALUES (586, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 52, '2026-04-07 17:54:08');
INSERT INTO `operation_log` VALUES (587, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 93, '2026-04-07 18:23:57');
INSERT INTO `operation_log` VALUES (588, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 53, '2026-04-07 18:23:58');
INSERT INTO `operation_log` VALUES (589, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-07 18:23:59');
INSERT INTO `operation_log` VALUES (590, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 48, '2026-04-07 18:24:00');
INSERT INTO `operation_log` VALUES (591, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-07 18:24:01');
INSERT INTO `operation_log` VALUES (592, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 147, '2026-04-07 18:24:02');
INSERT INTO `operation_log` VALUES (593, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 288, '2026-04-07 18:35:02');
INSERT INTO `operation_log` VALUES (594, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 1, '2026-04-07 18:35:02');
INSERT INTO `operation_log` VALUES (595, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 55, '2026-04-07 18:35:03');
INSERT INTO `operation_log` VALUES (596, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 42, '2026-04-07 18:35:04');
INSERT INTO `operation_log` VALUES (597, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-07 18:35:05');
INSERT INTO `operation_log` VALUES (598, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 48, '2026-04-07 18:35:06');
INSERT INTO `operation_log` VALUES (599, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-07 18:35:07');
INSERT INTO `operation_log` VALUES (600, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 47, '2026-04-07 18:35:08');
INSERT INTO `operation_log` VALUES (601, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-07 18:35:09');
INSERT INTO `operation_log` VALUES (602, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 54, '2026-04-07 18:35:10');
INSERT INTO `operation_log` VALUES (603, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 1, '2026-04-07 18:35:11');
INSERT INTO `operation_log` VALUES (604, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 55, '2026-04-07 18:35:12');
INSERT INTO `operation_log` VALUES (605, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 56, '2026-04-07 18:35:13');
INSERT INTO `operation_log` VALUES (606, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 1, '2026-04-07 18:35:14');
INSERT INTO `operation_log` VALUES (607, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 50, '2026-04-07 18:35:15');
INSERT INTO `operation_log` VALUES (608, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 1, '2026-04-07 18:35:16');
INSERT INTO `operation_log` VALUES (609, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 64, '2026-04-07 18:35:17');
INSERT INTO `operation_log` VALUES (610, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-07 18:35:18');
INSERT INTO `operation_log` VALUES (611, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 46, '2026-04-07 18:35:19');
INSERT INTO `operation_log` VALUES (612, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 1, '2026-04-07 18:35:20');
INSERT INTO `operation_log` VALUES (613, 2, 'FileuploadFile', '物业FileuploadFile的信息', 'POST /api/file/upload', '127.0.0.1', 'SUCCESS', 'PROPERTY', 19, '2026-04-07 18:37:56');
INSERT INTO `operation_log` VALUES (614, 2, 'FileuploadFile', '物业FileuploadFile的信息', 'POST /api/file/upload', '127.0.0.1', 'SUCCESS', 'PROPERTY', 9, '2026-04-07 18:38:08');
INSERT INTO `operation_log` VALUES (615, 2, 'FileuploadFile', '物业FileuploadFile的信息', 'POST /api/file/upload', '127.0.0.1', 'SUCCESS', 'PROPERTY', 12, '2026-04-07 18:39:04');
INSERT INTO `operation_log` VALUES (616, 2, 'FileuploadFile', '物业FileuploadFile的信息', 'POST /api/file/upload', '127.0.0.1', 'SUCCESS', 'PROPERTY', 9, '2026-04-07 18:57:12');
INSERT INTO `operation_log` VALUES (617, 2, 'FileuploadFile', '物业FileuploadFile的信息', 'POST /api/file/upload', '127.0.0.1', 'SUCCESS', 'PROPERTY', 6, '2026-04-07 18:59:08');
INSERT INTO `operation_log` VALUES (618, 2, '风险事件AI识别', '物业风险事件AI识别的信息', 'POST /api/risk/recognize', '127.0.0.1', 'SUCCESS', 'PROPERTY', 9093, '2026-04-07 18:59:17');
INSERT INTO `operation_log` VALUES (619, 2, '风险事件上报风险', '物业风险事件上报风险的信息', 'POST /api/risk/report', '127.0.0.1', 'SUCCESS', 'PROPERTY', 6822, '2026-04-07 18:59:24');
INSERT INTO `operation_log` VALUES (620, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 2, '2026-04-08 13:33:50');
INSERT INTO `operation_log` VALUES (621, NULL, '认证登录', '管理员认证登录的信息', 'POST /api/auth/login', '127.0.0.1', 'SUCCESS', NULL, 76, '2026-04-08 13:33:51');
INSERT INTO `operation_log` VALUES (622, NULL, 'FileuploadActivityPoster', '管理员FileuploadActivityPoster的信息', 'POST /api/file/upload/activity', '127.0.0.1', 'SUCCESS', NULL, 7, '2026-04-08 14:33:18');
INSERT INTO `operation_log` VALUES (623, 2, '活动updateActivity', '物业活动updateActivity的信息\n异常信息: 活动不存在', 'PUT /api/activity/496', '127.0.0.1', 'FAIL', 'PROPERTY', NULL, '2026-04-08 14:33:22');
INSERT INTO `operation_log` VALUES (624, NULL, 'FileuploadActivityPoster', '管理员FileuploadActivityPoster的信息', 'POST /api/file/upload/activity', '127.0.0.1', 'SUCCESS', NULL, 10, '2026-04-08 15:12:30');
INSERT INTO `operation_log` VALUES (625, NULL, 'FileuploadActivityPoster', '管理员FileuploadActivityPoster的信息', 'POST /api/file/upload/activity', '127.0.0.1', 'SUCCESS', NULL, 6, '2026-04-08 15:13:21');
INSERT INTO `operation_log` VALUES (626, NULL, 'FileuploadActivityPoster', '管理员FileuploadActivityPoster的信息', 'POST /api/file/upload/activity', '127.0.0.1', 'SUCCESS', NULL, 4, '2026-04-08 15:43:02');
INSERT INTO `operation_log` VALUES (627, 2, '活动updateActivity', '物业活动updateActivity的信息', 'PUT /api/activity', '127.0.0.1', 'SUCCESS', 'PROPERTY', 21, '2026-04-08 15:43:02');
INSERT INTO `operation_log` VALUES (628, NULL, 'FileuploadActivityPoster', '管理员FileuploadActivityPoster的信息', 'POST /api/file/upload/activity', '127.0.0.1', 'SUCCESS', NULL, 3, '2026-04-08 15:50:53');
INSERT INTO `operation_log` VALUES (629, 2, '活动updateActivity', '物业活动updateActivity的信息', 'PUT /api/activity', '127.0.0.1', 'SUCCESS', 'PROPERTY', 10, '2026-04-08 15:50:56');
INSERT INTO `operation_log` VALUES (630, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 1, '2026-04-10 15:56:50');
INSERT INTO `operation_log` VALUES (631, NULL, '认证登录', '管理员认证登录的信息', 'POST /api/auth/login', '127.0.0.1', 'SUCCESS', NULL, 92, '2026-04-10 15:56:51');
INSERT INTO `operation_log` VALUES (632, 4, 'ProfileupdateUserThemePreference', '用户ProfileupdateUserThemePreference了用户对象的信息', 'PUT /api/user/theme', '127.0.0.1', 'SUCCESS', 'RESIDENT', 12, '2026-04-10 16:00:10');
INSERT INTO `operation_log` VALUES (633, 4, 'ProfileupdateUserThemePreference', '用户ProfileupdateUserThemePreference了用户对象的信息', 'PUT /api/user/theme', '127.0.0.1', 'SUCCESS', 'RESIDENT', 6, '2026-04-10 16:00:11');
INSERT INTO `operation_log` VALUES (634, 4, 'ProfileupdateUserThemePreference', '用户ProfileupdateUserThemePreference了用户对象的信息', 'PUT /api/user/theme', '127.0.0.1', 'SUCCESS', 'RESIDENT', 7, '2026-04-10 16:00:13');
INSERT INTO `operation_log` VALUES (635, 4, 'ProfileupdateUserThemePreference', '用户ProfileupdateUserThemePreference了用户对象的信息', 'PUT /api/user/theme', '127.0.0.1', 'SUCCESS', 'RESIDENT', 8, '2026-04-10 16:00:13');
INSERT INTO `operation_log` VALUES (636, 4, 'ProfileupdateUserThemePreference', '用户ProfileupdateUserThemePreference了用户对象的信息', 'PUT /api/user/theme', '127.0.0.1', 'SUCCESS', 'RESIDENT', 9, '2026-04-10 16:00:21');
INSERT INTO `operation_log` VALUES (637, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 0, '2026-04-10 16:01:02');
INSERT INTO `operation_log` VALUES (638, NULL, '认证登录', '管理员认证登录的信息\n异常信息: 用户不存在', 'POST /api/auth/login', '127.0.0.1', 'FAIL', NULL, NULL, '2026-04-10 16:01:03');
INSERT INTO `operation_log` VALUES (639, NULL, '认证登录', '管理员认证登录的信息\n异常信息: 验证码不存在或已过期，请重新获取', 'POST /api/auth/login', '127.0.0.1', 'FAIL', NULL, NULL, '2026-04-10 16:01:09');
INSERT INTO `operation_log` VALUES (640, NULL, '认证登录', '管理员认证登录的信息\n异常信息: 验证码不存在或已过期，请重新获取', 'POST /api/auth/login', '127.0.0.1', 'FAIL', NULL, NULL, '2026-04-10 16:01:19');
INSERT INTO `operation_log` VALUES (641, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 0, '2026-04-10 16:02:12');
INSERT INTO `operation_log` VALUES (642, NULL, '认证登录', '管理员认证登录的信息', 'POST /api/auth/login', '127.0.0.1', 'SUCCESS', NULL, 8, '2026-04-10 16:02:14');
INSERT INTO `operation_log` VALUES (643, NULL, 'FileuploadActivityPoster', '管理员FileuploadActivityPoster的信息', 'POST /api/file/upload/activity', '127.0.0.1', 'SUCCESS', NULL, 3, '2026-04-10 16:06:41');
INSERT INTO `operation_log` VALUES (644, 2, '活动updateActivity', '物业活动updateActivity的信息', 'PUT /api/activity', '127.0.0.1', 'SUCCESS', 'PROPERTY', 17, '2026-04-10 16:06:43');
INSERT INTO `operation_log` VALUES (645, NULL, 'FileuploadActivityPoster', '管理员FileuploadActivityPoster的信息', 'POST /api/file/upload/activity', '127.0.0.1', 'SUCCESS', NULL, 2, '2026-04-10 16:07:53');
INSERT INTO `operation_log` VALUES (646, 2, '活动updateActivity', '物业活动updateActivity的信息', 'PUT /api/activity', '127.0.0.1', 'SUCCESS', 'PROPERTY', 7, '2026-04-10 16:08:06');
INSERT INTO `operation_log` VALUES (647, 2, 'FileuploadFile', '物业FileuploadFile的信息', 'POST /api/file/upload', '127.0.0.1', 'SUCCESS', 'PROPERTY', 5, '2026-04-10 16:09:14');
INSERT INTO `operation_log` VALUES (648, 2, '风险事件AI识别', '物业风险事件AI识别的信息', 'POST /api/risk/recognize', '127.0.0.1', 'SUCCESS', 'PROPERTY', 10657, '2026-04-10 16:09:25');
INSERT INTO `operation_log` VALUES (649, 2, 'FileuploadFile', '物业FileuploadFile的信息', 'POST /api/file/upload', '127.0.0.1', 'SUCCESS', 'PROPERTY', 3, '2026-04-10 16:09:41');
INSERT INTO `operation_log` VALUES (650, 2, '风险事件AI识别', '物业风险事件AI识别的信息', 'POST /api/risk/recognize', '127.0.0.1', 'SUCCESS', 'PROPERTY', 7186, '2026-04-10 16:09:48');
INSERT INTO `operation_log` VALUES (651, 2, '风险事件上报风险', '物业风险事件上报风险的信息', 'POST /api/risk/report', '127.0.0.1', 'SUCCESS', 'PROPERTY', 6596, '2026-04-10 16:09:55');
INSERT INTO `operation_log` VALUES (652, NULL, '认证sendCode', '管理员认证sendCode的信息', 'POST /api/auth/send-code', '127.0.0.1', 'SUCCESS', NULL, 1, '2026-04-15 13:06:32');
INSERT INTO `operation_log` VALUES (653, NULL, '认证登录', '管理员认证登录的信息', 'POST /api/auth/login', '127.0.0.1', 'SUCCESS', NULL, 75, '2026-04-15 13:06:33');
INSERT INTO `operation_log` VALUES (654, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 510, '2026-04-15 13:06:42');
INSERT INTO `operation_log` VALUES (655, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-15 13:06:42');
INSERT INTO `operation_log` VALUES (656, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 50, '2026-04-15 13:06:43');
INSERT INTO `operation_log` VALUES (657, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 58, '2026-04-15 13:06:44');
INSERT INTO `operation_log` VALUES (658, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-15 13:06:45');
INSERT INTO `operation_log` VALUES (659, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 56, '2026-04-15 13:06:46');
INSERT INTO `operation_log` VALUES (660, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-15 13:06:47');
INSERT INTO `operation_log` VALUES (661, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 61, '2026-04-15 13:06:48');
INSERT INTO `operation_log` VALUES (662, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 1, '2026-04-15 13:06:49');
INSERT INTO `operation_log` VALUES (663, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 199, '2026-04-15 13:16:59');
INSERT INTO `operation_log` VALUES (664, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-15 13:17:00');
INSERT INTO `operation_log` VALUES (665, 2, 'FileuploadMonitorSnapshot', '物业FileuploadMonitorSnapshot的信息', 'POST /api/file/upload/monitor', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 9, '2026-04-15 13:17:00');
INSERT INTO `operation_log` VALUES (666, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 67, '2026-04-15 13:17:01');
INSERT INTO `operation_log` VALUES (667, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 0, '2026-04-15 13:17:02');
INSERT INTO `operation_log` VALUES (668, 2, 'FileuploadMonitorSnapshot', '物业FileuploadMonitorSnapshot的信息', 'POST /api/file/upload/monitor', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 3, '2026-04-15 13:17:02');
INSERT INTO `operation_log` VALUES (669, 2, 'FileuploadMonitorSnapshot', '物业FileuploadMonitorSnapshot的信息', 'POST /api/file/upload/monitor', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 3, '2026-04-15 13:17:02');
INSERT INTO `operation_log` VALUES (670, 2, 'AlarmdetectBehavior', '物业AlarmdetectBehavior的信息', 'POST /api/alarm/detect', '0:0:0:0:0:0:0:1', 'SUCCESS', 'PROPERTY', 66, '2026-04-15 13:17:03');

-- ----------------------------
-- Table structure for permission
-- ----------------------------
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '权限名称',
  `permission_key` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '权限标识（如 repair:assign）',
  `type` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT 'api' COMMENT '权限类型：menu/button/api',
  `path` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '前端路由路径',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `permission_key`(`permission_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of permission
-- ----------------------------
INSERT INTO `permission` VALUES (1, '用户管理-查看', 'user:view', 'api', NULL, '2026-02-24 12:05:41');
INSERT INTO `permission` VALUES (2, '用户管理-新增', 'user:add', 'api', NULL, '2026-02-24 12:05:41');
INSERT INTO `permission` VALUES (3, '用户管理-编辑', 'user:edit', 'api', NULL, '2026-02-24 12:05:41');
INSERT INTO `permission` VALUES (4, '用户管理-删除', 'user:delete', 'api', NULL, '2026-02-24 12:05:41');
INSERT INTO `permission` VALUES (5, '报修查看', 'repair:view', 'api', NULL, '2026-02-24 12:05:41');
INSERT INTO `permission` VALUES (6, '报修分配', 'repair:assign', 'api', NULL, '2026-02-24 12:05:41');
INSERT INTO `permission` VALUES (7, '报修处理', 'repair:process', 'api', NULL, '2026-02-24 12:05:41');
INSERT INTO `permission` VALUES (8, '公告管理', 'notice:manage', 'api', NULL, '2026-02-24 12:05:41');
INSERT INTO `permission` VALUES (9, '活动管理', 'activity:manage', 'api', NULL, '2026-02-24 12:05:41');
INSERT INTO `permission` VALUES (10, '健康档案管理', 'health:manage', 'api', NULL, '2026-02-24 12:05:41');

-- ----------------------------
-- Table structure for repair
-- ----------------------------
DROP TABLE IF EXISTS `repair`;
CREATE TABLE `repair`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '报修ID，主键，自增',
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '用户ID，关联user表',
  `house_id` bigint(0) NULL DEFAULT NULL COMMENT '房屋ID，关联house表',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '问题描述',
  `status` enum('pending','assigned','processing','completed','cancelled') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'pending' COMMENT '报修状态：pending-待派单，assigned-已派单/待接单，processing-处理中，completed-已完成，cancelled-已取消',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `completed_at` datetime(0) NULL DEFAULT NULL COMMENT '完成时间',
  `assigned_to` bigint(0) NULL DEFAULT NULL COMMENT '分配的维修师傅ID',
  `assigned_at` datetime(0) NULL DEFAULT NULL COMMENT '分配时间',
  `started_at` datetime(0) NULL DEFAULT NULL COMMENT '开始处理时间',
  `rating` int(0) NULL DEFAULT NULL COMMENT '服务评分（1-5分）',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '服务评价内容',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE,
  INDEX `house_id`(`house_id`) USING BTREE,
  INDEX `assigned_to`(`assigned_to`) USING BTREE,
  CONSTRAINT `fk_repair_assigned_to` FOREIGN KEY (`assigned_to`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `repair_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `repair_ibfk_2` FOREIGN KEY (`house_id`) REFERENCES `house` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报修表，存储报修申请信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of repair
-- ----------------------------
INSERT INTO `repair` VALUES (1, 4, 1, '厨房水龙头漏水严重，请尽快维修。', 'completed', '2026-01-30 16:03:43', '2026-01-30 18:01:29', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `repair` VALUES (2, 4, 1, '厨房水龙头又漏水，请尽快维修。', 'completed', '2026-02-18 14:53:24', '2026-02-18 15:25:03', 17, '2026-02-18 14:53:48', '2026-02-18 14:53:53', NULL, NULL);
INSERT INTO `repair` VALUES (3, 4, 1, '厨房水管漏水，需要紧急维修', 'processing', '2026-03-07 16:08:54', NULL, 19, '2026-03-08 15:32:09', NULL, NULL, NULL);
INSERT INTO `repair` VALUES (4, 4, 1, '厨房水管漏水，需要紧急维修', 'pending', '2026-03-07 16:09:18', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `repair` VALUES (6, 4, 1, '测试取消的报修', 'cancelled', '2026-03-07 16:09:34', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `repair` VALUES (8, 4, 1, '测试取消的报修', 'cancelled', '2026-03-07 16:09:57', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `repair` VALUES (9, 4, 1, '厨房水龙头漏水了', 'completed', '2026-03-25 10:52:06', '2026-03-25 12:11:20', 17, '2026-03-25 11:01:41', '2026-03-25 12:11:18', NULL, NULL);

-- ----------------------------
-- Table structure for resident_house
-- ----------------------------
DROP TABLE IF EXISTS `resident_house`;
CREATE TABLE `resident_house`  (
  `user_id` bigint(0) NOT NULL COMMENT '用户ID，关联user表',
  `house_id` bigint(0) NOT NULL COMMENT '房屋ID，关联house表',
  `relation` enum('owner','tenant','family') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '与房屋关系：owner-业主，tenant-租户，family-家属',
  `certified` tinyint(0) NULL DEFAULT 0 COMMENT '认证状态：0-未认证，1-已认证',
  `cert_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '房产证号',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '房产详细地址',
  PRIMARY KEY (`user_id`, `house_id`) USING BTREE,
  INDEX `house_id`(`house_id`) USING BTREE,
  CONSTRAINT `resident_house_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `resident_house_ibfk_2` FOREIGN KEY (`house_id`) REFERENCES `house` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '居民房产关联表，存储居民与房产的绑定关系' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of resident_house
-- ----------------------------
INSERT INTO `resident_house` VALUES (4, 1, 'owner', 1, NULL, NULL);
INSERT INTO `resident_house` VALUES (4, 3, 'tenant', 1, NULL, NULL);
INSERT INTO `resident_house` VALUES (4, 4, 'tenant', 1, NULL, NULL);
INSERT INTO `resident_house` VALUES (7, 5, 'family', 1, NULL, NULL);
INSERT INTO `resident_house` VALUES (8, 6, 'owner', 1, NULL, NULL);
INSERT INTO `resident_house` VALUES (9, 7, 'owner', 1, NULL, NULL);
INSERT INTO `resident_house` VALUES (10, 8, 'owner', 1, NULL, NULL);
INSERT INTO `resident_house` VALUES (11, 9, 'owner', 1, NULL, NULL);
INSERT INTO `resident_house` VALUES (12, 10, 'owner', 1, NULL, NULL);
INSERT INTO `resident_house` VALUES (13, 11, 'owner', 1, NULL, NULL);
INSERT INTO `resident_house` VALUES (14, 12, 'owner', 1, NULL, NULL);
INSERT INTO `resident_house` VALUES (15, 13, 'family', 1, NULL, NULL);
INSERT INTO `resident_house` VALUES (16, 14, 'tenant', 1, NULL, NULL);

-- ----------------------------
-- Table structure for risk_event
-- ----------------------------
DROP TABLE IF EXISTS `risk_event`;
CREATE TABLE `risk_event`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '事件标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '事件描述',
  `risk_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '风险类型',
  `risk_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '风险等级（LOW/MEDIUM/HIGH）',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片地址',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING' COMMENT '处理状态（PENDING/PROCESSING/DONE）',
  `reporter_id` bigint(0) NULL DEFAULT NULL COMMENT '上报人ID',
  `handler_id` bigint(0) NULL DEFAULT NULL COMMENT '处理人ID',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `ai_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `ai_confidence` double NULL DEFAULT NULL,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '椋庨櫓鍙戠敓浣嶇疆',
  `glm_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `baidu_labels` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `reporter_id`(`reporter_id`) USING BTREE,
  INDEX `handler_id`(`handler_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_risk_level`(`risk_level`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  CONSTRAINT `risk_event_ibfk_1` FOREIGN KEY (`reporter_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `risk_event_ibfk_2` FOREIGN KEY (`handler_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '风险事件表，存储社区风险识别和上报信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of risk_event
-- ----------------------------
INSERT INTO `risk_event` VALUES (11, '老人摔倒', '系统检测到可能存在【老人摔倒】，风险等级：高风险，疑似人员摔倒情况，建议物业人员及时前往查看并提供必要帮助。', '老人摔倒', 'HIGH', '/upload/images/e9629fed-4020-4dd7-98ce-4cb408d87961.png', 'PENDING', 4, NULL, '2026-03-20 13:49:18', '2026-03-20 13:49:18', 'AI 识别结果：风险类型=老人摔倒, 风险等级=HIGH', NULL, NULL, '儿童活动区', '\n存在【老人摔倒】,具体情况是图片中有一位老人躺在地上。', '美女 人物活动 瑜伽 女孩 沙发');
INSERT INTO `risk_event` VALUES (12, '摄像头 1 - 花园区域 - 老人摔倒', '系统检测到可能存在【老人摔倒】，风险等级：高风险，疑似人员摔倒情况，建议物业人员及时前往查看并提供必要帮助。', '老人摔倒', 'HIGH', '/upload/images/7e08b792-f204-4ab3-9dc6-092a3285286f.png', 'PENDING', 2, NULL, '2026-03-20 15:23:33', '2026-03-20 15:23:33', 'AI 识别结果：风险类型=老人摔倒, 风险等级=HIGH', NULL, NULL, '花园区域', '\n存在【老人摔倒】,具体情况是图片中有一名老人躺在草地上，处于摔倒状态。', '卡通动漫人物 合照 喷农药 情侣 光头强');
INSERT INTO `risk_event` VALUES (13, '摄像头 1 - 花园区域 - 老人摔倒', '系统检测到可能存在【老人摔倒】，风险等级：高风险，疑似人员摔倒情况，建议物业人员及时前往查看并提供必要帮助。', '老人摔倒', 'HIGH', '/upload/images/61a79454-9826-4812-85f4-54b51fff330e.png', 'PENDING', 2, NULL, '2026-03-27 13:45:14', '2026-03-27 13:45:14', 'AI 识别结果：风险类型=老人摔倒, 风险等级=HIGH', NULL, NULL, '花园区域', '\n存在【老人摔倒】,具体情况是图片中有一位老人躺在草地上。', '卡通动漫人物 合照 喷农药 情侣 光头强');
INSERT INTO `risk_event` VALUES (14, '摄像头 1 - 花园区域 - 老人摔倒', '系统检测到可能存在【老人摔倒】，风险等级：高风险，疑似人员摔倒情况，建议物业人员及时前往查看并提供必要帮助。', '老人摔倒', 'HIGH', '/upload/images/8a2f1456-7147-4ad0-a636-62e4fa75c6ee.png', 'PENDING', 2, NULL, '2026-04-07 18:59:17', '2026-04-07 18:59:17', 'AI 识别结果：风险类型=老人摔倒, 风险等级=HIGH', NULL, NULL, '花园区域', '\n存在【老人摔倒】,具体情况是图片中有一位老人躺在草地上。', '卡通动漫人物 合照 喷农药 情侣 光头强');
INSERT INTO `risk_event` VALUES (15, '摄像头 3 - 楼道 - 火灾风险', '系统检测到可能存在【火灾风险】，风险等级：高风险，疑似火灾或烟雾情况，存在严重安全隐患，建议物业人员立即前往查看并处理。', '火灾风险', 'HIGH', '/upload/images/951d09c2-2516-4ae8-b3af-5b2b29744dac.png', 'PENDING', 2, NULL, '2026-04-10 16:09:49', '2026-04-10 16:09:49', 'AI 识别结果：风险类型=火灾风险, 风险等级=HIGH', NULL, NULL, '楼道', '\n存在【火灾烟雾】,具体情况是有明火、浓烟、燃烧', '熊熊大火 森林火灾 大火 篝火 爆炸现场');

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '角色ID，主键，自增',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称，唯一标识',
  `description` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色描述',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表，存储系统角色信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1, 'ROLE_ADMIN', '系统超级管理员');
INSERT INTO `role` VALUES (2, 'ROLE_PROPERTY', '社区物业管理人员');
INSERT INTO `role` VALUES (3, 'ROLE_DOCTOR', '社区医生');
INSERT INTO `role` VALUES (4, 'ROLE_RESIDENT', '社区居民');
INSERT INTO `role` VALUES (5, 'ROLE_PROPERTY_STAFF', '社区物业工作人员');
INSERT INTO `role` VALUES (6, 'ROLE_PROPERTY_TECHNICIAN', '社区物业维修师傅');

-- ----------------------------
-- Table structure for role_permission
-- ----------------------------
DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE `role_permission`  (
  `role_id` bigint(0) NOT NULL,
  `permission_id` bigint(0) NOT NULL,
  PRIMARY KEY (`role_id`, `permission_id`) USING BTREE,
  INDEX `fk_role_permission_permission`(`permission_id`) USING BTREE,
  CONSTRAINT `fk_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role_permission
-- ----------------------------
INSERT INTO `role_permission` VALUES (1, 1);
INSERT INTO `role_permission` VALUES (2, 1);
INSERT INTO `role_permission` VALUES (3, 1);
INSERT INTO `role_permission` VALUES (5, 1);
INSERT INTO `role_permission` VALUES (1, 2);
INSERT INTO `role_permission` VALUES (1, 3);
INSERT INTO `role_permission` VALUES (1, 4);
INSERT INTO `role_permission` VALUES (1, 5);
INSERT INTO `role_permission` VALUES (2, 5);
INSERT INTO `role_permission` VALUES (1, 6);
INSERT INTO `role_permission` VALUES (2, 6);
INSERT INTO `role_permission` VALUES (1, 7);
INSERT INTO `role_permission` VALUES (2, 7);
INSERT INTO `role_permission` VALUES (5, 7);
INSERT INTO `role_permission` VALUES (1, 8);
INSERT INTO `role_permission` VALUES (2, 8);
INSERT INTO `role_permission` VALUES (1, 9);
INSERT INTO `role_permission` VALUES (2, 9);
INSERT INTO `role_permission` VALUES (4, 9);
INSERT INTO `role_permission` VALUES (1, 10);
INSERT INTO `role_permission` VALUES (3, 10);

-- ----------------------------
-- Table structure for technician_skill
-- ----------------------------
DROP TABLE IF EXISTS `technician_skill`;
CREATE TABLE `technician_skill`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '技能ID，主键，自增',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '技能编码，唯一标识',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '技能名称',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '技能描述',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `code`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '维修师傅技能表，存储所有可用的维修技能类型' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of technician_skill
-- ----------------------------
INSERT INTO `technician_skill` VALUES (1, 'WATER', '水暖维修', '包含龙头、管道等', '2026-02-18 14:50:40');
INSERT INTO `technician_skill` VALUES (2, 'ELECTRIC', '电路维修', '包含灯具、开关等', '2026-02-18 14:50:40');
INSERT INTO `technician_skill` VALUES (3, 'AC', '空调维修', '包含空调、制冷设备等', '2026-02-18 14:50:40');
INSERT INTO `technician_skill` VALUES (4, 'ELEVATOR', '电梯维修', '包含电梯、升降设备等', '2026-02-18 14:50:40');
INSERT INTO `technician_skill` VALUES (5, 'GAS', '燃气维修', '包含燃气管道、热水器等', '2026-02-18 14:50:40');

-- ----------------------------
-- Table structure for technician_skill_relation
-- ----------------------------
DROP TABLE IF EXISTS `technician_skill_relation`;
CREATE TABLE `technician_skill_relation`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '关联ID，主键，自增',
  `user_id` bigint(0) NOT NULL COMMENT '维修师傅ID，关联user表',
  `skill_id` bigint(0) NOT NULL COMMENT '技能ID，关联technician_skill表',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE,
  INDEX `skill_id`(`skill_id`) USING BTREE,
  CONSTRAINT `technician_skill_relation_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `technician_skill_relation_ibfk_2` FOREIGN KEY (`skill_id`) REFERENCES `technician_skill` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '维修师傅技能关联表，实现师傅与技能的多对多关系' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of technician_skill_relation
-- ----------------------------
INSERT INTO `technician_skill_relation` VALUES (1, 17, 1, '2026-02-18 09:00:00');
INSERT INTO `technician_skill_relation` VALUES (2, 17, 2, '2026-02-18 09:00:00');
INSERT INTO `technician_skill_relation` VALUES (3, 18, 3, '2026-02-18 09:00:00');
INSERT INTO `technician_skill_relation` VALUES (4, 18, 5, '2026-02-18 09:00:00');
INSERT INTO `technician_skill_relation` VALUES (5, 19, 4, '2026-02-18 09:00:00');
INSERT INTO `technician_skill_relation` VALUES (6, 19, 2, '2026-02-18 09:00:00');
INSERT INTO `technician_skill_relation` VALUES (7, 20, 1, '2026-02-18 09:00:00');
INSERT INTO `technician_skill_relation` VALUES (8, 20, 5, '2026-02-18 09:00:00');
INSERT INTO `technician_skill_relation` VALUES (9, 21, 3, '2026-02-18 09:00:00');
INSERT INTO `technician_skill_relation` VALUES (10, 21, 4, '2026-02-18 09:00:00');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键，自增',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号，唯一标识',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录密码，加密存储',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户姓名',
  `gender` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '性别：0-女，1-男',
  `birthday` date NULL DEFAULT NULL COMMENT '出生日期',
  `id_card` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号',
  `type` enum('resident','property','doctor','technician','admin') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'resident' COMMENT '用户类型：resident-居民，property-物业，doctor-医生，technician-维修师傅，admin-管理员',
  `status` tinyint(0) NULL DEFAULT 1 COMMENT '账号状态：0-禁用，1-启用',
  `tags` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '重点人群分类标签',
  `theme_mode` enum('light','dark','system') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'system' COMMENT '主题模式：light-浅色，dark-深色，system-跟随系统',
  `theme_color` enum('teal','blue','purple','rose','orange','green') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'teal' COMMENT '主题颜色：teal-青色，blue-蓝色，purple-紫色，rose-玫红，orange-橙色，green-绿色',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像地址',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `phone`(`phone`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表，存储系统所有用户信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '13800000001', '123456', '系统管理员', NULL, NULL, NULL, 'admin', 1, NULL, 'system', 'teal', NULL, '2026-01-30 16:03:43');
INSERT INTO `user` VALUES (2, '13800000002', '$2a$10$.BT8psGv.v5ahlFxVQBNg./ticQuTsEl0L.OA2byXPAkUXB1VPaqe', '物业张经理', '1', '2026-02-16', NULL, 'property', 1, NULL, 'system', 'orange', '/upload/avatar/6c49d942-7a7f-4ff1-a00c-16154e2be748.png', '2026-01-30 16:03:43');
INSERT INTO `user` VALUES (3, '13800000003', '123456', '李医生', NULL, NULL, NULL, 'doctor', 1, NULL, 'system', 'teal', NULL, '2026-01-30 16:03:43');
INSERT INTO `user` VALUES (4, '13800000004', '123456', '王明', '1', '2000-03-09', '110101199201011234', 'resident', 1, NULL, 'light', 'teal', '/upload/avatar/781bf963-f4ad-417d-a889-76387da67e8c.jpg', '2026-01-30 16:03:43');
INSERT INTO `user` VALUES (5, '13800001001', '123456', '张医生', '1', '1980-05-12', '110101198005123456', 'doctor', 1, NULL, 'system', 'teal', NULL, '2026-02-11 12:58:24');
INSERT INTO `user` VALUES (6, '13800001002', '123456', '李医生', '0', '1985-09-20', '110101198509203333', 'doctor', 1, NULL, 'system', 'teal', NULL, '2026-02-11 12:58:24');
INSERT INTO `user` VALUES (7, '13900002001', '123456', 'TestName', '1', '2015-03-10', '110101201503101111', 'resident', 1, 'child', 'system', 'teal', NULL, '2026-02-11 12:58:24');
INSERT INTO `user` VALUES (8, '13900002002', '123456', '刘晓红', '0', '1998-07-22', '110101199807223333', 'resident', 1, NULL, 'system', 'teal', NULL, '2026-02-11 12:58:24');
INSERT INTO `user` VALUES (9, '13900002003', '123456', '赵强', '1', '1990-11-02', '110101199011024444', 'resident', 1, NULL, 'system', 'teal', NULL, '2026-02-11 12:58:24');
INSERT INTO `user` VALUES (10, '13900002004', '123456', '孙丽', '0', '1988-01-15', '110101198801155555', 'resident', 1, 'pregnant', 'system', 'teal', NULL, '2026-02-11 12:58:24');
INSERT INTO `user` VALUES (11, '13900002005', '123456', '周建国', '1', '1970-06-08', '110101197006086666', 'resident', 1, 'chronic_disease', 'system', 'teal', NULL, '2026-02-11 12:58:24');
INSERT INTO `user` VALUES (12, '13900002006', '123456', '吴芳', '0', '1968-09-18', '110101196809187777', 'resident', 1, 'chronic_disease', 'system', 'teal', NULL, '2026-02-11 12:58:24');
INSERT INTO `user` VALUES (13, '13900002007', '123456', '郑老', '1', '1955-12-01', '110101195512018888', 'resident', 1, 'elderly,chronic_disease', 'system', 'teal', NULL, '2026-02-11 12:58:24');
INSERT INTO `user` VALUES (14, '13900002008', '123456', '陈奶奶', '0', '1952-04-25', '110101195204259999', 'resident', 1, 'elderly', 'system', 'teal', NULL, '2026-02-11 12:58:24');
INSERT INTO `user` VALUES (15, '13900002009', '123456', '何俊', '1', '2002-08-30', '110101200208301212', 'resident', 1, NULL, 'system', 'teal', NULL, '2026-02-11 12:58:24');
INSERT INTO `user` VALUES (16, '13900002010', '123456', '唐悦', '0', '1995-02-14', '110101199502141313', 'resident', 1, NULL, 'system', 'teal', NULL, '2026-02-11 12:58:24');
INSERT INTO `user` VALUES (17, '13800000005', '123456', '王师傅', '1', '1975-03-14', '110101197503151234', 'technician', 1, NULL, 'light', 'teal', '/upload/avatar/d07d8370-bb39-475b-881f-a81150857e7a.png', '2026-02-18 09:00:00');
INSERT INTO `user` VALUES (18, '13800000006', '123456', '李师傅', '1', '1980-08-20', '110101198008202345', 'technician', 1, NULL, 'system', 'teal', NULL, '2026-02-18 09:00:00');
INSERT INTO `user` VALUES (19, '13800000007', '123456', '张师傅', '1', '1978-05-10', '110101197805103456', 'technician', 1, NULL, 'system', 'teal', NULL, '2026-02-18 09:00:00');
INSERT INTO `user` VALUES (20, '13800000008', '123456', '刘师傅', '1', '1982-01-25', '110101198201254567', 'technician', 1, NULL, 'system', 'teal', NULL, '2026-02-18 09:00:00');
INSERT INTO `user` VALUES (21, '13800000009', '123456', '陈师傅', '1', '1970-11-05', '110101197011055678', 'technician', 0, NULL, 'system', 'teal', NULL, '2026-02-18 09:00:00');
INSERT INTO `user` VALUES (23, '13800138000', '$2a$10$MPRh2YazcDJIcT8PQzji..6nIG/yUzMzJOayhlEdKRyq0YizsS3rC', 'DuplicateTestUser', NULL, NULL, NULL, 'resident', 1, NULL, 'system', 'teal', NULL, '2026-03-07 14:51:39');
INSERT INTO `user` VALUES (25, '1373959984', '$2a$10$y0n3hYR4nQ52v0CMP75Z2OMiwwgvoB3RvG8NsaY3fdvMajdpJh.p6', 'RegUser1373959984', '1', NULL, NULL, 'resident', 1, NULL, 'system', 'teal', NULL, '2026-03-07 14:56:24');
INSERT INTO `user` VALUES (26, '13899999999', '$2a$10$N.zOVGHt9qXJzW3Q4N5Z5.7u5qXJzW3Q4N5Z5.7u5qXJzW3Q4N5Z5', 'Test User', 'F', NULL, NULL, 'resident', 1, NULL, 'system', 'teal', NULL, '2026-03-07 16:30:57');

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role`  (
  `user_id` bigint(0) NOT NULL COMMENT '用户ID，关联user表',
  `role_id` bigint(0) NOT NULL COMMENT '角色ID，关联role表',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE,
  INDEX `role_id`(`role_id`) USING BTREE,
  CONSTRAINT `user_role_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `user_role_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联表，实现多对多关系' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_role
-- ----------------------------
INSERT INTO `user_role` VALUES (1, 1);
INSERT INTO `user_role` VALUES (23, 1);
INSERT INTO `user_role` VALUES (2, 2);
INSERT INTO `user_role` VALUES (3, 3);
INSERT INTO `user_role` VALUES (5, 3);
INSERT INTO `user_role` VALUES (6, 3);
INSERT INTO `user_role` VALUES (4, 4);
INSERT INTO `user_role` VALUES (7, 4);
INSERT INTO `user_role` VALUES (8, 4);
INSERT INTO `user_role` VALUES (9, 4);
INSERT INTO `user_role` VALUES (10, 4);
INSERT INTO `user_role` VALUES (11, 4);
INSERT INTO `user_role` VALUES (12, 4);
INSERT INTO `user_role` VALUES (13, 4);
INSERT INTO `user_role` VALUES (14, 4);
INSERT INTO `user_role` VALUES (15, 4);
INSERT INTO `user_role` VALUES (16, 4);
INSERT INTO `user_role` VALUES (25, 4);
INSERT INTO `user_role` VALUES (26, 4);
INSERT INTO `user_role` VALUES (17, 6);
INSERT INTO `user_role` VALUES (18, 6);
INSERT INTO `user_role` VALUES (19, 6);
INSERT INTO `user_role` VALUES (20, 6);
INSERT INTO `user_role` VALUES (21, 6);

SET FOREIGN_KEY_CHECKS = 1;
