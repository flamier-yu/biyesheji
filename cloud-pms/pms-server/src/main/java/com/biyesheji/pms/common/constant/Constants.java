package com.biyesheji.pms.common.constant;

/**
 * 全局常量
 */
public final class Constants {

    private Constants() {
    }

    /* ========== 认证相关 ========== */
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    /** Redis 中缓存登录用户信息的 key 前缀 */
    public static final String LOGIN_USER_KEY = "pms:login:user:";
    /** Redis 中缓存验证码的 key 前缀 */
    public static final String CAPTCHA_KEY = "pms:captcha:";
    /** 超级管理员账号 */
    public static final String SUPER_ADMIN = "admin";
    /** 超级管理员用户ID */
    public static final Long ADMIN_USER_ID = 100L;
    /** 超级管理员角色ID */
    public static final Long ADMIN_ROLE_ID = 1L;
    /** 全部权限标识 */
    public static final String ALL_PERMISSION = "*:*:*";

    /* ========== 通用状态 ========== */
    public static final String YES = "Y";
    public static final String NO = "N";
    /** 状态：正常 */
    public static final String STATUS_NORMAL = "0";
    /** 状态：停用 */
    public static final String STATUS_DISABLE = "1";
    /** 未删除 */
    public static final String DEL_FLAG_NORMAL = "0";
    /** 已删除 */
    public static final String DEL_FLAG_DELETED = "2";
    /** 默认密码 */
    public static final String DEFAULT_PASSWORD = "123456";

    /* ========== 菜单类型 ========== */
    /** 目录 */
    public static final String MENU_TYPE_DIR = "M";
    /** 菜单 */
    public static final String MENU_TYPE_MENU = "C";
    /** 按钮 */
    public static final String MENU_TYPE_BUTTON = "F";

    /* ========== 数据范围 ========== */
    /** 全部数据 */
    public static final String DATA_SCOPE_ALL = "1";
    /** 本部门数据 */
    public static final String DATA_SCOPE_DEPT = "2";
    /** 本部门及以下数据 */
    public static final String DATA_SCOPE_DEPT_AND_CHILD = "3";
    /** 仅本人数据 */
    public static final String DATA_SCOPE_SELF = "4";

    /* ========== 业务状态 ========== */
    /** 项目/任务：进行中 */
    public static final String BIZ_STATUS_RUNNING = "1";
    /** 项目/任务：已完成 */
    public static final String BIZ_STATUS_DONE = "2";
    /** 工时：待审批 */
    public static final String WORKLOG_PENDING = "0";
    /** 工时：已通过 */
    public static final String WORKLOG_PASSED = "1";
    /** 工时：已驳回 */
    public static final String WORKLOG_REJECTED = "2";

    /* ========== 其他 ========== */
    /** 顶级父节点ID */
    public static final Long ROOT_PARENT_ID = 0L;
    /** 树形结构祖先分隔符 */
    public static final String ANCESTORS_SEPARATOR = ",";
    /** 未知 IP */
    public static final String UNKNOWN_IP = "unknown";
}
