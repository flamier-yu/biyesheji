-- ============================================================
-- 业务模块菜单与权限
-- 依赖：01-schema.sql、02-data.sql 已执行
-- 可重复执行（先删后插）
--
-- 说明：清理语句的 WHERE 用到 menu_id，在 sys_role_menu 中它不是独立索引列，
--   会触发客户端「安全更新模式」报错 Error Code 1175，
--   故临时关闭该限制（仅当前会话生效）。
-- ============================================================
SET NAMES utf8mb4;
SET SQL_SAFE_UPDATES = 0;

-- ------------------------------------------------------------
-- 清理旧的业务菜单（2000 段与 4000 段）
-- ------------------------------------------------------------
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2000 AND 4999;
DELETE FROM sys_menu WHERE menu_id BETWEEN 2000 AND 4999;

-- ------------------------------------------------------------
-- 一级：项目管理
-- ------------------------------------------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(2000, '项目管理', 0, 3, '/project', NULL, 'M', '0', '0', '', 'Folder', 1, NOW());

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(2100, '项目列表', 2000, 1, '/project/list',    'project/list/index',    'C', '0', '0', 'project:project:list', 'Files',    1, NOW()),
(2200, '任务管理', 2000, 2, '/project/task',    'project/task/index',    'C', '0', '0', 'project:task:list',    'List',     1, NOW()),
(2300, '工时管理', 2000, 3, '/project/worklog', 'project/worklog/index', 'C', '0', '0', 'project:worklog:list', 'Clock',    1, NOW()),
(2400, '里程碑',   2000, 4, '/project/milestone','project/milestone/index','C','0','0', 'project:milestone:list','Flag',   1, NOW());

-- 项目按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(2101, '项目查询', 2100, 1, '', NULL, 'F', '0', '0', 'project:project:query',  '#', 1, NOW()),
(2102, '项目新增', 2100, 2, '', NULL, 'F', '0', '0', 'project:project:add',    '#', 1, NOW()),
(2103, '项目修改', 2100, 3, '', NULL, 'F', '0', '0', 'project:project:edit',   '#', 1, NOW()),
(2104, '项目删除', 2100, 4, '', NULL, 'F', '0', '0', 'project:project:remove', '#', 1, NOW());

-- 任务按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(2201, '任务查询', 2200, 1, '', NULL, 'F', '0', '0', 'project:task:query',  '#', 1, NOW()),
(2202, '任务新增', 2200, 2, '', NULL, 'F', '0', '0', 'project:task:add',    '#', 1, NOW()),
(2203, '任务修改', 2200, 3, '', NULL, 'F', '0', '0', 'project:task:edit',   '#', 1, NOW()),
(2204, '任务删除', 2200, 4, '', NULL, 'F', '0', '0', 'project:task:remove', '#', 1, NOW());

-- 工时按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(2301, '工时查询', 2300, 1, '', NULL, 'F', '0', '0', 'project:worklog:query',  '#', 1, NOW()),
(2302, '工时填报', 2300, 2, '', NULL, 'F', '0', '0', 'project:worklog:add',    '#', 1, NOW()),
(2303, '工时修改', 2300, 3, '', NULL, 'F', '0', '0', 'project:worklog:edit',   '#', 1, NOW()),
(2304, '工时删除', 2300, 4, '', NULL, 'F', '0', '0', 'project:worklog:remove', '#', 1, NOW()),
(2305, '工时审批', 2300, 5, '', NULL, 'F', '0', '0', 'project:worklog:audit',  '#', 1, NOW());

-- 里程碑按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(2401, '里程碑新增', 2400, 1, '', NULL, 'F', '0', '0', 'project:milestone:add',    '#', 1, NOW()),
(2402, '里程碑修改', 2400, 2, '', NULL, 'F', '0', '0', 'project:milestone:edit',   '#', 1, NOW()),
(2403, '里程碑删除', 2400, 3, '', NULL, 'F', '0', '0', 'project:milestone:remove', '#', 1, NOW());

-- ------------------------------------------------------------
-- 一级：统计报表
-- ------------------------------------------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(3000, '统计报表', 0, 4, '/report', NULL, 'M', '0', '0', '', 'DataLine', 1, NOW());

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(3100, '数据看板', 3000, 1, '/report/dashboard', 'report/dashboard/index', 'C', '0', '0', 'report:dashboard:list', 'PieChart',  1, NOW()),
(3200, '工时统计', 3000, 2, '/report/worklog',   'report/worklog/index',   'C', '0', '0', 'report:worklog:list',   'TrendCharts', 1, NOW()),
(3300, '成员绩效', 3000, 3, '/report/performance','report/performance/index','C','0','0', 'report:performance:list','Medal',   1, NOW());

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(3201, '工时导出', 3200, 1, '', NULL, 'F', '0', '0', 'report:worklog:export', '#', 1, NOW());

-- ------------------------------------------------------------
-- 一级：消息中心（单页菜单，前端会自动包一层布局）
-- ------------------------------------------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(4000, '消息中心', 0, 5, '/message', 'message/index', 'C', '0', '0', 'message:message:list', 'Bell', 1, NOW());

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(4001, '消息删除', 4000, 1, '', NULL, 'F', '0', '0', 'message:message:remove', '#', 1, NOW());

-- ------------------------------------------------------------
-- 角色授权
-- ------------------------------------------------------------

-- 超级管理员：拥有全部菜单
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu
WHERE menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);

-- 项目经理：工作台 + 项目管理全部 + 报表全部 + 消息中心 + 系统管理只读
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1), (2, 1000), (2, 1100), (2, 1101), (2, 1200), (2, 1201),
(2, 1400), (2, 1401), (2, 1500), (2, 1501),
(2, 2000), (2, 2100), (2, 2101), (2, 2102), (2, 2103), (2, 2104),
(2, 2200), (2, 2201), (2, 2202), (2, 2203), (2, 2204),
(2, 2300), (2, 2301), (2, 2302), (2, 2303), (2, 2304), (2, 2305),
(2, 2400), (2, 2401), (2, 2402), (2, 2403),
(2, 3000), (2, 3100), (2, 3200), (2, 3201), (2, 3300),
(2, 4000), (2, 4001)
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 项目成员：工作台 + 项目/任务/工时（不含审批）+ 消息中心
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1),
(3, 2000), (3, 2100), (3, 2101),
(3, 2200), (3, 2201), (3, 2202), (3, 2203),
(3, 2300), (3, 2301), (3, 2302), (3, 2303),
(3, 2400),
(3, 4000), (3, 4001)
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 恢复客户端的默认安全设置
SET SQL_SAFE_UPDATES = 1;

-- ============================================================
-- 导入完成。执行后可验证：
--   SELECT COUNT(*) FROM sys_menu;                       -- 期望 80
--   SELECT role_id, COUNT(*) FROM sys_role_menu GROUP BY role_id;
--   -- 期望：1 -> 80   2 -> 38   3 -> 15
-- ============================================================
