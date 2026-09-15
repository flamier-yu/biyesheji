-- ============================================================
-- 云协同 PMS 项目管理系统 - 表结构
-- 数据库：biyesheji    字符集：utf8mb4
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- 1. 部门表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
  dept_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  parent_id   BIGINT       NOT NULL DEFAULT 0      COMMENT '父部门ID',
  ancestors   VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '祖级列表',
  dept_name   VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '部门名称',
  order_num   INT          NOT NULL DEFAULT 0      COMMENT '显示顺序',
  leader      VARCHAR(30)           DEFAULT NULL   COMMENT '负责人',
  phone       VARCHAR(20)           DEFAULT NULL   COMMENT '联系电话',
  email       VARCHAR(50)           DEFAULT NULL   COMMENT '邮箱',
  status      CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '状态(0正常 1停用)',
  del_flag    CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志(0存在 2删除)',
  create_by   BIGINT                DEFAULT NULL   COMMENT '创建者',
  create_time DATETIME              DEFAULT NULL   COMMENT '创建时间',
  update_by   BIGINT                DEFAULT NULL   COMMENT '更新者',
  update_time DATETIME              DEFAULT NULL   COMMENT '更新时间',
  PRIMARY KEY (dept_id),
  KEY idx_dept_parent (parent_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='部门表';

-- ------------------------------------------------------------
-- 2. 用户表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  user_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  dept_id     BIGINT                DEFAULT NULL   COMMENT '部门ID',
  username    VARCHAR(30)  NOT NULL                COMMENT '登录账号',
  nick_name   VARCHAR(30)  NOT NULL                COMMENT '用户昵称',
  email       VARCHAR(50)           DEFAULT ''     COMMENT '邮箱',
  phonenumber VARCHAR(11)           DEFAULT ''     COMMENT '手机号',
  sex         CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '性别(0男 1女 2未知)',
  avatar      VARCHAR(255)          DEFAULT ''     COMMENT '头像地址',
  password    VARCHAR(100) NOT NULL                COMMENT '密码(BCrypt)',
  status      CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '状态(0正常 1停用)',
  del_flag    CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志(0存在 2删除)',
  login_ip    VARCHAR(128)          DEFAULT ''     COMMENT '最后登录IP',
  login_date  DATETIME              DEFAULT NULL   COMMENT '最后登录时间',
  create_by   BIGINT                DEFAULT NULL   COMMENT '创建者',
  create_time DATETIME              DEFAULT NULL   COMMENT '创建时间',
  update_by   BIGINT                DEFAULT NULL   COMMENT '更新者',
  update_time DATETIME              DEFAULT NULL   COMMENT '更新时间',
  remark      VARCHAR(500)          DEFAULT NULL   COMMENT '备注',
  PRIMARY KEY (user_id),
  UNIQUE KEY uk_user_username (username),
  KEY idx_user_dept (dept_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ------------------------------------------------------------
-- 3. 岗位表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_post;
CREATE TABLE sys_post (
  post_id     BIGINT      NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  post_code   VARCHAR(64) NOT NULL                COMMENT '岗位编码',
  post_name   VARCHAR(50) NOT NULL                COMMENT '岗位名称',
  order_num   INT         NOT NULL DEFAULT 0      COMMENT '显示顺序',
  status      CHAR(1)     NOT NULL DEFAULT '0'    COMMENT '状态(0正常 1停用)',
  del_flag    CHAR(1)     NOT NULL DEFAULT '0'    COMMENT '删除标志(0存在 2删除)',
  create_by   BIGINT               DEFAULT NULL   COMMENT '创建者',
  create_time DATETIME             DEFAULT NULL   COMMENT '创建时间',
  update_by   BIGINT               DEFAULT NULL   COMMENT '更新者',
  update_time DATETIME             DEFAULT NULL   COMMENT '更新时间',
  remark      VARCHAR(500)         DEFAULT NULL   COMMENT '备注',
  PRIMARY KEY (post_id),
  UNIQUE KEY uk_post_code (post_code)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='岗位表';

-- ------------------------------------------------------------
-- 4. 角色表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
  role_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  role_name   VARCHAR(30)  NOT NULL                COMMENT '角色名称',
  role_key    VARCHAR(100) NOT NULL                COMMENT '角色权限字符串',
  role_sort   INT          NOT NULL DEFAULT 0      COMMENT '显示顺序',
  data_scope  CHAR(1)      NOT NULL DEFAULT '1'    COMMENT '数据范围(1全部 2本部门 3本部门及以下 4仅本人)',
  status      CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '状态(0正常 1停用)',
  del_flag    CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志(0存在 2删除)',
  create_by   BIGINT                DEFAULT NULL   COMMENT '创建者',
  create_time DATETIME              DEFAULT NULL   COMMENT '创建时间',
  update_by   BIGINT                DEFAULT NULL   COMMENT '更新者',
  update_time DATETIME              DEFAULT NULL   COMMENT '更新时间',
  remark      VARCHAR(500)          DEFAULT NULL   COMMENT '备注',
  PRIMARY KEY (role_id),
  UNIQUE KEY uk_role_key (role_key)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';

-- ------------------------------------------------------------
-- 5. 菜单权限表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
  menu_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  menu_name   VARCHAR(50)  NOT NULL                COMMENT '菜单名称',
  parent_id   BIGINT       NOT NULL DEFAULT 0      COMMENT '父菜单ID',
  order_num   INT          NOT NULL DEFAULT 0      COMMENT '显示顺序',
  path        VARCHAR(200)          DEFAULT ''     COMMENT '路由地址',
  component   VARCHAR(255)          DEFAULT NULL   COMMENT '组件路径',
  is_frame    CHAR(1)      NOT NULL DEFAULT '1'    COMMENT '是否外链(0是 1否)',
  is_cache    CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '是否缓存(0缓存 1不缓存)',
  menu_type   CHAR(1)      NOT NULL DEFAULT ''     COMMENT '菜单类型(M目录 C菜单 F按钮)',
  visible     CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '显示状态(0显示 1隐藏)',
  status      CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '菜单状态(0正常 1停用)',
  perms       VARCHAR(100)          DEFAULT NULL   COMMENT '权限标识',
  icon        VARCHAR(100)          DEFAULT '#'    COMMENT '菜单图标',
  create_by   BIGINT                DEFAULT NULL   COMMENT '创建者',
  create_time DATETIME              DEFAULT NULL   COMMENT '创建时间',
  update_by   BIGINT                DEFAULT NULL   COMMENT '更新者',
  update_time DATETIME              DEFAULT NULL   COMMENT '更新时间',
  remark      VARCHAR(500)          DEFAULT ''     COMMENT '备注',
  PRIMARY KEY (menu_id),
  KEY idx_menu_parent (parent_id)
) ENGINE=InnoDB AUTO_INCREMENT=2000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单权限表';

-- ------------------------------------------------------------
-- 6. 用户-角色关联表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  PRIMARY KEY (user_id, role_id),
  KEY idx_ur_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户角色关联表';

-- ------------------------------------------------------------
-- 7. 用户-岗位关联表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_user_post;
CREATE TABLE sys_user_post (
  user_id BIGINT NOT NULL COMMENT '用户ID',
  post_id BIGINT NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (user_id, post_id),
  KEY idx_up_post (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户岗位关联表';

-- ------------------------------------------------------------
-- 8. 角色-菜单关联表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
  role_id BIGINT NOT NULL COMMENT '角色ID',
  menu_id BIGINT NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (role_id, menu_id),
  KEY idx_rm_menu (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色菜单关联表';

-- ------------------------------------------------------------
-- 9. 角色-部门关联表（数据权限）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_role_dept;
CREATE TABLE sys_role_dept (
  role_id BIGINT NOT NULL COMMENT '角色ID',
  dept_id BIGINT NOT NULL COMMENT '部门ID',
  PRIMARY KEY (role_id, dept_id),
  KEY idx_rd_dept (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色部门关联表';

-- ------------------------------------------------------------
-- 10. 字典类型表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_dict_type;
CREATE TABLE sys_dict_type (
  dict_id     BIGINT      NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  dict_name   VARCHAR(100) NOT NULL DEFAULT ''    COMMENT '字典名称',
  dict_type   VARCHAR(100) NOT NULL DEFAULT ''    COMMENT '字典类型',
  status      CHAR(1)     NOT NULL DEFAULT '0'    COMMENT '状态(0正常 1停用)',
  create_by   BIGINT               DEFAULT NULL   COMMENT '创建者',
  create_time DATETIME             DEFAULT NULL   COMMENT '创建时间',
  update_by   BIGINT               DEFAULT NULL   COMMENT '更新者',
  update_time DATETIME             DEFAULT NULL   COMMENT '更新时间',
  remark      VARCHAR(500)         DEFAULT NULL   COMMENT '备注',
  PRIMARY KEY (dict_id),
  UNIQUE KEY uk_dict_type (dict_type)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典类型表';

-- ------------------------------------------------------------
-- 11. 字典数据表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_dict_data;
CREATE TABLE sys_dict_data (
  dict_code   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  dict_sort   INT          NOT NULL DEFAULT 0      COMMENT '字典排序',
  dict_label  VARCHAR(100) NOT NULL DEFAULT ''     COMMENT '字典标签',
  dict_value  VARCHAR(100) NOT NULL DEFAULT ''     COMMENT '字典键值',
  dict_type   VARCHAR(100) NOT NULL DEFAULT ''     COMMENT '字典类型',
  css_class   VARCHAR(100)          DEFAULT NULL   COMMENT '样式属性',
  list_class  VARCHAR(100)          DEFAULT NULL   COMMENT '表格回显样式',
  is_default  CHAR(1)      NOT NULL DEFAULT 'N'    COMMENT '是否默认(Y是 N否)',
  status      CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '状态(0正常 1停用)',
  create_by   BIGINT                DEFAULT NULL   COMMENT '创建者',
  create_time DATETIME              DEFAULT NULL   COMMENT '创建时间',
  update_by   BIGINT                DEFAULT NULL   COMMENT '更新者',
  update_time DATETIME              DEFAULT NULL   COMMENT '更新时间',
  remark      VARCHAR(500)          DEFAULT NULL   COMMENT '备注',
  PRIMARY KEY (dict_code),
  KEY idx_dd_type (dict_type)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典数据表';

-- ------------------------------------------------------------
-- 12. 参数配置表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
  config_id    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  config_name  VARCHAR(100) NOT NULL DEFAULT ''     COMMENT '参数名称',
  config_key   VARCHAR(100) NOT NULL DEFAULT ''     COMMENT '参数键名',
  config_value VARCHAR(500) NOT NULL DEFAULT ''     COMMENT '参数键值',
  config_type  CHAR(1)      NOT NULL DEFAULT 'N'    COMMENT '系统内置(Y是 N否)',
  create_by    BIGINT                DEFAULT NULL   COMMENT '创建者',
  create_time  DATETIME              DEFAULT NULL   COMMENT '创建时间',
  update_by    BIGINT                DEFAULT NULL   COMMENT '更新者',
  update_time  DATETIME              DEFAULT NULL   COMMENT '更新时间',
  remark       VARCHAR(500)          DEFAULT NULL   COMMENT '备注',
  PRIMARY KEY (config_id),
  UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='参数配置表';

-- ------------------------------------------------------------
-- 13. 操作日志表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_oper_log;
CREATE TABLE sys_oper_log (
  oper_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  title          VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '模块标题',
  business_type  INT          NOT NULL DEFAULT 0      COMMENT '业务类型(0其它 1新增 2修改 3删除 4导出 5导入)',
  method         VARCHAR(200)          DEFAULT ''     COMMENT '方法名称',
  request_method VARCHAR(10)           DEFAULT ''     COMMENT '请求方式',
  oper_name      VARCHAR(50)           DEFAULT ''     COMMENT '操作人员',
  oper_url       VARCHAR(255)          DEFAULT ''     COMMENT '请求URL',
  oper_ip        VARCHAR(128)          DEFAULT ''     COMMENT '操作IP',
  oper_param     VARCHAR(2000)         DEFAULT ''     COMMENT '请求参数',
  json_result    VARCHAR(2000)         DEFAULT ''     COMMENT '返回结果',
  status         INT          NOT NULL DEFAULT 0      COMMENT '操作状态(0正常 1异常)',
  error_msg      VARCHAR(2000)         DEFAULT ''     COMMENT '错误消息',
  cost_time      BIGINT       NOT NULL DEFAULT 0      COMMENT '消耗时间(毫秒)',
  oper_time      DATETIME              DEFAULT NULL   COMMENT '操作时间',
  PRIMARY KEY (oper_id),
  KEY idx_ol_time (oper_time),
  KEY idx_ol_name (oper_name)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';

-- ------------------------------------------------------------
-- 14. 登录日志表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_login_log;
CREATE TABLE sys_login_log (
  info_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  username    VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '登录账号',
  ipaddr      VARCHAR(128) NOT NULL DEFAULT ''     COMMENT '登录IP地址',
  browser     VARCHAR(100)          DEFAULT ''     COMMENT '浏览器类型',
  os          VARCHAR(100)          DEFAULT ''     COMMENT '操作系统',
  status      CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '登录状态(0成功 1失败)',
  msg         VARCHAR(255)          DEFAULT ''     COMMENT '提示消息',
  login_time  DATETIME              DEFAULT NULL   COMMENT '访问时间',
  PRIMARY KEY (info_id),
  KEY idx_ll_time (login_time),
  KEY idx_ll_username (username)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='登录日志表';

-- ============================================================
--                        业务域
-- ============================================================

-- ------------------------------------------------------------
-- 15. 项目表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS pm_project;
CREATE TABLE pm_project (
  project_id   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  project_code VARCHAR(50)  NOT NULL                COMMENT '项目编号',
  project_name VARCHAR(100) NOT NULL                COMMENT '项目名称',
  manager_id   BIGINT                DEFAULT NULL   COMMENT '项目经理ID',
  dept_id      BIGINT                DEFAULT NULL   COMMENT '所属部门ID',
  description  TEXT                                 COMMENT '项目描述',
  priority     CHAR(1)      NOT NULL DEFAULT '2'    COMMENT '优先级(1高 2中 3低)',
  status       CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '状态(0待审批 1进行中 2已完成 3已暂停 4已终止)',
  progress     INT          NOT NULL DEFAULT 0      COMMENT '进度百分比',
  budget       DECIMAL(14,2)         DEFAULT 0.00   COMMENT '项目预算',
  start_date   DATE                  DEFAULT NULL   COMMENT '计划开始日期',
  end_date     DATE                  DEFAULT NULL   COMMENT '计划结束日期',
  actual_end   DATE                  DEFAULT NULL   COMMENT '实际结束日期',
  del_flag     CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志(0存在 2删除)',
  create_by    BIGINT                DEFAULT NULL   COMMENT '创建者',
  create_time  DATETIME              DEFAULT NULL   COMMENT '创建时间',
  update_by    BIGINT                DEFAULT NULL   COMMENT '更新者',
  update_time  DATETIME              DEFAULT NULL   COMMENT '更新时间',
  remark       VARCHAR(500)          DEFAULT NULL   COMMENT '备注',
  PRIMARY KEY (project_id),
  UNIQUE KEY uk_project_code (project_code),
  KEY idx_pj_status (status),
  KEY idx_pj_manager (manager_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目表';

-- ------------------------------------------------------------
-- 16. 项目成员表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS pm_project_member;
CREATE TABLE pm_project_member (
  id           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  project_id   BIGINT      NOT NULL                COMMENT '项目ID',
  user_id      BIGINT      NOT NULL                COMMENT '用户ID',
  role_in_pj   VARCHAR(30) NOT NULL DEFAULT '成员' COMMENT '项目内角色',
  join_time    DATETIME             DEFAULT NULL   COMMENT '加入时间',
  create_time  DATETIME             DEFAULT NULL   COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_pm_project_user (project_id, user_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目成员表';

-- ------------------------------------------------------------
-- 17. 里程碑表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS pm_milestone;
CREATE TABLE pm_milestone (
  milestone_id   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '里程碑ID',
  project_id     BIGINT       NOT NULL                COMMENT '项目ID',
  milestone_name VARCHAR(100) NOT NULL                COMMENT '里程碑名称',
  plan_date      DATE                  DEFAULT NULL   COMMENT '计划完成日期',
  actual_date    DATE                  DEFAULT NULL   COMMENT '实际完成日期',
  status         CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '状态(0未开始 1进行中 2已完成 3已延期)',
  order_num      INT          NOT NULL DEFAULT 0      COMMENT '显示顺序',
  del_flag       CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志(0存在 2删除)',
  create_by      BIGINT                DEFAULT NULL   COMMENT '创建者',
  create_time    DATETIME              DEFAULT NULL   COMMENT '创建时间',
  update_by      BIGINT                DEFAULT NULL   COMMENT '更新者',
  update_time    DATETIME              DEFAULT NULL   COMMENT '更新时间',
  remark         VARCHAR(500)          DEFAULT NULL   COMMENT '备注',
  PRIMARY KEY (milestone_id),
  KEY idx_ms_project (project_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='里程碑表';

-- ------------------------------------------------------------
-- 18. 任务表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS pm_task;
CREATE TABLE pm_task (
  task_id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  project_id   BIGINT       NOT NULL                COMMENT '项目ID',
  parent_id    BIGINT       NOT NULL DEFAULT 0      COMMENT '父任务ID(0为顶级)',
  milestone_id BIGINT                DEFAULT NULL   COMMENT '所属里程碑ID',
  task_name    VARCHAR(200) NOT NULL                COMMENT '任务名称',
  description  TEXT                                 COMMENT '任务描述',
  assignee_id  BIGINT                DEFAULT NULL   COMMENT '负责人ID',
  priority     CHAR(1)      NOT NULL DEFAULT '2'    COMMENT '优先级(1高 2中 3低)',
  status       CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '状态(0待开始 1进行中 2已完成 3已挂起)',
  progress     INT          NOT NULL DEFAULT 0      COMMENT '进度百分比',
  plan_start   DATE                  DEFAULT NULL   COMMENT '计划开始日期',
  plan_end     DATE                  DEFAULT NULL   COMMENT '计划结束日期',
  actual_end   DATE                  DEFAULT NULL   COMMENT '实际完成日期',
  estimate_hours DECIMAL(8,2)        DEFAULT 0.00   COMMENT '预估工时',
  actual_hours   DECIMAL(8,2)        DEFAULT 0.00   COMMENT '实际工时',
  order_num    INT          NOT NULL DEFAULT 0      COMMENT '排序号',
  del_flag     CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志(0存在 2删除)',
  create_by    BIGINT                DEFAULT NULL   COMMENT '创建者',
  create_time  DATETIME              DEFAULT NULL   COMMENT '创建时间',
  update_by    BIGINT                DEFAULT NULL   COMMENT '更新者',
  update_time  DATETIME              DEFAULT NULL   COMMENT '更新时间',
  remark       VARCHAR(500)          DEFAULT NULL   COMMENT '备注',
  PRIMARY KEY (task_id),
  KEY idx_tk_project (project_id),
  KEY idx_tk_assignee (assignee_id),
  KEY idx_tk_status (status)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='任务表';

-- ------------------------------------------------------------
-- 19. 任务评论表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS pm_task_comment;
CREATE TABLE pm_task_comment (
  comment_id  BIGINT   NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  task_id     BIGINT   NOT NULL                COMMENT '任务ID',
  user_id     BIGINT   NOT NULL                COMMENT '评论人ID',
  content     VARCHAR(1000) NOT NULL           COMMENT '评论内容',
  del_flag    CHAR(1)  NOT NULL DEFAULT '0'    COMMENT '删除标志(0存在 2删除)',
  create_time DATETIME          DEFAULT NULL   COMMENT '创建时间',
  PRIMARY KEY (comment_id),
  KEY idx_tc_task (task_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='任务评论表';

-- ------------------------------------------------------------
-- 20. 工时表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS pm_work_log;
CREATE TABLE pm_work_log (
  log_id      BIGINT        NOT NULL AUTO_INCREMENT COMMENT '工时ID',
  project_id  BIGINT        NOT NULL                COMMENT '项目ID',
  task_id     BIGINT                 DEFAULT NULL   COMMENT '任务ID',
  user_id     BIGINT        NOT NULL                COMMENT '填报人ID',
  work_date   DATE          NOT NULL                COMMENT '工作日期',
  hours       DECIMAL(5,2)  NOT NULL DEFAULT 0.00   COMMENT '工时数',
  content     VARCHAR(500)           DEFAULT ''     COMMENT '工作内容',
  status      CHAR(1)       NOT NULL DEFAULT '0'    COMMENT '状态(0待审批 1已通过 2已驳回)',
  audit_by    BIGINT                 DEFAULT NULL   COMMENT '审批人ID',
  audit_time  DATETIME               DEFAULT NULL   COMMENT '审批时间',
  audit_remark VARCHAR(500)          DEFAULT NULL   COMMENT '审批意见',
  del_flag    CHAR(1)       NOT NULL DEFAULT '0'    COMMENT '删除标志(0存在 2删除)',
  create_time DATETIME               DEFAULT NULL   COMMENT '创建时间',
  update_time DATETIME               DEFAULT NULL   COMMENT '更新时间',
  PRIMARY KEY (log_id),
  KEY idx_wl_project (project_id),
  KEY idx_wl_user_date (user_id, work_date)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工时表';

-- ------------------------------------------------------------
-- 21. 附件表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS pm_attachment;
CREATE TABLE pm_attachment (
  attach_id   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '附件ID',
  biz_type    VARCHAR(30)  NOT NULL DEFAULT ''     COMMENT '业务类型(project/task/worklog)',
  biz_id      BIGINT       NOT NULL DEFAULT 0      COMMENT '业务ID',
  file_name   VARCHAR(200) NOT NULL                COMMENT '原始文件名',
  file_path   VARCHAR(500) NOT NULL                COMMENT '存储路径',
  file_suffix VARCHAR(20)           DEFAULT ''     COMMENT '文件后缀',
  file_size   BIGINT       NOT NULL DEFAULT 0      COMMENT '文件大小(字节)',
  file_md5    VARCHAR(64)           DEFAULT ''     COMMENT '文件MD5',
  upload_by   BIGINT                DEFAULT NULL   COMMENT '上传人ID',
  del_flag    CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志(0存在 2删除)',
  create_time DATETIME              DEFAULT NULL   COMMENT '上传时间',
  PRIMARY KEY (attach_id),
  KEY idx_at_biz (biz_type, biz_id),
  KEY idx_at_md5 (file_md5)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='附件表';

-- ------------------------------------------------------------
-- 22. 消息通知表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS pm_message;
CREATE TABLE pm_message (
  message_id  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  receiver_id BIGINT       NOT NULL                COMMENT '接收人ID',
  sender_id   BIGINT                DEFAULT NULL   COMMENT '发送人ID',
  title       VARCHAR(200) NOT NULL DEFAULT ''     COMMENT '消息标题',
  content     VARCHAR(1000)         DEFAULT ''     COMMENT '消息内容',
  msg_type    CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '消息类型(0系统 1任务 2项目 3工时 4预警)',
  biz_type    VARCHAR(30)           DEFAULT NULL   COMMENT '关联业务类型',
  biz_id      BIGINT                DEFAULT NULL   COMMENT '关联业务ID',
  is_read     CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '是否已读(0未读 1已读)',
  read_time   DATETIME              DEFAULT NULL   COMMENT '阅读时间',
  del_flag    CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志(0存在 2删除)',
  create_time DATETIME              DEFAULT NULL   COMMENT '创建时间',
  PRIMARY KEY (message_id),
  KEY idx_mg_receiver (receiver_id, is_read)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='消息通知表';

-- ------------------------------------------------------------
-- 23. 项目燃尽图快照表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS pm_project_burndown;
CREATE TABLE pm_project_burndown (
  snapshot_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '快照ID',
  project_id      BIGINT       NOT NULL                COMMENT '项目ID',
  snapshot_date   DATE         NOT NULL                COMMENT '快照日期',
  total_hours     DECIMAL(10,2) NOT NULL DEFAULT 0.00   COMMENT '总预估工时',
  remaining_hours DECIMAL(10,2) NOT NULL DEFAULT 0.00   COMMENT '剩余工时',
  completed_hours DECIMAL(10,2) NOT NULL DEFAULT 0.00   COMMENT '已完成工时',
  create_time     DATETIME              DEFAULT NULL   COMMENT '创建时间',
  PRIMARY KEY (snapshot_id),
  UNIQUE KEY uk_bd_project_date (project_id, snapshot_date)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目燃尽图快照表';

SET FOREIGN_KEY_CHECKS = 1;
