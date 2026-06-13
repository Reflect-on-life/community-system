CREATE TABLE IF NOT EXISTS `alarm_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `alarm_type` VARCHAR(50) NOT NULL,
  `confidence` DOUBLE NOT NULL DEFAULT 0.0,
  `image_path` VARCHAR(500) DEFAULT NULL,
  `location` VARCHAR(200) DEFAULT NULL,
  `alarm_time` DATETIME NOT NULL,
  `is_read` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_alarm_time` (`alarm_time`),
  INDEX `idx_alarm_type` (`alarm_type`),
  INDEX `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
