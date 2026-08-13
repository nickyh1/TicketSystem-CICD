CREATE TABLE ticket_stats (
    status VARCHAR(20) NOT NULL COMMENT '工单状态',
    count INT NOT NULL DEFAULT 0 COMMENT '数量',
    PRIMARY KEY (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单统计表';

-- 初始化数据
INSERT INTO ticket_stats (status, count) VALUES
('PENDING', 0),
('PROCESSING', 0),
('RESOLVED', 0),
('CLOSED', 0);