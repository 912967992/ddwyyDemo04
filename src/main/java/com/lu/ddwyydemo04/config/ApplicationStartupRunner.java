package com.lu.ddwyydemo04.config;

import com.lu.ddwyydemo04.Service.DingTalkUserCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 应用启动初始化类
 * 在应用启动时执行清理用户信息缓存等初始化操作
 */
@Component
@Order(1) // 设置执行顺序，数字越小越先执行
public class ApplicationStartupRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupRunner.class);

    @Autowired
    private DingTalkUserCacheService dingTalkUserCacheService;

    /**
     * 应用启动后执行的方法
     * @param args 应用启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        try {
            logger.info("=========================================");
            logger.info("应用启动初始化：开始清理Redis中的用户信息缓存...");
            
            // 清理所有用户信息缓存
            int clearedCount = dingTalkUserCacheService.clearAllUserInfoCache();
            
            logger.info("应用启动初始化完成：已清理 {} 个用户信息缓存", clearedCount);
            logger.info("=========================================");
            
        } catch (Exception e) {
            logger.error("应用启动初始化时发生错误：清理用户信息缓存失败", e);
            // 不抛出异常，避免影响应用启动
        }
    }
}
