package com.example.futurecommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.futurecommunity.model.AlarmMessage;
import com.example.futurecommunity.model.AlarmRecord;
import com.example.futurecommunity.model.BehaviorDetectionResult;
import com.example.futurecommunity.model.PageResult;
import com.example.futurecommunity.model.RiskEvent;
import com.example.futurecommunity.model.dto.AlarmListRequest;
import com.example.futurecommunity.model.dto.DetectRequest;
import com.example.futurecommunity.service.AlarmRecordService;
import com.example.futurecommunity.service.BehaviorDetectionService;
import com.example.futurecommunity.service.FileService;
import com.example.futurecommunity.service.RiskEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 告警控制器
 * 处理行为识别和告警查询请求
 */
@Slf4j
@RestController
@RequestMapping("/api/alarm")
public class AlarmController {

    @Autowired
    private BehaviorDetectionService behaviorDetectionService;

    @Autowired
    private AlarmRecordService alarmRecordService;

    @Autowired
    private FileService fileService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RiskEventService riskEventService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 行为识别接口
     * 接收 Base64 图片，调用行为识别服务，识别到告警则保存并推送
     * 
     * @param request 识别请求（包含 Base64 图片和位置信息）
     * @return 识别结果
     */
    @PostMapping("/detect")
    public ResponseEntity<BehaviorDetectionResult> detectBehavior(@RequestBody DetectRequest request) {
        try {
            log.info("收到行为识别请求，位置：{}", request.getLocation());

            // 1. 调用行为识别服务
            BehaviorDetectionResult result = behaviorDetectionService.detectBehavior(request.getImage());
            
            // 添加详细日志，查看 AI 返回的结果
            log.info("AI 识别结果 - 是否告警：{}, 类型：{}, 置信度：{}, 人数：{}", 
                result.isAlarm(), 
                result.getAlarmType(), 
                result.getConfidence(),
                result.getPersonNum());

            // 2. 如果识别到告警，保存并推送
            if (result.isAlarm()) {
                log.info("检测到告警：类型={}, 置信度={}", result.getAlarmType(), result.getConfidence());

                // 2.1 保存截图（可选）
                String imagePath = null;
                try {
                    imagePath = fileService.saveAlarmImage(request.getImage());
                    result.setImagePath(imagePath);
                } catch (Exception e) {
                    log.error("保存截图失败", e);
                }

                // 2.2 创建告警记录
                AlarmRecord alarmRecord = new AlarmRecord();
                alarmRecord.setAlarmType(result.getAlarmType());
                alarmRecord.setConfidence(result.getConfidence());
                alarmRecord.setImagePath(imagePath);
                alarmRecord.setLocation(request.getLocation());
                alarmRecord.setAlarmTime(new Date());
                alarmRecord.setIsRead(0); // 未读
                alarmRecord.setCreatedAt(new Date());

                alarmRecordService.save(alarmRecord);
                result.setRiskEventId(alarmRecord.getId());

                // 2.3 通过 WebSocket 推送告警
                try {
                    AlarmMessage alarmMessage = AlarmMessage.of(
                        result.getAlarmType(),
                        result.getConfidence(),
                        LocalDateTime.now().format(DATE_TIME_FORMATTER),
                        imagePath,
                        request.getLocation()
                    );

                    messagingTemplate.convertAndSend("/topic/alarm", alarmMessage);
                    log.info("告警推送成功：{}", alarmMessage.getType());
                } catch (Exception e) {
                    log.error("告警推送失败", e);
                }
            } else {
                log.debug("场景正常，无需告警");
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("行为识别失败", e);
            return ResponseEntity.ok(BehaviorDetectionResult.error("识别失败：" + e.getMessage()));
        }
    }

    /**
     * 分页查询告警列表
     * 从 risk_event 表查询 AI 识别的风险事件（摄像头监控告警）
     * 支持按风险类型和时间范围筛选
     */
    @GetMapping("/list")
    public ResponseEntity<PageResult<RiskEvent>> getAlarmList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer size,
            @RequestParam(required = false) String alarmType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime
    ) {
        try {
            log.info("查询告警列表（risk_event），page={}, size={}, alarmType={}, startTime={}, endTime={}", 
                page, size, alarmType, startTime, endTime);

            // 1. 构建查询条件
            LambdaQueryWrapper<RiskEvent> queryWrapper = new LambdaQueryWrapper<>();

            // 按风险类型筛选
            if (alarmType != null && !alarmType.isEmpty()) {
                queryWrapper.eq(RiskEvent::getRiskType, alarmType);
            }

            // 按时间范围筛选
            if (startTime != null && !startTime.isEmpty()) {
                LocalDateTime startDateTime = LocalDateTime.parse(startTime, DATE_TIME_FORMATTER);
                queryWrapper.ge(RiskEvent::getCreateTime, java.sql.Timestamp.valueOf(startDateTime));
            }

            if (endTime != null && !endTime.isEmpty()) {
                LocalDateTime endDateTime = LocalDateTime.parse(endTime, DATE_TIME_FORMATTER);
                // 包含结束时间当天
                endDateTime = endDateTime.with(LocalTime.MAX);
                queryWrapper.le(RiskEvent::getCreateTime, java.sql.Timestamp.valueOf(endDateTime));
            }

            // 按创建时间倒序
            queryWrapper.orderByDesc(RiskEvent::getCreateTime);

            // 2. 分页查询
            Page<RiskEvent> alarmPage = new Page<>(page, size);
            Page<RiskEvent> resultPage = riskEventService.page(alarmPage, queryWrapper);

            // 3. 构建分页结果
            PageResult<RiskEvent> pageResult = PageResult.of(
                resultPage.getTotal(),
                resultPage.getRecords()
            );

            log.info("查询成功，总数={}, 当前页数据={}", pageResult.getTotal(), pageResult.getRecords().size());

            return ResponseEntity.ok(pageResult);

        } catch (Exception e) {
            log.error("查询告警列表失败", e);
            return ResponseEntity.ok(PageResult.of(0L, List.of()));
        }
    }

    /**
     * 标记为已读
     * 从 risk_event 表更新已读状态
     */
    @PostMapping("/read/{id}")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            RiskEvent riskEvent = riskEventService.getById(id);
            if (riskEvent == null) {
                return ResponseEntity.badRequest().body("风险事件不存在");
            }

            riskEvent.setIsRead(1);
            riskEventService.updateById(riskEvent);

            return ResponseEntity.ok("标记成功");
        } catch (Exception e) {
            log.error("标记已读失败，id={}", id, e);
            return ResponseEntity.badRequest().body("操作失败：" + e.getMessage());
        }
    }

    /**
     * 批量标记为已读
     * 从 risk_event 表更新已读状态
     */
    @PostMapping("/read/batch")
    public ResponseEntity<?> markBatchAsRead(@RequestBody List<Long> ids) {
        try {
            for (Long id : ids) {
                RiskEvent riskEvent = riskEventService.getById(id);
                if (riskEvent != null) {
                    riskEvent.setIsRead(1);
                    riskEventService.updateById(riskEvent);
                }
            }

            return ResponseEntity.ok("批量标记成功");
        } catch (Exception e) {
            log.error("批量标记失败", e);
            return ResponseEntity.badRequest().body("操作失败");
        }
    }

    /**
     * 删除告警记录（风险事件）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAlarm(@PathVariable Long id) {
        try {
            RiskEvent riskEvent = riskEventService.getById(id);
            if (riskEvent == null) {
                return ResponseEntity.badRequest().body("风险事件不存在");
            }

            riskEventService.removeById(id);
            log.info("删除风险事件成功，ID: {}", id);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            log.error("删除告警失败，id={}", id, e);
            return ResponseEntity.badRequest().body("删除失败：" + e.getMessage());
        }
    }
}
