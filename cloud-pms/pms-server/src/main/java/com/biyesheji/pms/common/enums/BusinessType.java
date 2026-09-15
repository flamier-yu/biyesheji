package com.biyesheji.pms.common.enums;

/**
 * 操作日志业务类型
 * 与 sys_oper_log.business_type 字段对应
 */
public enum BusinessType {

    /** 其它 */
    OTHER,
    /** 新增 */
    INSERT,
    /** 修改 */
    UPDATE,
    /** 删除 */
    DELETE,
    /** 授权 */
    GRANT,
    /** 导出 */
    EXPORT,
    /** 导入 */
    IMPORT,
    /** 强退 */
    FORCE,
    /** 清空 */
    CLEAN;

    /**
     * 转换为数据库存储的整型值
     */
    public int value() {
        return this.ordinal();
    }
}
