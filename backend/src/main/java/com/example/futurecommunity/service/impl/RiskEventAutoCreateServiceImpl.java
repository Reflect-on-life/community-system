package com.example.futurecommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.futurecommunity.model.Notice;
import com.example.futurecommunity.model.RiskEvent;
import com.example.futurecommunity.service.NoticeService;
import com.example.futurecommunity.service.RiskEventAutoCreateService;
import com.example.futurecommunity.service.RiskEventCacheService;
import com.example.futurecommunity.service.RiskEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 风险事件自动创建服务实现类
 */
@Service
public class RiskEventAutoCreateServiceImpl implements RiskEventAutoCreateService {
    
    @Autowired
    private RiskEventService riskEventService;
    
    @Autowired
    private NoticeService noticeService;
    
    @Autowired
    private RiskEventCacheService riskEventCacheService;
    
    // 5 分钟时间窗口（毫秒）
    private static final long FIVE_MINUTES = 5 * 60 * 1000;
    
    @Override
    public RiskEvent autoCreateRiskEvent(
        String riskType,
        String riskLevel,
        String location,
        String imageUrl,
        String description,
        String cameraId,
        String glmResult,
        String baiduLabels
    ) {
        System.out.println("=== 开始自动创建风险事件 ===");
        System.out.println("风险类型：" + riskType);
        System.out.println("风险等级：" + riskLevel);
        System.out.println("位置：" + location);
        System.out.println("摄像头 ID: " + cameraId);
        
        // 1. 首先检查内存缓存（快速去重）
        if (riskEventCacheService.isDuplicateRisk(cameraId, riskType)) {
            System.out.println("内存缓存检测到重复风险，跳过创建（5 分钟内）");
            System.out.println("========================");
            return null;
        }
        
        // 2. 检查数据库（持久化去重）
        if (hasRecentSameRisk(riskType, cameraId)) {
            System.out.println("数据库检测到重复风险，跳过创建（5 分钟内）");
            System.out.println("========================");
            return null;
        }
        
        try {
            // 2. 创建风险事件对象
            RiskEvent riskEvent = new RiskEvent();
            
            // 3. 自动生成标题
            String title = generateTitle(riskType, location);
            riskEvent.setTitle(title);
            
            // 4. 设置基本信息
            riskEvent.setRiskType(riskType);
            riskEvent.setRiskLevel(riskLevel);
            riskEvent.setLocation(location);
            riskEvent.setImageUrl(imageUrl);
            riskEvent.setDescription(description);
            
            // 5. 设置系统自动上报
            riskEvent.setReporterId(null); // null 表示系统自动上报
            riskEvent.setStatus("PENDING");
            
            // 6. 设置摄像头 ID
            riskEvent.setCameraId(cameraId);
            
            // 7. 设置时间
            Date now = new Date();
            riskEvent.setCreateTime(now);
            riskEvent.setUpdateTime(now);
            
            // 8. 设置 AI 识别结果
            String aiResult = "AI 自动识别：风险类型=" + riskType + ", 风险等级=" + riskLevel;
            riskEvent.setAiResult(aiResult);
            riskEvent.setAiConfidence(0.9);

            // 9. 设置 GLM 和百度 AI 原始结果
            riskEvent.setGlmResult(glmResult);
            riskEvent.setBaiduLabels(baiduLabels);
            
            // 9. 保存到数据库
            riskEventService.save(riskEvent);
            
            System.out.println("风险事件保存成功，事件 ID: " + riskEvent.getId());
            System.out.println("标题：" + title);
            System.out.println("描述：" + description);
            
            // 10. 更新内存缓存（记录新创建的风险事件）
            riskEventCacheService.recordRiskEvent(cameraId, riskType, riskEvent.getId());
            
            // 11. 发送系统通知（高风险）
            if ("HIGH".equals(riskLevel) || "高".equals(riskLevel)) {
                sendSystemNotification(riskEvent);
            }
            
            System.out.println("========================");
            
            return riskEvent;
            
        } catch (Exception e) {
            System.err.println("自动创建风险事件失败：" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public boolean hasRecentSameRisk(String riskType, String cameraId) {
        try {
            // 计算 5 分钟前的时间
            Date fiveMinutesAgo = new Date(System.currentTimeMillis() - FIVE_MINUTES);
            
            // 构建查询条件
            LambdaQueryWrapper<RiskEvent> queryWrapper = new LambdaQueryWrapper<>();
            
            // 风险类型相同
            queryWrapper.eq(RiskEvent::getRiskType, riskType);
            
            // 同一摄像头
            if (cameraId != null && !cameraId.isEmpty()) {
                queryWrapper.eq(RiskEvent::getCameraId, cameraId);
            }
            
            // 5 分钟内
            queryWrapper.ge(RiskEvent::getCreateTime, fiveMinutesAgo);
            
            // 查询是否存在
            long count = riskEventService.count(queryWrapper);
            
            System.out.println("检查 5 分钟内重复风险：riskType=" + riskType + ", cameraId=" + cameraId + ", count=" + count);
            
            return count > 0;
            
        } catch (Exception e) {
            System.err.println("检查重复风险失败：" + e.getMessage());
            e.printStackTrace();
            return false; // 查询失败时不阻止创建
        }
    }
    
    @Override
    public String generateTitle(String riskType, String location) {
        StringBuilder title = new StringBuilder();
        
        // 添加位置前缀
        if (location != null && !location.isEmpty()) {
            title.append(location);
        } else {
            title.append("监控区域");
        }
        
        // 添加风险类型
        title.append("发生").append(riskType);
        
        // 添加时间后缀（可选）
        // title.append("(").append(new Date().toLocaleString()).append(")");
        
        return title.toString();
    }
    
    @Override
    public void sendSystemNotification(RiskEvent riskEvent) {
        try {
            System.out.println("=== 发送系统通知 ===");
            
            String riskType = riskEvent.getRiskType();
            String location = riskEvent.getLocation();
            
            // 根据风险类型发送不同的通知
            if ("火灾".equals(riskType)) {
                // 火灾：发送给所有居民和物业
                sendFireNotification(riskEvent, location);
            } else if ("老人摔倒".equals(riskType)) {
                // 老人摔倒：发送给医生和物业
                sendFallNotification(riskEvent, location);
            } else if ("打斗冲突".equals(riskType)) {
                // 打斗冲突：发送给物业和安保
                sendFightNotification(riskEvent, location);
            } else {
                // 其他高风险：发送给物业
                sendGeneralNotification(riskEvent);
            }
            
            System.out.println("========================");
            
        } catch (Exception e) {
            System.err.println("发送系统通知失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 火灾通知：发送给所有居民
     */
    private void sendFireNotification(RiskEvent riskEvent, String location) {
        // 发送给所有居民
        Notice residentNotice = new Notice();
        residentNotice.setTitle("【紧急通知】火灾预警");
        residentNotice.setContent("紧急通知：" + location + "发生火灾，请各位居民注意安全，尽快远离事发地点，请勿乘坐电梯！");
        residentNotice.setTargetType("resident");
        residentNotice.setCreatedAt(new Date());
        residentNotice.setCreatedBy(1L);
        noticeService.save(residentNotice);
        System.out.println("已发送火灾通知给所有居民");
        
        // 发送给物业
        Notice propertyNotice = new Notice();
        propertyNotice.setTitle("【紧急通知】火灾预警");
        propertyNotice.setContent("紧急通知：" + location + "发生火灾，请立即启动应急预案，疏散人员并联系消防部门！");
        propertyNotice.setTargetType("property");
        propertyNotice.setCreatedAt(new Date());
        propertyNotice.setCreatedBy(1L);
        noticeService.save(propertyNotice);
        System.out.println("已发送火灾通知给物业");
    }
    
    /**
     * 老人摔倒通知：发送给医生和物业
     */
    private void sendFallNotification(RiskEvent riskEvent, String location) {
        // 发送给医生
        Notice doctorNotice = new Notice();
        doctorNotice.setTitle("【紧急通知】老人摔倒");
        doctorNotice.setContent("紧急通知：" + location + "发现老人摔倒，请医生紧急前往处理！");
        doctorNotice.setTargetType("doctor");
        doctorNotice.setCreatedAt(new Date());
        doctorNotice.setCreatedBy(1L);
        noticeService.save(doctorNotice);
        System.out.println("已发送老人摔倒通知给医生");
        
        // 发送给物业
        Notice propertyNotice = new Notice();
        propertyNotice.setTitle("【紧急通知】老人摔倒");
        propertyNotice.setContent("紧急通知：" + location + "发现老人摔倒，请立即前往查看并提供帮助！");
        propertyNotice.setTargetType("property");
        propertyNotice.setCreatedAt(new Date());
        propertyNotice.setCreatedBy(1L);
        noticeService.save(propertyNotice);
        System.out.println("已发送老人摔倒通知给物业");
    }
    
    /**
     * 打斗冲突通知：发送给物业和安保
     */
    private void sendFightNotification(RiskEvent riskEvent, String location) {
        Notice notice = new Notice();
        notice.setTitle("【紧急通知】打斗冲突");
        notice.setContent("紧急通知：" + location + "发生打斗冲突，请立即前往制止并调解，必要时联系公安部门！");
        notice.setTargetType("property");
        notice.setCreatedAt(new Date());
        notice.setCreatedBy(1L);
        noticeService.save(notice);
        System.out.println("已发送打斗冲突通知给物业");
    }
    
    /**
     * 一般高风险通知：发送给物业
     */
    private void sendGeneralNotification(RiskEvent riskEvent) {
        Notice notice = new Notice();
        notice.setTitle("【紧急风险预警】自动检测");
        notice.setContent("系统自动检测到：" + riskEvent.getTitle() + 
                        "，风险等级：" + riskEvent.getRiskLevel() +
                        "，请立即处理！");
        notice.setTargetType("property");
        notice.setCreatedAt(new Date());
        notice.setCreatedBy(1L);
        noticeService.save(notice);
        System.out.println("已发送风险预警通知给物业");
    }
}
