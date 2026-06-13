package com.example.futurecommunity.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 风险事件内存缓存服务
 * 
 * 功能：
 * 1. 维护一个 Map<CameraId, LastRiskEvent> 缓存
 * 2. 检查相同摄像头 + 相同风险类型 + 5 分钟内不重复创建
 * 3. 定时清理超过 10 分钟的过期记录
 */
@Service
public class RiskEventCacheService {
    
    // 缓存结构：CameraId -> LastRiskEvent
    private final Map<String, LastRiskEvent> riskEventCache = new ConcurrentHashMap<>();
    
    // 5 分钟时间窗口（毫秒）
    private static final long FIVE_MINUTES = 5 * 60 * 1000;
    
    // 10 分钟过期时间（毫秒）
    private static final long TEN_MINUTES = 10 * 60 * 1000;
    
    // 定时清理任务
    private final ScheduledExecutorService cleanupExecutor;
    
    public RiskEventCacheService() {
        // 初始化定时清理任务，每 2 分钟清理一次
        this.cleanupExecutor = Executors.newScheduledThreadPool(1);
        this.cleanupExecutor.scheduleAtFixedRate(
            this::cleanupExpiredRecords,
            2,
            2,
            TimeUnit.MINUTES
        );
        
        System.out.println("=== 风险事件缓存服务已启动 ===");
        System.out.println("清理间隔：2 分钟");
        System.out.println("过期时间：10 分钟");
        System.out.println("========================");
    }
    
    /**
     * 检查是否存在重复的风险事件
     * 
     * @param cameraId 摄像头 ID
     * @param riskType 风险类型
     * @return true-存在重复（5 分钟内），false-无重复
     */
    public boolean isDuplicateRisk(String cameraId, String riskType) {
        if (cameraId == null || riskType == null) {
            return false;
        }
        
        LastRiskEvent lastEvent = riskEventCache.get(cameraId);
        
        if (lastEvent == null) {
            return false;
        }
        
        // 检查是否是相同的风险类型
        if (!riskType.equals(lastEvent.getRiskType())) {
            return false;
        }
        
        // 检查是否在 5 分钟内
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastEvent.getTimestamp();
        
        boolean isDuplicate = timeDiff <= FIVE_MINUTES;
        
        if (isDuplicate) {
            System.out.println("检测到重复风险事件：cameraId=" + cameraId + 
                             ", riskType=" + riskType + 
                             ", 距离上次检测：" + (timeDiff / 1000) + "秒");
        }
        
        return isDuplicate;
    }
    
    /**
     * 记录新创建的风险事件
     * 
     * @param cameraId 摄像头 ID
     * @param riskType 风险类型
     * @param riskEventId 风险事件 ID
     */
    public void recordRiskEvent(String cameraId, String riskType, Long riskEventId) {
        if (cameraId == null || riskType == null) {
            return;
        }
        
        LastRiskEvent lastEvent = new LastRiskEvent();
        lastEvent.setCameraId(cameraId);
        lastEvent.setRiskType(riskType);
        lastEvent.setTimestamp(System.currentTimeMillis());
        lastEvent.setRiskEventId(riskEventId);
        
        riskEventCache.put(cameraId, lastEvent);
        
        System.out.println("已记录风险事件到缓存：cameraId=" + cameraId + 
                         ", riskType=" + riskType + 
                         ", riskEventId=" + riskEventId);
    }
    
    /**
     * 清理过期的记录（超过 10 分钟）
     */
    private void cleanupExpiredRecords() {
        long currentTime = System.currentTimeMillis();
        int removedCount = 0;
        
        for (Map.Entry<String, LastRiskEvent> entry : riskEventCache.entrySet()) {
            LastRiskEvent lastEvent = entry.getValue();
            long timeDiff = currentTime - lastEvent.getTimestamp();
            
            if (timeDiff > TEN_MINUTES) {
                riskEventCache.remove(entry.getKey());
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            System.out.println("清理过期记录：已删除 " + removedCount + " 条记录");
            System.out.println("当前缓存大小：" + riskEventCache.size());
        }
    }
    
    /**
     * 获取缓存大小
     * 
     * @return 缓存中的记录数量
     */
    public int getCacheSize() {
        return riskEventCache.size();
    }
    
    /**
     * 清空缓存
     */
    public void clearCache() {
        riskEventCache.clear();
        System.out.println("缓存已清空");
    }
    
    /**
     * 销毁服务
     */
    public void shutdown() {
        if (cleanupExecutor != null && !cleanupExecutor.isShutdown()) {
            cleanupExecutor.shutdown();
            System.out.println("风险事件缓存服务已停止");
        }
    }
    
    /**
     * 内部类：上次风险事件记录
     */
    private static class LastRiskEvent {
        private String cameraId;      // 摄像头 ID
        private String riskType;      // 风险类型
        private long timestamp;       // 检测时间戳（毫秒）
        private Long riskEventId;     // 已创建的风险事件 ID
        
        public String getCameraId() {
            return cameraId;
        }
        
        public void setCameraId(String cameraId) {
            this.cameraId = cameraId;
        }
        
        public String getRiskType() {
            return riskType;
        }
        
        public void setRiskType(String riskType) {
            this.riskType = riskType;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
        
        public Long getRiskEventId() {
            return riskEventId;
        }
        
        public void setRiskEventId(Long riskEventId) {
            this.riskEventId = riskEventId;
        }
    }
}
