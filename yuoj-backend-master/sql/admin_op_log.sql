-- Admin operation log
CREATE TABLE IF NOT EXISTS admin_op_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT NOT NULL COMMENT '操作人ID',
  op_type VARCHAR(64) NOT NULL COMMENT '操作类型(user_ban/contest_create/etc)',
  op_desc VARCHAR(256) NULL COMMENT '操作描述',
  op_data TEXT NULL COMMENT '操作数据(JSON)',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_user_id (user_id),
  KEY idx_op_type (op_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作日志';
