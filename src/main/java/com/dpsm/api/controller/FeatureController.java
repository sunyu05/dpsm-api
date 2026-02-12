package com.dpsm.api.controller;

import com.dpsm.api.config.AppConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 功能开关和配置管理控制器
 * 演示如何使用 AppConfig 进行动态配置管理
 *
 * @author dpsm
 */
@Slf4j
@RestController
@RequestMapping("/api/features")
@RequiredArgsConstructor
public class FeatureController {

    private final AppConfigService appConfigService;

    /**
     * 获取所有功能开关状态
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFeatures() {
        Map<String, Object> features = new HashMap<>();
        
        // 获取功能开关状态
        features.put("advancedSearch", getFeatureStatus("services.dpsm-api.features.advancedSearch"));
        features.put("fileUpload", getFeatureStatus("services.dpsm-api.features.fileUpload"));
        features.put("exportData", getFeatureStatus("services.dpsm-api.features.exportData"));
        
        return ResponseEntity.ok(features);
    }

    /**
     * 获取特定功能状态
     */
    @GetMapping("/{featureName}")
    public ResponseEntity<Map<String, Object>> getFeature(@PathVariable String featureName) {
        String configKey = "services.dpsm-api.features." + featureName;
        Map<String, Object> feature = getFeatureStatus(configKey);
        
        if (feature.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(feature);
    }

    /**
     * 获取系统限制配置
     */
    @GetMapping("/limits")
    public ResponseEntity<Map<String, Object>> getLimits() {
        Map<String, Object> limits = new HashMap<>();
        
        limits.put("maxRequestsPerMinute", 
            appConfigService.getIntValue("services.dpsm-api.limits.maxRequestsPerMinute", 1000));
        limits.put("maxUploadSizeMB", 
            appConfigService.getIntValue("services.dpsm-api.limits.maxUploadSizeMB", 10));
        limits.put("maxConcurrentRequests", 
            appConfigService.getIntValue("services.dpsm-api.limits.maxConcurrentRequests", 100));
        limits.put("requestTimeoutSeconds", 
            appConfigService.getIntValue("services.dpsm-api.limits.requestTimeoutSeconds", 30));
        
        return ResponseEntity.ok(limits);
    }

    /**
     * 获取缓存配置
     */
    @GetMapping("/cache")
    public ResponseEntity<Map<String, Object>> getCacheConfig() {
        Map<String, Object> cache = new HashMap<>();
        
        cache.put("enabled", 
            appConfigService.getBooleanValue("services.dpsm-api.cache.enabled", true));
        cache.put("ttlSeconds", 
            appConfigService.getIntValue("services.dpsm-api.cache.ttlSeconds", 300));
        cache.put("maxEntries", 
            appConfigService.getIntValue("services.dpsm-api.cache.maxEntries", 1000));
        
        return ResponseEntity.ok(cache);
    }

    /**
     * 获取 AppConfig 统计信息
     */
    @GetMapping("/config/stats")
    public ResponseEntity<Map<String, Object>> getConfigStats() {
        return ResponseEntity.ok(appConfigService.getConfigurationStats());
    }

    /**
     * 手动刷新配置
     */
    @PostMapping("/config/refresh")
    public ResponseEntity<Map<String, String>> refreshConfig() {
        appConfigService.manualRefresh();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Configuration refresh triggered");
        response.put("version", appConfigService.getCurrentConfigVersion());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有配置键
     */
    @GetMapping("/config/keys")
    public ResponseEntity<java.util.Set<String>> getAllConfigKeys() {
        return ResponseEntity.ok(appConfigService.getAllConfigurationKeys());
    }

    /**
     * 获取特定配置值
     */
    @GetMapping("/config/{key}")
    public ResponseEntity<Map<String, Object>> getConfigValue(@PathVariable String key) {
        if (!appConfigService.hasConfiguration(key)) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("key", key);
        response.put("value", appConfigService.getConfigurationValue(key, null));
        
        return ResponseEntity.ok(response);
    }

    /**
     * 辅助方法：获取功能状态
     */
    private Map<String, Object> getFeatureStatus(String configKey) {
        Map<String, Object> feature = new HashMap<>();
        
        String enabledKey = configKey + ".enabled";
        String rolloutKey = configKey + ".rolloutPercentage";
        
        if (appConfigService.hasConfiguration(enabledKey)) {
            feature.put("enabled", appConfigService.getBooleanValue(enabledKey, false));
            feature.put("rolloutPercentage", appConfigService.getIntValue(rolloutKey, 0));
        }
        
        return feature;
    }
}
