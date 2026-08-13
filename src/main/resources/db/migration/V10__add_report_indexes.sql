CREATE INDEX idx_tenant_status ON ticket (tenant_id, status, deleted);
CREATE INDEX idx_tenant_priority ON ticket (tenant_id, priority, deleted);