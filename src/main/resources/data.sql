-- 插入初始角色
INSERT INTO roles (name, description) VALUES 
('ADMIN', '系统管理员，拥有所有权限'),
('USER', '普通用户，拥有基本权限'),
('MANAGER', '经理，拥有管理权限');

-- 插入初始用户
INSERT INTO users (name, age, phone, email, active) VALUES 
('admin', 30, '13800000000', 'admin@example.com', TRUE),
('张三', 20, '13800138000', 'zhangsan@example.com', TRUE),
('李四', 22, '13800138001', 'lisi@example.com', TRUE),
('王五', 25, '13800138002', 'wangwu@example.com', TRUE);

-- 分配用户角色
-- admin 用户拥有 ADMIN 角色
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);

-- 张三、李四、王五拥有 USER 角色
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2);
INSERT INTO user_roles (user_id, role_id) VALUES (3, 2);
INSERT INTO user_roles (user_id, role_id) VALUES (4, 2);
