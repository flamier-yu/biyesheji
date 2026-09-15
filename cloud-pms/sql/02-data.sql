-- ============================================================
-- 云协同 PMS - 初始化数据
-- 默认账号：admin / admin123
--
-- 说明：本脚本包含大量清空表的 DELETE 语句。
--   MySQL Workbench / Navicat 等客户端默认开启「安全更新模式」，
--   会拒绝执行不带 WHERE 的 DELETE，报错：
--     Error Code: 1175 You are using safe update mode ...
--   下面临时关闭该限制（仅对当前会话生效），脚本执行完再恢复。
--   若仍报错，请确认客户端已重新连接数据库。
-- ============================================================
SET NAMES utf8mb4;
SET SQL_SAFE_UPDATES = 0;

-- ------------------------------------------------------------
-- 部门
-- ------------------------------------------------------------
DELETE FROM sys_dept;
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, create_by, create_time) VALUES
(100, 0,   '0',       '云协同科技有限公司', 0, '管理员', '15888888888', 'admin@yunxiao.com', '0', 1, NOW()),
(101, 100, '0,100',   '研发中心',           1, '张伟',   '15888888889', 'dev@yunxiao.com',   '0', 1, NOW()),
(102, 100, '0,100',   '产品部',             2, '陈静',   '15888888890', 'pm@yunxiao.com',    '0', 1, NOW()),
(103, 100, '0,100',   '测试部',             3, '王强',   '15888888891', 'qa@yunxiao.com',    '0', 1, NOW()),
(104, 101, '0,100,101', '后端开发组',       1, '李磊',   '15888888892', 'backend@yunxiao.com', '0', 1, NOW()),
(105, 101, '0,100,101', '前端开发组',       2, '赵敏',   '15888888893', 'frontend@yunxiao.com', '0', 1, NOW());

-- ------------------------------------------------------------
-- 岗位
-- ------------------------------------------------------------
DELETE FROM sys_post;
INSERT INTO sys_post (post_id, post_code, post_name, order_num, status, create_by, create_time, remark) VALUES
(1, 'ceo',  '董事长',     1, '0', 1, NOW(), '管理整个公司'),
(2, 'pm',   '项目经理',   2, '0', 1, NOW(), '负责项目统筹管理'),
(3, 'dev',  '开发工程师', 3, '0', 1, NOW(), '负责功能开发'),
(4, 'qa',   '测试工程师', 4, '0', 1, NOW(), '负责质量保障');

-- ------------------------------------------------------------
-- 角色
-- ------------------------------------------------------------
DELETE FROM sys_role;
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, status, create_by, create_time, remark) VALUES
(1, '超级管理员', 'admin',  1, '1', '0', 1, NOW(), '拥有系统全部权限'),
(2, '项目经理',   'pm',     2, '3', '0', 1, NOW(), '负责项目管理与任务分配'),
(3, '项目成员',   'member', 3, '4', '0', 1, NOW(), '参与项目执行,可查看本人数据');

-- ------------------------------------------------------------
-- 用户（密码统一为 admin123 / 123456 的 BCrypt 密文）
-- ------------------------------------------------------------
DELETE FROM sys_user;
INSERT INTO sys_user (user_id, dept_id, username, nick_name, email, phonenumber, sex, avatar, password, status, create_by, create_time, remark) VALUES
(100, 100, 'admin',    '超级管理员', 'admin@yunxiao.com', '15888888888', '0', '', '$2a$10$2Qc8P8bH3wuwUeg8ZIrNdO3qgoNWGvJ8r2.yEM..mOBLg8.ooPS7G', '0', 1, NOW(), '系统内置管理员账号'),
(101, 101, 'zhangwei', '张伟',       'zhangwei@yunxiao.com', '15888888889', '0', '', '$2a$10$2Qc8P8bH3wuwUeg8ZIrNdO3qgoNWGvJ8r2.yEM..mOBLg8.ooPS7G', '0', 1, NOW(), '研发中心负责人'),
(102, 104, 'lilei',    '李磊',       'lilei@yunxiao.com', '15888888892', '0', '', '$2a$10$2Qc8P8bH3wuwUeg8ZIrNdO3qgoNWGvJ8r2.yEM..mOBLg8.ooPS7G', '0', 1, NOW(), '后端开发'),
(103, 105, 'zhaomin',  '赵敏',       'zhaomin@yunxiao.com', '15888888893', '1', '', '$2a$10$2Qc8P8bH3wuwUeg8ZIrNdO3qgoNWGvJ8r2.yEM..mOBLg8.ooPS7G', '0', 1, NOW(), '前端开发'),
(104, 103, 'wangqiang','王强',       'wangqiang@yunxiao.com', '15888888891', '0', '', '$2a$10$2Qc8P8bH3wuwUeg8ZIrNdO3qgoNWGvJ8r2.yEM..mOBLg8.ooPS7G', '0', 1, NOW(), '测试工程师');

-- 用户角色
DELETE FROM sys_user_role;
INSERT INTO sys_user_role (user_id, role_id) VALUES (100, 1), (101, 2), (102, 3), (103, 3), (104, 3);

-- 用户岗位
DELETE FROM sys_user_post;
INSERT INTO sys_user_post (user_id, post_id) VALUES (100, 1), (101, 2), (102, 3), (103, 3), (104, 4);

-- ------------------------------------------------------------
-- 菜单
-- ------------------------------------------------------------
DELETE FROM sys_menu;
-- 一级目录
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(1,    '工作台',   0, 1, '/dashboard', 'dashboard/index', 'C', '0', '0', '', 'Odometer', 1, NOW()),
(1000, '系统管理', 0, 2, '/system',    NULL,              'M', '0', '0', '', 'Setting',  1, NOW()),
(2000, '项目管理', 0, 3, '/project',   NULL,              'M', '0', '0', '', 'Folder',   1, NOW()),
(3000, '统计报表', 0, 4, '/report',    NULL,              'M', '0', '0', '', 'DataLine', 1, NOW());

-- 系统管理 -> 子菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(1100, '用户管理', 1000, 1, '/system/user',      'system/user/index',      'C', '0', '0', 'system:user:list',      'User',          1, NOW()),
(1200, '角色管理', 1000, 2, '/system/role',      'system/role/index',      'C', '0', '0', 'system:role:list',      'Avatar',        1, NOW()),
(1300, '菜单管理', 1000, 3, '/system/menu',      'system/menu/index',      'C', '0', '0', 'system:menu:list',      'Menu',          1, NOW()),
(1400, '部门管理', 1000, 4, '/system/dept',      'system/dept/index',      'C', '0', '0', 'system:dept:list',      'OfficeBuilding',1, NOW()),
(1500, '岗位管理', 1000, 5, '/system/post',      'system/post/index',      'C', '0', '0', 'system:post:list',      'Postcard',      1, NOW()),
(1600, '字典管理', 1000, 6, '/system/dict',      'system/dict/index',      'C', '0', '0', 'system:dict:list',      'Collection',    1, NOW()),
(1700, '参数设置', 1000, 7, '/system/config',    'system/config/index',    'C', '0', '0', 'system:config:list',    'Tools',         1, NOW()),
(1800, '操作日志', 1000, 8, '/system/operlog',   'monitor/operlog/index',  'C', '0', '0', 'system:operlog:list',   'Document',      1, NOW()),
(1900, '登录日志', 1000, 9, '/system/loginlog',  'monitor/loginlog/index', 'C', '0', '0', 'system:loginlog:list',  'Tickets',       1, NOW());

-- 用户管理按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(1101, '用户查询', 1100, 1, '', NULL, 'F', '0', '0', 'system:user:query',     '#', 1, NOW()),
(1102, '用户新增', 1100, 2, '', NULL, 'F', '0', '0', 'system:user:add',       '#', 1, NOW()),
(1103, '用户修改', 1100, 3, '', NULL, 'F', '0', '0', 'system:user:edit',      '#', 1, NOW()),
(1104, '用户删除', 1100, 4, '', NULL, 'F', '0', '0', 'system:user:remove',    '#', 1, NOW()),
(1105, '重置密码', 1100, 5, '', NULL, 'F', '0', '0', 'system:user:resetPwd',  '#', 1, NOW()),
(1106, '用户导出', 1100, 6, '', NULL, 'F', '0', '0', 'system:user:export',    '#', 1, NOW()),
(1107, '用户导入', 1100, 7, '', NULL, 'F', '0', '0', 'system:user:import',    '#', 1, NOW());

-- 角色管理按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(1201, '角色查询', 1200, 1, '', NULL, 'F', '0', '0', 'system:role:query',  '#', 1, NOW()),
(1202, '角色新增', 1200, 2, '', NULL, 'F', '0', '0', 'system:role:add',    '#', 1, NOW()),
(1203, '角色修改', 1200, 3, '', NULL, 'F', '0', '0', 'system:role:edit',   '#', 1, NOW()),
(1204, '角色删除', 1200, 4, '', NULL, 'F', '0', '0', 'system:role:remove', '#', 1, NOW()),
(1205, '角色导出', 1200, 5, '', NULL, 'F', '0', '0', 'system:role:export', '#', 1, NOW());

-- 菜单管理按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(1301, '菜单查询', 1300, 1, '', NULL, 'F', '0', '0', 'system:menu:query',  '#', 1, NOW()),
(1302, '菜单新增', 1300, 2, '', NULL, 'F', '0', '0', 'system:menu:add',    '#', 1, NOW()),
(1303, '菜单修改', 1300, 3, '', NULL, 'F', '0', '0', 'system:menu:edit',   '#', 1, NOW()),
(1304, '菜单删除', 1300, 4, '', NULL, 'F', '0', '0', 'system:menu:remove', '#', 1, NOW());

-- 部门管理按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(1401, '部门查询', 1400, 1, '', NULL, 'F', '0', '0', 'system:dept:query',  '#', 1, NOW()),
(1402, '部门新增', 1400, 2, '', NULL, 'F', '0', '0', 'system:dept:add',    '#', 1, NOW()),
(1403, '部门修改', 1400, 3, '', NULL, 'F', '0', '0', 'system:dept:edit',   '#', 1, NOW()),
(1404, '部门删除', 1400, 4, '', NULL, 'F', '0', '0', 'system:dept:remove', '#', 1, NOW());

-- 岗位管理按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(1501, '岗位查询', 1500, 1, '', NULL, 'F', '0', '0', 'system:post:query',  '#', 1, NOW()),
(1502, '岗位新增', 1500, 2, '', NULL, 'F', '0', '0', 'system:post:add',    '#', 1, NOW()),
(1503, '岗位修改', 1500, 3, '', NULL, 'F', '0', '0', 'system:post:edit',   '#', 1, NOW()),
(1504, '岗位删除', 1500, 4, '', NULL, 'F', '0', '0', 'system:post:remove', '#', 1, NOW()),
(1505, '岗位导出', 1500, 5, '', NULL, 'F', '0', '0', 'system:post:export', '#', 1, NOW());

-- 字典管理按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(1601, '字典查询', 1600, 1, '', NULL, 'F', '0', '0', 'system:dict:query',  '#', 1, NOW()),
(1602, '字典新增', 1600, 2, '', NULL, 'F', '0', '0', 'system:dict:add',    '#', 1, NOW()),
(1603, '字典修改', 1600, 3, '', NULL, 'F', '0', '0', 'system:dict:edit',   '#', 1, NOW()),
(1604, '字典删除', 1600, 4, '', NULL, 'F', '0', '0', 'system:dict:remove', '#', 1, NOW()),
(1605, '字典导出', 1600, 5, '', NULL, 'F', '0', '0', 'system:dict:export', '#', 1, NOW());

-- 参数设置按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(1701, '参数查询', 1700, 1, '', NULL, 'F', '0', '0', 'system:config:query',  '#', 1, NOW()),
(1702, '参数新增', 1700, 2, '', NULL, 'F', '0', '0', 'system:config:add',    '#', 1, NOW()),
(1703, '参数修改', 1700, 3, '', NULL, 'F', '0', '0', 'system:config:edit',   '#', 1, NOW()),
(1704, '参数删除', 1700, 4, '', NULL, 'F', '0', '0', 'system:config:remove', '#', 1, NOW()),
(1705, '参数导出', 1700, 5, '', NULL, 'F', '0', '0', 'system:config:export', '#', 1, NOW());

-- 操作日志按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(1801, '日志查询', 1800, 1, '', NULL, 'F', '0', '0', 'system:operlog:query',  '#', 1, NOW()),
(1802, '日志删除', 1800, 2, '', NULL, 'F', '0', '0', 'system:operlog:remove', '#', 1, NOW()),
(1803, '日志导出', 1800, 3, '', NULL, 'F', '0', '0', 'system:operlog:export', '#', 1, NOW());

-- 登录日志按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(1901, '登录查询', 1900, 1, '', NULL, 'F', '0', '0', 'system:loginlog:query',  '#', 1, NOW()),
(1902, '登录删除', 1900, 2, '', NULL, 'F', '0', '0', 'system:loginlog:remove', '#', 1, NOW()),
(1903, '登录导出', 1900, 3, '', NULL, 'F', '0', '0', 'system:loginlog:export', '#', 1, NOW());

-- ------------------------------------------------------------
-- 角色菜单关联
-- ------------------------------------------------------------
DELETE FROM sys_role_menu;
-- 超级管理员：全部菜单
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu;
-- 项目经理：工作台 + 项目/报表目录 + 系统管理只读部分
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1), (2, 1000), (2, 2000), (2, 3000),
(2, 1100), (2, 1101), (2, 1200), (2, 1201), (2, 1400), (2, 1401), (2, 1500), (2, 1501);
-- 项目成员：工作台 + 项目目录
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1), (3, 2000);

-- ------------------------------------------------------------
-- 字典类型
-- ------------------------------------------------------------
DELETE FROM sys_dict_type;
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(100, '用户性别',   'sys_user_sex',       '0', 1, NOW(), '用户性别列表'),
(101, '系统开关',   'sys_normal_disable', '0', 1, NOW(), '系统开关列表'),
(102, '系统是否',   'sys_yes_no',         '0', 1, NOW(), '系统是否列表'),
(103, '项目状态',   'pm_project_status',  '0', 1, NOW(), '项目状态列表'),
(104, '任务状态',   'pm_task_status',     '0', 1, NOW(), '任务状态列表'),
(105, '优先级',     'pm_priority',        '0', 1, NOW(), '任务/项目优先级'),
(106, '工时状态',   'pm_worklog_status',  '0', 1, NOW(), '工时审批状态'),
(107, '消息类型',   'pm_msg_type',        '0', 1, NOW(), '站内消息类型');

-- ------------------------------------------------------------
-- 字典数据
-- ------------------------------------------------------------
DELETE FROM sys_dict_data;
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time) VALUES
(1, '男',   '0', 'sys_user_sex',       'primary', 'Y', '0', 1, NOW()),
(2, '女',   '1', 'sys_user_sex',       'danger',  'N', '0', 1, NOW()),
(3, '未知', '2', 'sys_user_sex',       'info',    'N', '0', 1, NOW()),
(1, '正常', '0', 'sys_normal_disable', 'success', 'Y', '0', 1, NOW()),
(2, '停用', '1', 'sys_normal_disable', 'danger',  'N', '0', 1, NOW()),
(1, '是',   'Y', 'sys_yes_no',         'primary', 'Y', '0', 1, NOW()),
(2, '否',   'N', 'sys_yes_no',         'danger',  'N', '0', 1, NOW()),
(1, '待审批', '0', 'pm_project_status', 'warning', 'Y', '0', 1, NOW()),
(2, '进行中', '1', 'pm_project_status', 'primary', 'N', '0', 1, NOW()),
(3, '已完成', '2', 'pm_project_status', 'success', 'N', '0', 1, NOW()),
(4, '已暂停', '3', 'pm_project_status', 'info',    'N', '0', 1, NOW()),
(5, '已终止', '4', 'pm_project_status', 'danger',  'N', '0', 1, NOW()),
(1, '待开始', '0', 'pm_task_status', 'info',    'Y', '0', 1, NOW()),
(2, '进行中', '1', 'pm_task_status', 'primary', 'N', '0', 1, NOW()),
(3, '已完成', '2', 'pm_task_status', 'success', 'N', '0', 1, NOW()),
(4, '已挂起', '3', 'pm_task_status', 'warning', 'N', '0', 1, NOW()),
(1, '高', '1', 'pm_priority', 'danger',  'N', '0', 1, NOW()),
(2, '中', '2', 'pm_priority', 'warning', 'Y', '0', 1, NOW()),
(3, '低', '3', 'pm_priority', 'info',    'N', '0', 1, NOW()),
(1, '待审批', '0', 'pm_worklog_status', 'warning', 'Y', '0', 1, NOW()),
(2, '已通过', '1', 'pm_worklog_status', 'success', 'N', '0', 1, NOW()),
(3, '已驳回', '2', 'pm_worklog_status', 'danger',  'N', '0', 1, NOW()),
(1, '系统消息', '0', 'pm_msg_type', 'info',    'Y', '0', 1, NOW()),
(2, '任务通知', '1', 'pm_msg_type', 'primary', 'N', '0', 1, NOW()),
(3, '项目通知', '2', 'pm_msg_type', 'success', 'N', '0', 1, NOW()),
(4, '工时通知', '3', 'pm_msg_type', 'warning', 'N', '0', 1, NOW()),
(5, '逾期预警', '4', 'pm_msg_type', 'danger',  'N', '0', 1, NOW());

-- ------------------------------------------------------------
-- 参数配置
-- ------------------------------------------------------------
DELETE FROM sys_config;
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
(100, '用户初始密码',     'sys.user.initPassword',     '123456', 'Y', 1, NOW(), '新增用户时的默认密码'),
(101, '验证码开关',       'sys.login.captchaEnabled',  'true',   'Y', 1, NOW(), '是否开启登录验证码'),
(102, '项目逾期预警天数', 'pm.task.overdueDays',       '1',      'Y', 1, NOW(), '任务到期前N天开始预警'),
(103, '单次上传大小上限', 'sys.file.maxUploadSize',    '50',     'Y', 1, NOW(), '单位MB');

-- ------------------------------------------------------------
-- 演示业务数据
-- ------------------------------------------------------------
DELETE FROM pm_project;
INSERT INTO pm_project (project_id, project_code, project_name, manager_id, dept_id, description, priority, status, progress, budget, start_date, end_date, create_by, create_time) VALUES
(100, 'PJ2026001', '云协同项目管理平台研发', 101, 101, '面向中小企业的项目全生命周期管理系统,涵盖立项、任务、工时、报表等模块。', '1', '1', 45, 380000.00, '2026-06-01', '2026-12-31', 100, NOW()),
(101, 'PJ2026002', '企业门户官网改版',       101, 105, '对现有官网进行视觉升级与响应式改造,提升移动端体验。', '2', '1', 70, 120000.00, '2026-07-01', '2026-10-31', 100, NOW()),
(102, 'PJ2026003', '内部考勤系统集成',       101, 104, '将考勤机数据接入现有管理平台,实现统一人员管理。', '2', '0', 0, 80000.00, '2026-09-20', '2026-11-30', 100, NOW());

DELETE FROM pm_project_member;
INSERT INTO pm_project_member (project_id, user_id, role_in_pj, join_time, create_time) VALUES
(100, 101, '项目经理', NOW(), NOW()),
(100, 102, '后端开发', NOW(), NOW()),
(100, 103, '前端开发', NOW(), NOW()),
(100, 104, '测试',     NOW(), NOW()),
(101, 101, '项目经理', NOW(), NOW()),
(101, 103, '前端开发', NOW(), NOW());

DELETE FROM pm_milestone;
INSERT INTO pm_milestone (milestone_id, project_id, milestone_name, plan_date, status, order_num, create_by, create_time) VALUES
(100, 100, '需求分析与设计评审', '2026-06-30', '2', 1, 100, NOW()),
(101, 100, '核心模块开发完成',   '2026-09-30', '1', 2, 100, NOW()),
(102, 100, '系统测试与上线',     '2026-12-20', '0', 3, 100, NOW());

DELETE FROM pm_task;
INSERT INTO pm_task (task_id, project_id, parent_id, milestone_id, task_name, description, assignee_id, priority, status, progress, plan_start, plan_end, estimate_hours, actual_hours, order_num, create_by, create_time) VALUES
(100, 100, 0, 100, '需求调研与文档编写', '完成用户访谈并输出需求规格说明书', 101, '1', '2', 100, '2026-06-01', '2026-06-15', 40.00, 42.00, 1, 100, NOW()),
(101, 100, 0, 100, '数据库设计',         '完成 E-R 图与表结构设计',           102, '1', '2', 100, '2026-06-16', '2026-06-25', 24.00, 20.00, 2, 100, NOW()),
(102, 100, 0, 101, '认证鉴权模块开发',   '实现登录、JWT 认证与权限控制',      102, '1', '1', 60,  '2026-07-01', '2026-07-20', 60.00, 36.00, 3, 100, NOW()),
(103, 100, 102, NULL, '登录接口开发',     '账号密码登录 + 验证码 + Token 签发', 102, '1', '2', 100, '2026-07-01', '2026-07-08', 16.00, 14.00, 1, 100, NOW()),
(104, 100, 102, NULL, '权限拦截器开发',   '实现接口级与按钮级权限校验',        102, '2', '1', 40,  '2026-07-09', '2026-07-20', 24.00, 10.00, 2, 100, NOW()),
(105, 100, 0, 101, '前端框架搭建',       '搭建 Vue3 工程与主框架布局',        103, '2', '1', 80,  '2026-07-05', '2026-07-25', 48.00, 38.00, 4, 100, NOW()),
(106, 100, 0, 102, '系统测试',           '编写测试用例并执行回归测试',        104, '2', '0', 0,   '2026-12-01', '2026-12-15', 40.00, 0.00,  5, 100, NOW()),
(107, 100, 0, 101, '报表模块开发',       '实现工时统计与燃尽图',              102, '2', '0', 0,   '2026-08-01', '2026-09-15', 56.00, 0.00,  6, 100, NOW());

DELETE FROM pm_task_comment;
INSERT INTO pm_task_comment (task_id, user_id, content, create_time) VALUES
(102, 101, '认证模块优先级最高,请先保证登录链路通畅。', NOW()),
(102, 102, '已完成登录接口,正在做权限拦截器。', NOW()),
(105, 101, '布局参考 Element Plus 官方示例即可。', NOW());

DELETE FROM pm_work_log;
INSERT INTO pm_work_log (project_id, task_id, user_id, work_date, hours, content, status, audit_by, audit_time, create_time) VALUES
(100, 102, 102, '2026-09-08', 8.00, '登录接口联调与异常处理',   '1', 101, NOW(), NOW()),
(100, 102, 102, '2026-09-09', 7.50, 'JWT 工具类封装',           '1', 101, NOW(), NOW()),
(100, 104, 102, '2026-09-10', 6.00, '权限注解切面开发',         '0', NULL, NULL, NOW()),
(100, 105, 103, '2026-09-08', 8.00, '主框架布局与侧边栏菜单',   '1', 101, NOW(), NOW()),
(100, 105, 103, '2026-09-09', 7.00, '动态路由接入',             '1', 101, NOW(), NOW()),
(101, NULL, 103, '2026-09-10', 5.00, '官网首页响应式适配',       '0', NULL, NULL, NOW());

DELETE FROM pm_project_burndown;
INSERT INTO pm_project_burndown (project_id, snapshot_date, total_hours, remaining_hours, completed_hours, create_time) VALUES
(100, '2026-09-08', 292.00, 180.00, 112.00, NOW()),
(100, '2026-09-09', 292.00, 172.00, 120.00, NOW()),
(100, '2026-09-10', 292.00, 165.00, 127.00, NOW()),
(100, '2026-09-11', 292.00, 158.00, 134.00, NOW()),
(100, '2026-09-12', 292.00, 150.00, 142.00, NOW()),
(100, '2026-09-13', 292.00, 143.00, 149.00, NOW()),
(100, '2026-09-14', 292.00, 138.00, 154.00, NOW());

DELETE FROM pm_message;
INSERT INTO pm_message (receiver_id, sender_id, title, content, msg_type, biz_type, biz_id, is_read, create_time) VALUES
(102, 101, '任务指派通知', '您被指派了任务「认证鉴权模块开发」,请及时处理。', '1', 'task', 102, '0', NOW()),
(103, 101, '任务指派通知', '您被指派了任务「前端框架搭建」,请及时处理。',     '1', 'task', 105, '0', NOW()),
(102, 101, '工时审批通过', '您 2026-09-08 填报的工时已审批通过。',           '3', 'worklog', 1, '1', NOW()),
(104, 101, '任务指派通知', '您被指派了任务「系统测试」,请及时处理。',       '1', 'task', 106, '0', NOW());

-- 恢复客户端的默认安全设置
SET SQL_SAFE_UPDATES = 1;

-- ============================================================
-- 导入完成。默认账号：
--   admin / admin123      超级管理员
--   zhangwei / admin123   项目经理
--   lilei / admin123      项目成员
-- ============================================================
