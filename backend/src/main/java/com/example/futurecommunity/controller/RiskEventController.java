package com.example.futurecommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.futurecommunity.model.Notice;
import com.example.futurecommunity.model.RiskEvent;
import com.example.futurecommunity.service.AIRecognitionService;
import com.example.futurecommunity.service.NoticeService;
import com.example.futurecommunity.service.RiskEventService;
import com.example.futurecommunity.service.RiskEventAutoCreateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/risk")
public class RiskEventController {

    @Autowired
    private RiskEventService riskEventService;
    
    @Autowired
    private AIRecognitionService aiRecognitionService;
    
    @Autowired
    private NoticeService noticeService;
    
    @Autowired
    private RiskEventAutoCreateService riskEventAutoCreateService;

    @PostMapping("/report")
    public RiskEvent reportRisk(@RequestBody RiskEvent riskEvent) {
        // 设置基本状态
        riskEvent.setStatus("PENDING");
        riskEvent.setCreateTime(new Date());
        riskEvent.setUpdateTime(new Date());
        
        // 检查是否为系统自动上报（reporterId 为 0 或 null）
        if (riskEvent.getReporterId() == null || riskEvent.getReporterId() == 0) {
            // 系统自动上报
            riskEvent.setReporterId(null);
            System.out.println("=== 系统自动上报风险事件 ===");
        } else {
            // 用户手动上报
            String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
            if (currentUserId == null) {
                throw new RuntimeException("用户未登录");
            }
            Long userId = Long.parseLong(currentUserId);
            riskEvent.setReporterId(userId);
            System.out.println("=== 用户手动上报风险事件 ===");
            System.out.println("上报用户 ID: " + userId);
        }
        
        String imageUrl = riskEvent.getImageUrl();
        String userRiskType = riskEvent.getRiskType();
        String location = riskEvent.getLocation();
        
        System.out.println("风险位置：" + location);
        System.out.println("摄像头 ID: " + (riskEvent.getCameraId() != null ? riskEvent.getCameraId() : "未指定"));
        
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // 使用完整识别方法，获取 GLM 和百度结果
            Map<String, String> recognitionResult = aiRecognitionService.recognizeRiskWithFullInfo(imageUrl, riskEvent.getTitle());
            
            System.out.println("=== AI 识别结果 ===");
            System.out.println("图片 URL: " + imageUrl);
            System.out.println("GLM 结果：" + recognitionResult.get("glmResult"));
            System.out.println("百度标签：" + recognitionResult.get("baiduLabels"));
            System.out.println("风险类型：" + recognitionResult.get("riskType"));
            System.out.println("风险等级：" + recognitionResult.get("riskLevel"));
            System.out.println("风险描述：" + recognitionResult.get("description"));
            System.out.println("===================");
            
            if (userRiskType == null || userRiskType.isEmpty()) {
                riskEvent.setRiskType(recognitionResult.get("riskType"));
            }
            
            riskEvent.setRiskLevel(recognitionResult.get("riskLevel"));
            
            // 使用 AI 生成的描述，如果没有则使用默认描述
            String aiDescription = recognitionResult.get("description");
            if (aiDescription != null && !aiDescription.isEmpty()) {
                riskEvent.setDescription(aiDescription);
            }
            
            // 保存 AI 识别结果
            String aiResult = "AI 识别结果：风险类型=" + recognitionResult.get("riskType") + 
                            ", 风险等级=" + recognitionResult.get("riskLevel");
            riskEvent.setAiResult(aiResult);
            
            // 保存 GLM 和百度识别结果
            riskEvent.setGlmResult(recognitionResult.get("glmResult"));
            riskEvent.setBaiduLabels(recognitionResult.get("baiduLabels"));
            
            if ("HIGH".equals(recognitionResult.get("riskLevel")) || "高".equals(recognitionResult.get("riskLevel"))) {
                // 高风险：先保存，然后发送按类型分发的通知
                riskEventService.save(riskEvent);
                riskEventAutoCreateService.sendSystemNotification(riskEvent);
                System.out.println("高风险事件保存成功并发送通知，事件 ID: " + riskEvent.getId());
                System.out.println("===================");
                return riskEvent;
            }
        } else {
            if (userRiskType == null || userRiskType.isEmpty()) {
                riskEvent.setRiskType("人工上报");
            }
            
            // 如果用户没有指定风险等级，则默认为 MEDIUM
            if (riskEvent.getRiskLevel() == null || riskEvent.getRiskLevel().isEmpty()) {
                riskEvent.setRiskLevel("MEDIUM");
            }
            
            System.out.println("=== 人工上报 ===");
            System.out.println("未上传图片，使用默认值");
            System.out.println("风险类型：" + (userRiskType != null ? userRiskType : "人工上报"));
            System.out.println("风险等级：" + riskEvent.getRiskLevel());
            System.out.println("===================");
            
            // 手动上报也可能是高风险，需要发送通知
            if ("HIGH".equals(riskEvent.getRiskLevel()) || "高".equals(riskEvent.getRiskLevel())) {
                riskEventService.save(riskEvent);
                riskEventAutoCreateService.sendSystemNotification(riskEvent);
                System.out.println("高风险事件保存成功并发送通知，事件 ID: " + riskEvent.getId());
                System.out.println("===================");
                return riskEvent;
            }
        }
        
        riskEventService.save(riskEvent);
        
        System.out.println("风险事件保存成功，事件 ID: " + riskEvent.getId());
        System.out.println("===================");
        
        return riskEvent;
    }
    
    private void sendAlertNotification(String riskType) {
        try {
            Notice notice = new Notice();
            notice.setTitle("【紧急风险预警】");
            notice.setContent("发现高风险事件：" + riskType);
            notice.setTargetType("PROPERTY");
            notice.setCreatedAt(new Date());
            notice.setCreatedBy(1L);
            
            noticeService.save(notice);
            
            System.out.println("=== 预警通知已发送 ===");
            System.out.println("通知标题：【紧急风险预警】");
            System.out.println("通知内容：发现高风险事件：" + riskType);
            System.out.println("目标用户：物业");
            System.out.println("===================");
        } catch (Exception e) {
            System.err.println("发送预警通知失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @GetMapping("/list")
    public Page<RiskEvent> getRiskList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String riskLevel) {
        
        Page<RiskEvent> riskPage = new Page<>(page, size);
        
        LambdaQueryWrapper<RiskEvent> queryWrapper = new LambdaQueryWrapper<>();
        
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(RiskEvent::getStatus, status);
        }
        
        if (riskLevel != null && !riskLevel.isEmpty()) {
            queryWrapper.eq(RiskEvent::getRiskLevel, riskLevel);
        }
        
        queryWrapper.orderByDesc(RiskEvent::getCreateTime);
        
        riskEventService.page(riskPage, queryWrapper);
        
        return riskPage;
    }

    @PutMapping("/handle/{id}")
    public RiskEvent handleRisk(@PathVariable Long id) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录");
        }
        Long userId = Long.parseLong(currentUserId);
        
        System.out.println("=== 处理风险事件 ===");
        System.out.println("风险事件 ID: " + id);
        System.out.println("处理人 ID: " + userId);
        
        RiskEvent riskEvent = riskEventService.getById(id);
        if (riskEvent == null) {
            System.out.println("风险事件不存在");
            System.out.println("===================");
            throw new RuntimeException("风险事件不存在");
        }
        
        riskEvent.setStatus("DONE");
        riskEvent.setHandlerId(userId);
        riskEvent.setUpdateTime(new Date());
        
        riskEventService.updateById(riskEvent);
        
        System.out.println("风险事件处理完成");
        System.out.println("===================");
        
        return riskEvent;
    }
    
    @GetMapping("/statistics/level")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY')")
    public Map<String, Integer> getRiskLevelStatistics(@RequestParam(defaultValue = "day") String dimension) {
        Map<String, Integer> statistics = new HashMap<>();
        
        // 根据时间维度计算起始时间
        Date startDate = getStartDateByDimension(dimension);
        
        LambdaQueryWrapper<RiskEvent> queryWrapper = new LambdaQueryWrapper<>();
        if (startDate != null) {
            queryWrapper.ge(RiskEvent::getCreateTime, startDate);
        }
        
        // 统计 HIGH 风险等级
        long highCount = riskEventService.count(new LambdaQueryWrapper<RiskEvent>()
            .apply(startDate != null, "create_time >= {0}", startDate)
            .eq(RiskEvent::getRiskLevel, "HIGH"));
        
        // 统计 MEDIUM 风险等级
        long mediumCount = riskEventService.count(new LambdaQueryWrapper<RiskEvent>()
            .apply(startDate != null, "create_time >= {0}", startDate)
            .eq(RiskEvent::getRiskLevel, "MEDIUM"));
        
        // 统计 LOW 风险等级
        long lowCount = riskEventService.count(new LambdaQueryWrapper<RiskEvent>()
            .apply(startDate != null, "create_time >= {0}", startDate)
            .eq(RiskEvent::getRiskLevel, "LOW"));
        
        statistics.put("high", (int) highCount);
        statistics.put("medium", (int) mediumCount);
        statistics.put("low", (int) lowCount);
        
        return statistics;
    }
    
    @GetMapping("/statistics/trend")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY')")
    public List<Map<String, Object>> getRiskTrendStatistics(@RequestParam(defaultValue = "day") String dimension) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 根据维度决定查询范围和数据格式
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;
        int periodCount; // 要返回的数据点数量
        
        switch (dimension.toLowerCase()) {
            case "week":
                startDate = endDate.minusWeeks(3); // 近4周
                periodCount = 4;
                break;
            case "month":
                startDate = endDate.minusMonths(5); // 近6个月
                periodCount = 6;
                break;
            case "year":
                startDate = endDate.minusYears(2); // 近3年
                periodCount = 3;
                break;
            case "day":
            default:
                startDate = endDate.minusDays(6); // 近7天
                periodCount = 7;
                break;
        }
        
        // 查询对应时间范围的数据
        List<RiskEvent> riskEvents = riskEventService.list(new LambdaQueryWrapper<RiskEvent>()
            .ge(RiskEvent::getCreateTime, startDate.atStartOfDay())
            .le(RiskEvent::getCreateTime, endDate.plusDays(1).atStartOfDay().minusSeconds(1))
        );
        
        // 根据维度生成不同的标签和数据
        Map<String, Integer> periodCounts = new HashMap<>();
        
        for (RiskEvent riskEvent : riskEvents) {
            if (riskEvent.getCreateTime() != null) {
                LocalDate eventDate = riskEvent.getCreateTime().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
                String label = getDateLabelByDimension(eventDate, dimension);
                periodCounts.put(label, periodCounts.getOrDefault(label, 0) + 1);
            }
        }
        
        // 生成完整的数据序列
        List<String> labels = getDateLabels(dimension, startDate, endDate);
        for (String label : labels) {
            Map<String, Object> periodData = new HashMap<>();
            periodData.put("date", label);
            periodData.put("count", periodCounts.getOrDefault(label, 0));
            result.add(periodData);
        }
        
        return result;
    }
    
    @GetMapping("/statistics/type")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY')")
    public List<Map<String, Object>> getRiskTypeStatistics() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 查询所有风险事件，按风险类型分组统计
        Map<String, Integer> typeCounts = new HashMap<>();
        
        // 查询所有风险事件
        List<RiskEvent> riskEvents = riskEventService.list();
        
        // 手动按风险类型分组统计
        for (RiskEvent riskEvent : riskEvents) {
            if (riskEvent.getRiskType() != null && !riskEvent.getRiskType().isEmpty()) {
                String riskType = riskEvent.getRiskType();
                typeCounts.put(riskType, typeCounts.getOrDefault(riskType, 0) + 1);
            }
        }
        
        // 转换为要求的格式
        for (Map.Entry<String, Integer> entry : typeCounts.entrySet()) {
            Map<String, Object> typeData = new HashMap<>();
            typeData.put("type", entry.getKey());
            typeData.put("count", entry.getValue());
            result.add(typeData);
        }
        
        return result;
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY')")
    public RiskEvent getRiskDetail(@PathVariable Long id) {
        RiskEvent riskEvent = riskEventService.getById(id);
        if (riskEvent == null) {
            throw new RuntimeException("风险事件不存在");
        }
        return riskEvent;
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY')")
    public RiskEvent updateRiskStatus(
        @PathVariable Long id,
        @RequestBody Map<String, String> request
    ) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录");
        }
        Long userId = Long.parseLong(currentUserId);
        
        System.out.println("=== 更新风险事件状态 ===");
        System.out.println("风险事件 ID: " + id);
        System.out.println("更新人 ID: " + userId);
        System.out.println("新状态：" + request.get("status"));
        System.out.println("备注：" + request.get("remark"));
        
        RiskEvent riskEvent = riskEventService.getById(id);
        if (riskEvent == null) {
            System.out.println("风险事件不存在");
            System.out.println("===================");
            throw new RuntimeException("风险事件不存在");
        }
        
        String status = request.get("status");
        String remark = request.get("remark");
        
        if (status != null && !status.isEmpty()) {
            riskEvent.setStatus(status);
        }
        
        if (remark != null) {
            riskEvent.setRemark(remark);
        }
        
        riskEvent.setHandlerId(userId);
        riskEvent.setUpdateTime(new Date());
        
        riskEventService.updateById(riskEvent);
        
        System.out.println("风险事件状态更新完成");
        System.out.println("===================");
        
        return riskEvent;
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY')")
    public Map<String, Object> deleteRisk(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        RiskEvent riskEvent = riskEventService.getById(id);
        if (riskEvent == null) {
            result.put("code", 404);
            result.put("message", "风险事件不存在");
            return result;
        }
        
        riskEventService.removeById(id);
        
        result.put("code", 200);
        result.put("message", "删除成功");
        return result;
    }
    
    /**
     * 连续帧动作识别接口
     * POST /api/risk/recognize-motion
     * 
     * @param request 包含 imageUrls 数组（3 个图片 URL）和 location
     * @return 动作识别结果 { actionType, riskLevel, description, frameAnalysis }
     */
    @PostMapping("/recognize-motion")
    public Map<String, Object> recognizeMotion(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("=== 开始连续帧动作识别 ===");
            
            // 1. 获取参数
            @SuppressWarnings("unchecked")
            List<String> imageUrls = (List<String>) request.get("imageUrls");
            String location = (String) request.get("location");
            
            if (imageUrls == null || imageUrls.size() < 3) {
                System.out.println("图片 URL 数量不足 3 张");
                System.out.println("===================");
                result.put("actionType", "未知动作");
                result.put("riskLevel", "LOW");
                result.put("description", "帧数不足，无法进行动作分析");
                return result;
            }
            
            System.out.println("图片数量：" + imageUrls.size());
            System.out.println("位置：" + (location != null ? location : "未指定"));
            
            // 2. 调用 AI 服务进行动作分析
            Map<String, String> motionResult = aiRecognitionService.recognizeMotion(imageUrls, location);
            
            System.out.println("=== 动作识别结果 ===");
            System.out.println("动作类型：" + motionResult.get("actionType"));
            System.out.println("风险等级：" + motionResult.get("riskLevel"));
            System.out.println("描述信息：" + motionResult.get("description"));
            System.out.println("===================");
            
            // 3. 返回结果
            result.put("actionType", motionResult.get("actionType"));
            result.put("riskLevel", motionResult.get("riskLevel"));
            result.put("description", motionResult.get("description"));
            result.put("frameAnalysis", motionResult.get("frameAnalysis"));
            
            return result;
            
        } catch (Exception e) {
            System.err.println("动作识别异常：" + e.getMessage());
            e.printStackTrace();
            
            result.put("actionType", "未知动作");
            result.put("riskLevel", "LOW");
            result.put("description", "动作识别失败：" + e.getMessage());
            
            return result;
        }
    }
    
    @GetMapping("/statistics/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY')")
    public Map<String, Integer> getPendingRiskCount() {
        Map<String, Integer> result = new HashMap<>();
        
        long pendingCount = riskEventService.count(new LambdaQueryWrapper<RiskEvent>()
            .eq(RiskEvent::getStatus, "PENDING"));
        
        result.put("pending", (int) pendingCount);
        return result;
    }
    
    /**
     * 风险类型饼图统计接口
     * 统计所有风险类型
     */
    @GetMapping("/statistics/type-pie")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY')")
    public Map<String, Integer> getRiskTypePieStatistics(@RequestParam(defaultValue = "day") String dimension) {
        Map<String, Integer> statistics = new HashMap<>();
        
        // 根据时间维度计算起始时间
        Date startDate = getStartDateByDimension(dimension);
        
        // 查询所有风险事件
        LambdaQueryWrapper<RiskEvent> queryWrapper = new LambdaQueryWrapper<RiskEvent>()
            .apply(startDate != null, "create_time >= {0}", startDate);
        List<RiskEvent> riskEvents = riskEventService.list(queryWrapper);
        
        // 手动按风险类型分组统计
        for (RiskEvent riskEvent : riskEvents) {
            if (riskEvent.getRiskType() != null && !riskEvent.getRiskType().isEmpty()) {
                String riskType = riskEvent.getRiskType();
                String key = convertToCamelCase(riskType);
                statistics.put(key, statistics.getOrDefault(key, 0) + 1);
            }
        }
        
        if (statistics.isEmpty()) {
            statistics.put("elderFall", 0);
            statistics.put("roadWater", 0);
            statistics.put("facilityDamage", 0);
            statistics.put("ebikeCharging", 0);
        }
        
        return statistics;
    }
    
    /**
     * 将中文风险类型转换为驼峰命名
     */
    private String convertToCamelCase(String text) {
        // 直接移除空格和下划线，转为首字母小写的形式
        if (text == null || text.isEmpty()) {
            return "unknown";
        }
        
        // 如果包含下划线（如 FIRE_RISK），转换为小写
        if (text.contains("_")) {
            return text.toLowerCase();
        }
        
        // 中文类型，返回拼音或英文翻译
        // 这里简单处理，直接返回原文
        return text;
    }
    
    /**
     * 根据时间维度获取起始日期
     * @param dimension day=按天, week=按周, month=按月, year=按年
     * @return 起始日期，null 表示不限制
     */
    private Date getStartDateByDimension(String dimension) {
        if (dimension == null || dimension.isEmpty()) {
            return null;
        }
        
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        
        switch (dimension.toLowerCase()) {
            case "day":
                startDate = today.minusDays(6); // 近7天（含今天）
                break;
            case "week":
                startDate = today.minusWeeks(3); // 近4周（含本周）
                break;
            case "month":
                startDate = today.minusMonths(5); // 近6个月（含本月）
                break;
            case "year":
                startDate = today.minusYears(2); // 近3年（含今年）
                break;
            case "all":
                return null; // 不限制时间
            default:
                startDate = today.minusDays(6);
        }
        
        return java.sql.Timestamp.valueOf(startDate.atStartOfDay());
    }
    
    /**
     * 根据时间维度生成日期标签
     * day: "MM-dd" 格式
     * week: "第X周" 格式
     * month: "YYYY-MM" 格式
     * year: "YYYY年" 格式
     */
    private String getDateLabelByDimension(LocalDate date, String dimension) {
        switch (dimension.toLowerCase()) {
            case "week":
                // 计算是第几周（相对于起始周的偏移）
                return date.getMonthValue() + "月第" + ((date.getDayOfMonth() - 1) / 7 + 1) + "周";
            case "month":
                return date.getYear() + "-" + String.format("%02d", date.getMonthValue());
            case "year":
                return date.getYear() + "年";
            case "day":
            default:
                return date.getMonthValue() + "-" + date.getDayOfMonth();
        }
    }
    
    /**
     * 生成完整的时间标签序列
     */
    private List<String> getDateLabels(String dimension, LocalDate startDate, LocalDate endDate) {
        List<String> labels = new ArrayList<>();
        
        switch (dimension.toLowerCase()) {
            case "week":
                // 生成近4周的标签
                for (int i = 0; i < 4; i++) {
                    LocalDate weekStart = startDate.plusWeeks(i);
                    LocalDate weekEnd = weekStart.plusDays(6);
                    labels.add(weekStart.getMonthValue() + "月第" + ((weekStart.getDayOfMonth() - 1) / 7 + 1) + "周");
                }
                break;
            case "month":
                // 生成近6个月的标签
                for (int i = 0; i < 6; i++) {
                    LocalDate monthDate = startDate.plusMonths(i);
                    labels.add(monthDate.getYear() + "-" + String.format("%02d", monthDate.getMonthValue()));
                }
                break;
            case "year":
                // 生成近3年的标签
                for (int i = 0; i < 3; i++) {
                    LocalDate yearDate = startDate.plusYears(i);
                    labels.add(yearDate.getYear() + "年");
                }
                break;
            case "day":
            default:
                // 生成近7天的标签
                for (int i = 0; i < 7; i++) {
                    LocalDate currentDate = startDate.plusDays(i);
                    labels.add(currentDate.getMonthValue() + "-" + currentDate.getDayOfMonth());
                }
                break;
        }
        
        return labels;
    }
    
    /**
     * 获取紧急风险列表（HIGH 等级且未处理）
     */
    @GetMapping("/urgent")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY')")
    public List<Map<String, Object>> getUrgentRisks() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 查询 HIGH 风险等级且状态为 PENDING 的风险事件
        List<RiskEvent> urgentRisks = riskEventService.list(new LambdaQueryWrapper<RiskEvent>()
            .eq(RiskEvent::getRiskLevel, "HIGH")
            .eq(RiskEvent::getStatus, "PENDING")
            .orderByDesc(RiskEvent::getCreateTime)
            .last("LIMIT 10")); // 最多返回 10 条
        
        for (RiskEvent riskEvent : urgentRisks) {
            Map<String, Object> riskData = new HashMap<>();
            riskData.put("id", riskEvent.getId());
            riskData.put("title", riskEvent.getRiskType());
            riskData.put("location", riskEvent.getLocation() != null ? riskEvent.getLocation() : "未知位置");
            riskData.put("createTime", riskEvent.getCreateTime());
            riskData.put("riskLevel", riskEvent.getRiskLevel());
            result.add(riskData);
        }
        
        return result;
    }
    
    /**
     * 获取最新风险图片列表
     */
    @GetMapping("/images/latest")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY')")
    public List<Map<String, Object>> getLatestRiskImages() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 查询最近上传的风险图片（带图片的）
        List<RiskEvent> riskEvents = riskEventService.list(new LambdaQueryWrapper<RiskEvent>()
            .isNotNull(RiskEvent::getImageUrl)
            .ne(RiskEvent::getImageUrl, "")
            .orderByDesc(RiskEvent::getCreateTime)
            .last("LIMIT 12")); // 最多返回 12 张图片
        
        for (RiskEvent riskEvent : riskEvents) {
            Map<String, Object> imageData = new HashMap<>();
            imageData.put("id", riskEvent.getId());
            imageData.put("url", riskEvent.getImageUrl());
            imageData.put("title", riskEvent.getRiskType() != null ? riskEvent.getRiskType() : "风险事件");
            imageData.put("createTime", riskEvent.getCreateTime());
            imageData.put("riskLevel", riskEvent.getRiskLevel() != null ? riskEvent.getRiskLevel() : "MEDIUM");
            imageData.put("location", riskEvent.getLocation() != null ? riskEvent.getLocation() : "未知位置");
            imageData.put("description", riskEvent.getRemark() != null ? riskEvent.getRemark() : "");
            result.add(imageData);
        }
        
        return result;
    }
    
    @GetMapping("/statistics/today")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY')")
    public Map<String, Integer> getTodayRiskCount() {
        Map<String, Integer> result = new HashMap<>();
        
        LocalDate today = LocalDate.now();
        
        long todayCount = riskEventService.count(new LambdaQueryWrapper<RiskEvent>()
            .ge(RiskEvent::getCreateTime, today.atStartOfDay())
            .lt(RiskEvent::getCreateTime, today.plusDays(1).atStartOfDay()));
        
        result.put("today", (int) todayCount);
        return result;
    }
    
    @PostMapping("/recognize")
    @PreAuthorize("hasAnyRole('RESIDENT', 'ADMIN', 'PROPERTY')")
    public Map<String, String> recognizeRisk(@RequestBody Map<String, Object> request) {
        String imageUrl = (String) request.get("imageUrl");
        String riskTitle = (String) request.get("riskTitle");
        String cameraLocation = (String) request.get("cameraLocation");
        String cameraId = (String) request.get("cameraId");
        
        // 默认值
        if (cameraLocation == null || cameraLocation.isEmpty()) {
            cameraLocation = "监控区域";
        }
        if (cameraId == null || cameraId.isEmpty()) {
            cameraId = "camera_default";
        }
        
        System.out.println("=== AI 风险识别 ===");
        System.out.println("图片 URL: " + imageUrl);
        System.out.println("风险标题：" + (riskTitle != null ? riskTitle : "无"));
        System.out.println("摄像头位置：" + cameraLocation);
        System.out.println("摄像头ID：" + cameraId);
        
        if (imageUrl == null || imageUrl.isEmpty()) {
            System.out.println("图片 URL 为空，无法识别");
            System.out.println("===================");
            throw new RuntimeException("图片 URL 不能为空");
        }
        
        // 使用标题 + 图片 + 摄像头信息联合识别，返回完整信息并自动创建风险事件
        Map<String, String> result = aiRecognitionService.recognizeRiskWithCameraInfo(imageUrl, riskTitle, cameraLocation, cameraId);
        
        System.out.println("GLM 结果：" + result.get("glmResult"));
        System.out.println("百度标签：" + result.get("baiduLabels"));
        System.out.println("风险类型：" + result.get("riskType"));
        System.out.println("风险等级：" + result.get("riskLevel"));
        System.out.println("===================");
        
        return result;
    }
    
    /**
     * 综合统计接口
     * 返回今日数量、待处理数量、类型统计等综合数据
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY')")
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 1. 今日新增数量
        LocalDate today = LocalDate.now();
        long todayCount = riskEventService.count(new LambdaQueryWrapper<RiskEvent>()
            .ge(RiskEvent::getCreateTime, today.atStartOfDay())
            .lt(RiskEvent::getCreateTime, today.plusDays(1).atStartOfDay()));
        statistics.put("todayCount", (int) todayCount);
        
        // 2. 待处理数量
        long pendingCount = riskEventService.count(new LambdaQueryWrapper<RiskEvent>()
            .eq(RiskEvent::getStatus, "PENDING"));
        statistics.put("pendingCount", (int) pendingCount);
        
        // 3. 类型统计
        List<Map<String, Object>> typeStatistics = getRiskTypeStatistics();
        statistics.put("typeStatistics", typeStatistics);
        
        // 4. 风险等级统计
        Map<String, Integer> levelStatistics = getRiskLevelStatistics("day");
        statistics.put("levelStatistics", levelStatistics);
        
        // 5. 总数量
        long totalCount = riskEventService.count(new LambdaQueryWrapper<RiskEvent>());
        statistics.put("totalCount", (int) totalCount);
        
        // 6. 已处理数量
        long doneCount = riskEventService.count(new LambdaQueryWrapper<RiskEvent>()
            .eq(RiskEvent::getStatus, "DONE"));
        statistics.put("doneCount", (int) doneCount);
        
        return statistics;
    }
    
    /**
     * 风险发生位置统计接口
     * 按位置分组统计风险数量
     * 返回格式：[{location: '停车场', count: 3}, {location: '楼道', count: 5}]
     */
    @GetMapping("/statistics/location")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY')")
    public List<Map<String, Object>> getLocationStatistics() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 查询所有风险事件
        List<RiskEvent> riskEvents = riskEventService.list();
        
        // 手动按位置分组统计
        Map<String, Integer> locationCounts = new HashMap<>();
        for (RiskEvent riskEvent : riskEvents) {
            String location = riskEvent.getLocation();
            if (location != null && !location.isEmpty()) {
                locationCounts.put(location, locationCounts.getOrDefault(location, 0) + 1);
            } else {
                locationCounts.put("未知位置", locationCounts.getOrDefault("未知位置", 0) + 1);
            }
        }
        
        // 转换为要求的格式
        for (Map.Entry<String, Integer> entry : locationCounts.entrySet()) {
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("location", entry.getKey());
            locationData.put("count", entry.getValue());
            result.add(locationData);
        }
        
        // 按数量降序排序
        result.sort((a, b) -> (Integer)b.get("count") - (Integer)a.get("count"));
        
        return result;
    }
    
    /**
     * 风险地图数据接口
     * 返回所有风险事件的详细信息，用于地图展示
     */
    @GetMapping("/map")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY')")
    public List<Map<String, Object>> getRiskMapData() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 查询所有风险事件
        List<RiskEvent> riskEvents = riskEventService.list(new LambdaQueryWrapper<RiskEvent>()
            .orderByDesc(RiskEvent::getCreateTime));
        
        // 添加日志：输出实际查询到的风险事件数量
        System.out.println("========================================");
        System.out.println("【风险地图API】查询到风险事件总数: " + (riskEvents != null ? riskEvents.size() : 0));
        
        if (riskEvents == null || riskEvents.isEmpty()) {
            System.out.println("⚠️ 数据库中没有风险事件数据！将返回空列表");
            return result;
        }
        
        System.out.println("✅ 开始处理 " + riskEvents.size() + " 条风险事件...");
        
        // 为每个风险事件生成模拟的坐标位置
        for (int i = 0; i < riskEvents.size(); i++) {
            RiskEvent riskEvent = riskEvents.get(i);
            
            // 输出前5条数据的详细信息（便于调试）
            if (i < 5) {
                System.out.println("  [" + (i+1) + "] ID=" + riskEvent.getId() + 
                    ", 标题=" + riskEvent.getTitle() + 
                    ", 级别=" + riskEvent.getRiskLevel() +
                    ", 位置=" + riskEvent.getLocation());
            }
            
            Map<String, Object> riskData = new HashMap<>();
            riskData.put("id", riskEvent.getId());
            riskData.put("title", riskEvent.getTitle());
            riskData.put("riskType", riskEvent.getRiskType());
            riskData.put("riskLevel", riskEvent.getRiskLevel());
            riskData.put("riskLocation", riskEvent.getLocation());
            riskData.put("imageUrl", riskEvent.getImageUrl());
            riskData.put("glmResult", riskEvent.getGlmResult());
            riskData.put("baiduLabels", riskEvent.getBaiduLabels());
            
            // 根据位置生成模拟坐标（0-100 的相对坐标）
            double[] coords = generateMockCoordinates(riskEvent.getLocation(), riskEvent.getId());
            riskData.put("x", coords[0]);
            riskData.put("y", coords[1]);
            
            result.add(riskData);
        }
        
        System.out.println("【风险地图API】最终返回数据条数: " + result.size());
        System.out.println("========================================");
        
        return result;
    }
    
    /**
     * 根据数据库真实位置信息生成精确坐标
     * 基于小区平面图的实际布局，为每个位置分配固定坐标
     */
    private double[] generateMockCoordinates(String location, Long id) {
        double x = 50.0;
        double y = 50.0;
        
        if (location != null) {
            if (location.contains("儿童") || location.contains("游乐")) {
                x = 78; y = 65;
            } else if (location.contains("健身")) {
                x = 55; y = 70;
            } else if (location.contains("游泳")) {
                x = 35; y = 70;
            } else if (location.contains("花园") || location.contains("中心") || location.contains("绿化")) {
                x = 50; y = 40;
            } else if (location.contains("楼") || location.contains("住宅") || location.contains("单元")) {
                if (location.contains("1号楼") || location.contains("1 号楼") || location.contains("一号楼")) {
                    x = 30; y = 20;
                } else if (location.contains("2号楼") || location.contains("2 号楼") || location.contains("二号楼")) {
                    x = 70; y = 20;
                } else if (location.contains("3号楼") || location.contains("3 号楼") || location.contains("三号楼")) {
                    x = 30; y = 50;
                } else if (location.contains("4号楼") || location.contains("4 号楼") || location.contains("四号楼")) {
                    x = 70; y = 50;
                } else if (location.contains("5号楼") || location.contains("5 号楼") || location.contains("五号楼")) {
                    x = 78; y = 50;
                } else {
                    x = 30; y = 35;
                }
            } else if (location.contains("前门") || location.contains("主入口") || location.contains("大门")) {
                x = 50; y = 95;
            } else if (location.contains("后门") || location.contains("次入口")) {
                x = 50; y = 5;
            } else if (location.contains("停车")) {
                x = 15; y = 35;
            } else if (location.contains("道路") || location.contains("通道") || location.contains("走廊")) {
                x = 50; y = 75;
            } else if (location.contains("地下车库") || location.contains("地下室")) {
                x = 8; y = 60;
            }
        }
        
        return new double[]{x, y};
    }
}