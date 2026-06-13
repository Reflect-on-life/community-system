package com.example.futurecommunity.controller;

import com.example.futurecommunity.model.Complaint;
import com.example.futurecommunity.model.Repair;
import com.example.futurecommunity.model.ResidentHouse;
import com.example.futurecommunity.model.RiskEvent;
import com.example.futurecommunity.service.ComplaintService;
import com.example.futurecommunity.service.RepairService;
import com.example.futurecommunity.service.ResidentHouseService;
import com.example.futurecommunity.service.RiskEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 物业待办统计控制器
 */
@RestController
@RequestMapping("/api/property")
public class PropertyDashboardController {

    @Autowired
    private ResidentHouseService residentHouseService;

    @Autowired
    private RepairService repairService;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private RiskEventService riskEventService;

    /**
     * 获取物业待办事项统计
     */
    @PreAuthorize("hasRole('PROPERTY')")
    @GetMapping("/todo-stats")
    public ResponseEntity<Map<String, Object>> getTodoStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. 统计待审核认证数量（certified=0 的申请）
        long pendingCertifications = residentHouseService.lambdaQuery()
                .eq(ResidentHouse::getCertified, 0)
                .count();
        stats.put("pendingCertifications", pendingCertifications);

        // 2. 统计待处理报修数量（status='pending' 的工单）
        long pendingRepairs = repairService.lambdaQuery()
                .eq(Repair::getStatus, "pending")
                .count();
        stats.put("pendingRepairs", pendingRepairs);

        // 3. 统计待处理投诉数量（status='pending' 的投诉）
        long pendingComplaints = complaintService.lambdaQuery()
                .eq(Complaint::getStatus, "pending")
                .count();
        stats.put("pendingComplaints", pendingComplaints);

        // 4. 统计待处理风险数量（status='pending' 的风险事件）
        long pendingRisks = riskEventService.lambdaQuery()
                .eq(RiskEvent::getStatus, "pending")
                .count();
        stats.put("pendingRisks", pendingRisks);

        return ResponseEntity.ok(stats);
    }
}
