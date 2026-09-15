package com.biyesheji.pms.module.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.common.exception.ServiceException;
import com.biyesheji.pms.module.system.entity.SysConfig;
import com.biyesheji.pms.module.system.mapper.SysConfigMapper;
import com.biyesheji.pms.module.system.service.ISysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 参数配置服务实现
 * <p>
 * 参数值读取走 Redis 缓存，避免每次业务读取都打库。
 */
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {

    private static final String CONFIG_CACHE_KEY = "pms:config:";
    private static final long CACHE_EXPIRE_MINUTES = 30L;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public PageResult<SysConfig> selectConfigPage(SysConfig config, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        if (config != null) {
            if (StrUtil.isNotBlank(config.getConfigName())) {
                wrapper.like(SysConfig::getConfigName, config.getConfigName());
            }
            if (StrUtil.isNotBlank(config.getConfigKey())) {
                wrapper.like(SysConfig::getConfigKey, config.getConfigKey());
            }
        }
        wrapper.orderByAsc(SysConfig::getConfigId);
        Page<SysConfig> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page);
    }

    @Override
    public String selectValueByKey(String configKey) {
        if (StrUtil.isBlank(configKey)) {
            return null;
        }
        String cacheKey = CONFIG_CACHE_KEY + configKey;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached.toString();
        }
        String value = baseMapper.selectValueByKey(configKey);
        if (value != null) {
            redisTemplate.opsForValue().set(cacheKey, value, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
        return value;
    }

    @Override
    public void checkConfigKeyUnique(SysConfig config) {
        Long configId = config.getConfigId() == null ? -1L : config.getConfigId();
        SysConfig exist = lambdaQuery().eq(SysConfig::getConfigKey, config.getConfigKey()).one();
        if (exist != null && !exist.getConfigId().equals(configId)) {
            throw new ServiceException("参数键名已存在");
        }
    }

    @Override
    public void refreshCache() {
        Set<String> keys = redisTemplate.keys(CONFIG_CACHE_KEY + "*");
        if (CollUtil.isNotEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }
}
