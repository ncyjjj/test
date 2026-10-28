-- Material Maker 初始化脚本
-- 用于 MySQL 8.4，配合 docker-compose 自动初始化

CREATE DATABASE IF NOT EXISTS `material_maker` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `material_maker`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 用户表（演示用身份）
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` VARCHAR(64) PRIMARY KEY,
  `display_name` VARCHAR(64) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 模板表
DROP TABLE IF EXISTS `templates`;
CREATE TABLE `templates` (
  `id` VARCHAR(64) PRIMARY KEY,
  `title` VARCHAR(128) NOT NULL,
  `category` VARCHAR(32) NOT NULL,
  `cover_url` VARCHAR(255) NOT NULL,
  `description` TEXT NOT NULL,
  `required_image_count` INT NOT NULL DEFAULT 1,
  `accepted_mime_types` JSON NOT NULL,
  `max_file_size_bytes` BIGINT NOT NULL,
  `enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_category_enabled` (`category`, `enabled`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 素材表
DROP TABLE IF EXISTS `materials`;
CREATE TABLE `materials` (
  `id` VARCHAR(64) PRIMARY KEY,
  `task_id` VARCHAR(64) NOT NULL,
  `original_name` VARCHAR(255) NOT NULL,
  `mime_type` VARCHAR(64) NOT NULL,
  `size_bytes` BIGINT NOT NULL,
  `stored_path` VARCHAR(512) NOT NULL,
  `public_url` VARCHAR(512) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 制作任务表
DROP TABLE IF EXISTS `tasks`;
CREATE TABLE `tasks` (
  `id` VARCHAR(64) PRIMARY KEY,
  `user_id` VARCHAR(64) NOT NULL,
  `template_id` VARCHAR(64) NOT NULL,
  `status` ENUM('WAITING','PROCESSING','SUCCESS','FAILED') NOT NULL DEFAULT 'WAITING',
  `stage` ENUM('ASSET_CHECK','GENERATE','FINALIZE') NULL,
  `fail_stage` ENUM('ASSET_CHECK','GENERATE','FINALIZE') NULL,
  `fail_reason` VARCHAR(512) NULL,
  `result_url` VARCHAR(512) NULL,
  `result_meta` JSON NULL,
  `request_snapshot` JSON NULL,
  `started_at` DATETIME NULL,
  `finished_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_user_id_created_at` (`user_id`, `created_at`),
  KEY `idx_status_updated_at` (`status`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 步骤事件审计表（便于观察每一步独立可观察性）
DROP TABLE IF EXISTS `task_events`;
CREATE TABLE `task_events` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `task_id` VARCHAR(64) NOT NULL,
  `stage` ENUM('ASSET_CHECK','GENERATE','FINALIZE') NOT NULL,
  `event` VARCHAR(32) NOT NULL,
  `detail` VARCHAR(512) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_task_id_created_at` (`task_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- 种子数据：模板
INSERT INTO `templates` (`id`, `title`, `category`, `cover_url`, `description`, `required_image_count`, `accepted_mime_types`, `max_file_size_bytes`, `enabled`, `sort_order`) VALUES
('demo-postcard', '旅行明信片', '创意图片', '/static/demo/postcard-cover.svg', '上传图片，模拟生成一张明信片。后台将按素材检查 → 模拟生成 → 结果整理三个步骤顺序处理。', 2, JSON_ARRAY('image/jpeg','image/png'), 5242880, 1, 1),
('demo-poster', '节日海报', '创意图片', '/static/demo/poster-cover.svg', '上传 1 张图片，模拟生成节日主题海报，演示单图素材场景。', 1, JSON_ARRAY('image/jpeg','image/png'), 5242880, 1, 2),
('demo-avatar', '头像拼接', '趣味合成', '/static/demo/avatar-cover.svg', '上传 2 张图片，模拟拼接生成头像，便于演示多任务互不串扰。', 2, JSON_ARRAY('image/jpeg','image/png'), 5242880, 1, 3);
