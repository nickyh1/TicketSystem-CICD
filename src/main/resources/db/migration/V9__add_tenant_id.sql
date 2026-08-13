-- 添加租户字段
ALTER TABLE sys_user ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID' AFTER id;
ALTER TABLE ticket ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID' AFTER id;

-- 创建租户表
CREATE TABLE sys_tenant (
    id BIGINT NOT NULL COMMENT '租户ID',
    tenant_name VARCHAR(100) NOT NULL COMMENT '租户名称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

-- 初始化默认租户
INSERT INTO sys_tenant (id, tenant_name, status) VALUES (1, '默认租户', 1);

-- 给 ticket 表加租户索引
CREATE INDEX idx_tenant_id ON ticket (tenant_id);
CREATE INDEX idx_tenant_id_user ON sys_user (tenant_id);