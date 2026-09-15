package com.biyesheji.pms.common.enums;

/**
 * 响应状态码
 */
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    CREATED(201, "创建成功"),
    BAD_REQUEST(400, "请求参数有误"),
    UNAUTHORIZED(401, "登录状态已过期，请重新登录"),
    FORBIDDEN(403, "没有访问权限，请联系管理员"),
    NOT_FOUND(404, "请求的资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "数据已存在，请勿重复提交"),
    ERROR(500, "系统内部错误，请联系管理员"),

    /* ===== 业务相关 ===== */
    CAPTCHA_ERROR(1001, "验证码错误或已过期"),
    CAPTCHA_EXPIRED(1002, "验证码已过期"),
    USERNAME_OR_PASSWORD_ERROR(1003, "账号或密码错误"),
    USER_DISABLED(1004, "账号已被停用，请联系管理员"),
    USER_NOT_EXIST(1005, "用户不存在"),
    OLD_PASSWORD_ERROR(1006, "原密码不正确"),
    PASSWORD_NOT_MATCH(1007, "两次输入的密码不一致"),

    ROLE_KEY_EXIST(1101, "角色权限字符已存在"),
    ROLE_ASSIGNED_CANNOT_DELETE(1102, "角色已分配用户，不能删除"),
    ADMIN_ROLE_CANNOT_OPERATE(1103, "不允许操作超级管理员角色"),

    DEPT_HAS_CHILDREN(1201, "存在下级部门，不允许删除"),
    DEPT_HAS_USER(1202, "部门下存在用户，不允许删除"),

    MENU_HAS_CHILDREN(1301, "存在子菜单，不允许删除"),
    MENU_ASSIGNED_CANNOT_DELETE(1302, "菜单已分配给角色，不允许删除"),

    USERNAME_EXIST(1401, "登录账号已存在"),
    PHONE_EXIST(1402, "手机号已被使用"),
    EMAIL_EXIST(1403, "邮箱已被使用"),
    ADMIN_CANNOT_OPERATE(1404, "不允许操作超级管理员用户"),

    PROJECT_CODE_EXIST(1501, "项目编号已存在"),
    PROJECT_NOT_EXIST(1502, "项目不存在"),
    PROJECT_STATUS_ILLEGAL(1503, "当前项目状态不允许该操作"),

    TASK_NOT_EXIST(1601, "任务不存在"),
    TASK_STATUS_ILLEGAL(1602, "当前任务状态不允许该操作"),

    FILE_EMPTY(1701, "上传文件不能为空"),
    FILE_TYPE_NOT_ALLOWED(1702, "不支持的文件类型"),
    FILE_SIZE_EXCEED(1703, "文件大小超出限制"),
    FILE_UPLOAD_ERROR(1704, "文件上传失败");

    private final Integer code;
    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
