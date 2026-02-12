package com.dpsm.api.service;

import com.dpsm.api.config.AppConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 功能开关服务
 * 提供功能开关的判断逻辑，支持灰度发布
 *
 * @author dpsm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureFlagService {

    private final AppConfigService appConfigService;

    /**
     * 检查功能是否启用
     *
     * @param featureName 功能名称
     * @return 是否启用
     */
    public boolean isFeatureEnabled(String featureName) {
        String configKey = "services.dpsm-api.features." + featureName + ".enabled";
        boolean enabled = appConfigService.getBooleanValue(configKey, false);
        
        log.debug("Feature '{}' enabled: {}", featureName, enabled);
        return enabled;
    }

    /**
     * 检查功能是否对特定用户启用（支持灰度发布）
     *
     * @param featureName 功能名称
     * @param userId 用户ID
     * @return 是否启用
     */
    public boolean isFeatureEnabledForUser(String featureName, String userId) {
        // 首先检查功能是否全局启用
        if (!isFeatureEnabled(featureName)) {
            return false;
        }
        
        // 获取灰度发布百分比
        String rolloutKey = "services.dpsm-api.features." + featureName + ".rolloutPercentage";
        int rolloutPercentage = appConfigService.getIntValue(rolloutKey, 100);
        
        // 如果是 100%，直接返回 true
        if (rolloutPercentage >= 100) {
            return true;
        }
        
        // 如果是 0%，直接返回 false
        if (rolloutPercentage <= 0) {
            return false;
        }
        
        // 使用用户ID的哈希值决定是否启用
        int userHash = Math.abs(userId.hashCode() % 100);
        boolean enabled = userHash < rolloutPercentage;
        
        log.debug("Feature '{}' enabled for user '{}': {} (rollout: {}%, hash: {})", 
            featureName, userId, enabled, rolloutPercentage, userHash);
        
        return enabled;
    }

    /**
     * 获取限流配置
     *
     * @return 每分钟最大请求数
     */
    public int getMaxRequestsPerMinute() {
        return appConfigService.getIntValue(
            "services.dpsm-api.limits.maxRequestsPerMinute", 1000);
    }

    /**
     * 获取最大上传大小（MB）
     *
     * @return 最大上传大小
     */
    public int getMaxUploadSizeMB() {
        return appConfigService.getIntValue(
            "services.dpsm-api.limits.maxUploadSizeMB", 10);
    }

    /**
     * 获取请求超时时间（秒）
     *
     * @return 超时时间
     */
    public int getRequestTimeoutSeconds() {
        return appConfigService.getIntValue(
            "services.dpsm-api.limits.requestTimeoutSeconds", 30);
    }

    /**
     * 检查缓存是否启用
     *
     * @return 是否启用缓存
     */
    public boolean isCacheEnabled() {
        return appConfigService.getBooleanValue(
            "services.dpsm-api.cache.enabled", true);
    }

    /**
     * 获取缓存 TTL（秒）
     *
     * @return 缓存 TTL
     */
    public int getCacheTtlSeconds() {
        return appConfigService.getIntValue(
            "services.dpsm-api.cache.ttlSeconds", 300);
    }
}
