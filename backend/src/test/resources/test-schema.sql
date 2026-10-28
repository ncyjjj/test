CREATE TABLE IF NOT EXISTS users (
  id VARCHAR(64) PRIMARY KEY,
  display_name VARCHAR(64) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS templates (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(128) NOT NULL,
  category VARCHAR(32) NOT NULL,
  cover_url VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  required_image_count INT NOT NULL DEFAULT 1,
  accepted_mime_types VARCHAR(255) NOT NULL,
  max_file_size_bytes BIGINT NOT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  sort_order INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS materials (
  id VARCHAR(64) PRIMARY KEY,
  task_id VARCHAR(64) NOT NULL,
  original_name VARCHAR(255) NOT NULL,
  mime_type VARCHAR(64) NOT NULL,
  size_bytes BIGINT NOT NULL,
  stored_path VARCHAR(512) NOT NULL,
  public_url VARCHAR(512) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tasks (
  id VARCHAR(64) PRIMARY KEY,
  user_id VARCHAR(64) NOT NULL,
  template_id VARCHAR(64) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'WAITING',
  stage VARCHAR(16),
  fail_stage VARCHAR(16),
  fail_reason VARCHAR(512),
  result_url VARCHAR(512),
  result_meta VARCHAR(1024),
  request_snapshot VARCHAR(1024),
  started_at TIMESTAMP,
  finished_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS task_events (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  task_id VARCHAR(64) NOT NULL,
  stage VARCHAR(16) NOT NULL,
  event VARCHAR(32) NOT NULL,
  detail VARCHAR(512),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO templates (id, title, category, cover_url, description, required_image_count, accepted_mime_types, max_file_size_bytes, enabled, sort_order)
VALUES ('demo-postcard', '旅行明信片', '创意图片', '/static/demo/postcard-cover.svg', '测试用模板', 2, '["image/jpeg","image/png"]', 5242880, 1, 1);
