-- 将 sys_user 的全局唯一索引改为租户内唯一，支持不同租户注册相同用户名
ALTER TABLE sys_user DROP INDEX uk_username;

ALTER TABLE sys_user
    ADD UNIQUE KEY uk_tenant_username (tenant_id, username);
