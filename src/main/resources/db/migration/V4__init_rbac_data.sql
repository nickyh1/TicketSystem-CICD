-- 角色
INSERT INTO sys_role (id, role_name, role_key, status) VALUES
(1, '管理员', 'admin', 1),
(2, '普通用户', 'user', 1),
(3, '处理人', 'handler', 1);

-- 权限
INSERT INTO sys_permission (id, perm_name, perm_key) VALUES
(1, '创建工单', 'ticket:create'),
(2, '查看工单', 'ticket:view'),
(3, '更新工单', 'ticket:update'),
(4, '接单', 'ticket:assign'),
(5, '处理工单', 'ticket:process'),
(6, '查看所有工单', 'ticket:view:all'),
(7, '用户管理', 'user:manage');

-- 管理员拥有所有权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7);

-- 普通用户：创建、查看、更新自己的工单
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(2, 1), (2, 2), (2, 3);

-- 处理人：查看、接单、处理
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(3, 2), (3, 4), (3, 5);