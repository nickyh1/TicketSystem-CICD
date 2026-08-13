-- 清空旧数据
DELETE FROM ticket_stats;

-- 加租户字段
ALTER TABLE ticket_stats ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID' FIRST;

-- 改主键为联合主键
ALTER TABLE ticket_stats DROP PRIMARY KEY;
ALTER TABLE ticket_stats ADD PRIMARY KEY (tenant_id, status);

-- 重新初始化（默认租户）
INSERT INTO ticket_stats (tenant_id, status, count) VALUES
(1, 'PENDING', 0),
(1, 'PROCESSING', 0),
(1, 'RESOLVED', 0),
(1, 'CLOSED', 0);