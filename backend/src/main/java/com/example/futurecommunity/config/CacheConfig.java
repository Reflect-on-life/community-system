package com.example.futurecommunity.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存配置类
 * 用于启用 Spring 缓存功能（支持 @Cacheable 注解）
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 配置缓存管理器
     * 使用基于 ConcurrentHashMap 的简单缓存管理器（内存缓存）
     */
    @Bean
    public CacheManager cacheManager() {
        // 使用构造函数传入缓存名称
        return new ConcurrentMapCacheManager("baiduToken");
    }
}
