package com.biyesheji.pms.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.module.system.entity.SysConfig;

/**
 * 参数配置服务
 */
public interface ISysConfigService extends IService<SysConfig> {

    PageResult<SysConfig> selectConfigPage(SysConfig config, Integer pageNum, Integer pageSize);

    /**
     * 按键名取参数值（走 Redis 缓存）
     */
    String selectValueByKey(String configKey);

    void checkConfigKeyUnique(SysConfig config);

    /**
     * 清空参数缓存
     */
    void refreshCache();
}
