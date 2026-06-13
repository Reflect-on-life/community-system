package com.example.futurecommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.futurecommunity.enums.RepairStatus;
import com.example.futurecommunity.mapper.TechnicianSkillMapper;
import com.example.futurecommunity.mapper.TechnicianSkillRelationMapper;
import com.example.futurecommunity.model.Notice;
import com.example.futurecommunity.model.Repair;
import com.example.futurecommunity.model.ResidentHouse;
import com.example.futurecommunity.model.TechnicianSkill;
import com.example.futurecommunity.model.TechnicianSkillRelation;
import com.example.futurecommunity.model.User;
import com.example.futurecommunity.service.NoticeService;
import com.example.futurecommunity.service.RepairService;
import com.example.futurecommunity.service.ResidentHouseService;
import com.example.futurecommunity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/repair")
public class RepairController {

    @Autowired
    private RepairService repairService;

    @Autowired
    private ResidentHouseService residentHouseService;

    @Autowired
    private UserService userService;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private TechnicianSkillMapper technicianSkillMapper;

    @Autowired
    private TechnicianSkillRelationMapper technicianSkillRelationMapper;

    /**
     * 提交报修申请
     * 居民用户可以提交报修申请
     */
    @PreAuthorize("hasRole('RESIDENT')")
    @PostMapping
    public Repair createRepair(@RequestBody Repair repair) {
        // 从 SecurityContextHolder 中获取当前登录用户的 userId
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(currentUserId);
        
        // 检查用户是否为认证居民
        List<ResidentHouse> residentHouses = residentHouseService.lambdaQuery()
                .eq(ResidentHouse::getUserId, userId)
                .eq(ResidentHouse::getCertified, 1)
                .list();
        
        if (residentHouses.isEmpty()) {
            throw new RuntimeException("只有认证居民可以提交报修申请");
        }
        
        // 设置 userId 为当前登录用户的 ID，防止用户伪造其他用户的 ID
        repair.setUserId(userId);
        
        // 自动关联当前登录用户的 house_id
        if (repair.getHouseId() == null && !residentHouses.isEmpty()) {
            repair.setHouseId(residentHouses.get(0).getHouseId());
        }
        
        // 设置初始状态为 pending
        repair.setStatus("pending");
        repair.setCreatedAt(new Date());
        
        repairService.save(repair);
        return repair;
    }

    /**
     * 获取用户的报修列表
     * 用于前端显示用户的报修记录
     */
    @PreAuthorize("hasAnyRole('RESIDENT', 'TECHNICIAN', 'PROPERTY_TECHNICIAN', 'PROPERTY', 'PROPERTY_STAFF', 'ADMIN')")
    @GetMapping
    public List<Map<String, Object>> getUserRepairs() {
        // 从 SecurityContextHolder 中获取当前登录用户的 userId
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // 安全地获取用户权限
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isTechnician = false;
        boolean isProperty = false;
        
        if (authentication != null && authentication.getAuthorities() != null) {
            for (var authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                if ("ROLE_TECHNICIAN".equals(role) || "ROLE_PROPERTY_TECHNICIAN".equals(role)) {
                    isTechnician = true;
                }
                if ("ROLE_PROPERTY".equals(role) || "ROLE_PROPERTY_STAFF".equals(role) || "ROLE_ADMIN".equals(role)) {
                    isProperty = true;
                }
            }
        }
        
        // 根据用户类型返回不同的数据
        if (isProperty) {
            // 物业、物业工作人员和管理员可以看到所有报修
            return repairService.list().stream().map(this::convertToMap).toList();
        } else if (isTechnician) {
            // 维修师傅只能看到分配给自己的工单
            Long technicianId = Long.parseLong(currentUserId);
            return repairService.lambdaQuery()
                    .eq(Repair::getAssignedTo, technicianId)
                    .list()
                    .stream()
                    .map(this::convertToMap)
                    .toList();
        } else {
            // 居民只能看到自己的报修
            Long userId = Long.parseLong(currentUserId);
            return repairService.lambdaQuery()
                    .eq(Repair::getUserId, userId)
                    .list()
                    .stream()
                    .map(this::convertToMap)
                    .toList();
        }
    }

    /**
     * 根据 ID 获取单个报修记录
     * 居民只能查看自己的报修，维修师傅/物业可以查看分配的报修
     */
    @PreAuthorize("hasAnyRole('RESIDENT', 'TECHNICIAN', 'PROPERTY_TECHNICIAN', 'PROPERTY', 'PROPERTY_STAFF', 'ADMIN')")
    @GetMapping("/{id}")
    public Map<String, Object> getRepairById(@PathVariable Long id) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isProperty = false;
        boolean isTechnician = false;
        
        if (authentication != null && authentication.getAuthorities() != null) {
            for (var authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                if ("ROLE_TECHNICIAN".equals(role) || "ROLE_PROPERTY_TECHNICIAN".equals(role)) {
                    isTechnician = true;
                }
                if ("ROLE_PROPERTY".equals(role) || "ROLE_PROPERTY_STAFF".equals(role) || "ROLE_ADMIN".equals(role)) {
                    isProperty = true;
                }
            }
        }
        
        Repair repair = repairService.getById(id);
        if (repair == null) {
            throw new RuntimeException("报修记录不存在");
        }
        
        if (isProperty) {
            return convertToMap(repair);
        } else if (isTechnician) {
            Long technicianId = Long.parseLong(currentUserId);
            if (!technicianId.equals(repair.getAssignedTo())) {
                throw new RuntimeException("无权查看此报修记录");
            }
            return convertToMap(repair);
        } else {
            Long userId = Long.parseLong(currentUserId);
            if (!repair.getUserId().equals(userId)) {
                throw new RuntimeException("无权查看此报修记录");
            }
            return convertToMap(repair);
        }
    }

    /**
     * 获取所有报修列表
     * 物业用户可以查看所有报修记录
     */
    @PreAuthorize("hasAnyRole('PROPERTY', 'PROPERTY_STAFF', 'ADMIN')")
    @GetMapping("/all")
    public List<Map<String, Object>> getAllRepairs() {
        return repairService.list().stream().map(this::convertToMap).toList();
    }

    /**
     * 根据状态获取报修列表
     * 物业用户可以查看指定状态的报修记录
     */
    @PreAuthorize("hasAnyRole('PROPERTY', 'PROPERTY_STAFF', 'ADMIN')")
    @GetMapping("/status/{status}")
    public List<Map<String, Object>> getRepairsByStatus(@PathVariable String status) {
        return repairService.lambdaQuery()
                .eq(Repair::getStatus, status)
                .list()
                .stream()
                .map(this::convertToMap)
                .toList();
    }

    /**
     * 分配维修师傅（派单）
     * 物业用户分配维修师傅并生成"报修分配"公告
     * 状态变更：PENDING -> ASSIGNED
     */
    @PreAuthorize("hasAnyRole('PROPERTY', 'ADMIN')")
    @PostMapping("/dispatch/{id}")
    public Map<String, Object> dispatchRepair(@PathVariable Long id, @RequestBody Map<String, Long> request) {
        Repair repair = repairService.getById(id);
        if (repair == null) {
            throw new RuntimeException("报修记录不存在");
        }
        
        // 检查状态是否为 pending
        if (!"pending".equals(repair.getStatus())) {
            throw new RuntimeException("只有待派单的工单可以分配");
        }
        
        Long assignedTo = request.get("assignedTo");
        if (assignedTo == null) {
            throw new RuntimeException("请指定维修师傅");
        }
        
        // 更新报修状态为 ASSIGNED（已派单/待接单）
        repair.setStatus(RepairStatus.ASSIGNED.name().toLowerCase());
        repair.setAssignedTo(assignedTo);
        repair.setAssignedAt(new Date());
        repairService.updateById(repair);
        
        // 生成"报修分配"公告
        User assignedUser = userService.getById(assignedTo);
        String content = String.format("您的报修申请已被分配给维修师傅：%s，请等待接单", 
                assignedUser != null ? assignedUser.getName() : "未知");
        
        Notice notice = new Notice();
        notice.setTitle("报修派单通知");
        notice.setContent(content);
        notice.setTargetType("all");
        notice.setRepairId(id);
        notice.setCreatedBy(Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName()));
        notice.setCreatedAt(new Date());
        noticeService.save(notice);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "派单成功，等待维修师傅接单");
        result.put("data", convertToMap(repair));
        return result;
    }

    /**
     * 维修师傅接单
     * 维修师傅确认接单并开始处理问题
     * 状态变更：ASSIGNED -> PROCESSING
     */
    @PreAuthorize("hasAnyRole('PROPERTY_STAFF', 'ADMIN')")
    @PostMapping("/accept/{id}")
    public Map<String, Object> acceptRepair(@PathVariable Long id) {
        // 获取当前用户 ID
        Long currentUserId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        
        Repair repair = repairService.getById(id);
        if (repair == null) {
            throw new RuntimeException("报修记录不存在");
        }
        
        // 检查状态是否为 assigned
        if (!"assigned".equals(repair.getStatus())) {
            throw new RuntimeException("只有已派单的工单可以接单");
        }
        
        // 检查当前用户是否为该工单的分配师傅
        if (!currentUserId.equals(repair.getAssignedTo())) {
            throw new RuntimeException("只能接单分配给自己的工单");
        }
        
        // 更新报修状态为 PROCESSING（处理中）
        repair.setStatus(RepairStatus.PROCESSING.name().toLowerCase());
        repair.setStartedAt(new Date());
        repairService.updateById(repair);
        
        // 生成"维修师傅已接单"公告
        String content = "维修师傅已确认接单，将尽快为您处理";
        
        Notice notice = new Notice();
        notice.setTitle("维修接单通知");
        notice.setContent(content);
        notice.setTargetType("all");
        notice.setRepairId(id);
        notice.setCreatedBy(currentUserId);
        notice.setCreatedAt(new Date());
        noticeService.save(notice);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "接单成功");
        result.put("data", convertToMap(repair));
        return result;
    }

    /**
     * 完成维修
     * 维修师傅完成任务并生成"维修完成"公告（竣工通知）
     * 状态变更：PROCESSING -> COMPLETED
     */
    @PreAuthorize("hasAnyRole('PROPERTY_STAFF', 'ADMIN')")
    @PostMapping("/complete/{id}")
    public Map<String, Object> completeRepair(@PathVariable Long id) {
        // 获取当前用户 ID
        Long currentUserId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        
        Repair repair = repairService.getById(id);
        if (repair == null) {
            throw new RuntimeException("报修记录不存在");
        }
        
        // 检查状态是否为 processing
        if (!"processing".equals(repair.getStatus())) {
            throw new RuntimeException("只有处理中的工单可以完成");
        }
        
        // 检查当前用户是否为该工单的分配师傅
        if (!currentUserId.equals(repair.getAssignedTo())) {
            throw new RuntimeException("只能完成分配给自己的工单");
        }
        
        // 更新报修状态为 COMPLETED（已完成）
        repair.setStatus(RepairStatus.COMPLETED.name().toLowerCase());
        repair.setCompletedAt(new Date());
        repairService.updateById(repair);
        
        // 生成"维修完成"公告（竣工通知）
        String content = "您的报修申请已完成，维修师傅已提交竣工报告。请对服务进行评价";
        
        Notice notice = new Notice();
        notice.setTitle("维修竣工通知");
        notice.setContent(content);
        notice.setTargetType("all");
        notice.setRepairId(id);
        notice.setCreatedBy(currentUserId);
        notice.setCreatedAt(new Date());
        noticeService.save(notice);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "维修已完成，请评价");
        result.put("data", convertToMap(repair));
        return result;
    }

    /**
     * 居民评价服务
     * 居民对维修服务进行评价
     */
    @PreAuthorize("hasRole('RESIDENT')")
    @PostMapping("/rate/{id}")
    public Map<String, Object> rateRepair(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Repair repair = repairService.getById(id);
        if (repair == null) {
            throw new RuntimeException("报修记录不存在");
        }
        
        // 检查状态是否为 completed
        if (!"completed".equals(repair.getStatus())) {
            throw new RuntimeException("只有已完成的工单可以评价");
        }
        
        // 检查是否为报修的提交者
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(currentUserId);
        if (!repair.getUserId().equals(userId)) {
            throw new RuntimeException("只有报修的提交者可以评价");
        }
        
        // 更新评价信息
        Integer rating = ((Number) request.get("rating")).intValue();
        String comment = (String) request.get("comment");
        
        repair.setRating(rating);
        repair.setComment(comment);
        repairService.updateById(repair);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "评价成功");
        result.put("data", convertToMap(repair));
        return result;
    }

    /**
     * 更新报修状态
     * 物业用户可以更新报修状态
     */
    @PreAuthorize("hasAnyRole('PROPERTY', 'PROPERTY_STAFF', 'ADMIN')")
    @PutMapping("/status/{id}")
    public Map<String, Object> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Repair repair = repairService.getById(id);
        if (repair == null) {
            throw new RuntimeException("报修记录不存在");
        }
        
        // 状态机流转逻辑
        if ("processing".equals(status)) {
            // 从 pending 切换到 processing
            if (!"pending".equals(repair.getStatus())) {
                throw new RuntimeException("只有待处理的工单可以开始处理");
            }
        } else if ("completed".equals(status)) {
            // 从 processing 切换到 completed
            if (!"processing".equals(repair.getStatus())) {
                throw new RuntimeException("只有处理中的工单可以完成");
            }
            repair.setCompletedAt(new Date());
        } else if ("cancelled".equals(status)) {
            // 从 pending 切换到 cancelled
            if (!"pending".equals(repair.getStatus())) {
                throw new RuntimeException("只有待处理的工单可以取消");
            }
        }
        
        repair.setStatus(status);
        repairService.updateById(repair);
        return convertToMap(repair);
    }

    /**
     * 居民取消报修
     * 居民用户可以在物业派单前取消报修
     * 状态变更：PENDING -> CANCELLED
     */
    @PreAuthorize("hasRole('RESIDENT')")
    @PutMapping("/cancel/{id}")
    public Map<String, Object> cancelRepair(@PathVariable Long id) {
        Repair repair = repairService.getById(id);
        if (repair == null) {
            throw new RuntimeException("报修记录不存在");
        }
        
        // 从 SecurityContextHolder 中获取当前登录用户的 userId
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(currentUserId);
        
        // 检查是否为报修的提交者
        if (!repair.getUserId().equals(userId)) {
            throw new RuntimeException("只有报修的提交者可以取消报修");
        }
        
        // 检查状态是否为 pending
        if (!"pending".equals(repair.getStatus())) {
            throw new RuntimeException("只有待派单的工单可以取消");
        }
        
        repair.setStatus(RepairStatus.CANCELLED.name().toLowerCase());
        repairService.updateById(repair);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "取消成功");
        result.put("data", convertToMap(repair));
        return result;
    }
    
    /**
     * 物业取消工单
     * 物业用户可以取消工单
     * 状态变更：PENDING/ASSIGNED -> CANCELLED
     */
    @PreAuthorize("hasAnyRole('PROPERTY', 'ADMIN')")
    @PutMapping("/cancel-by-property/{id}")
    public Map<String, Object> cancelByProperty(@PathVariable Long id, @RequestBody(required = false) Map<String, String> request) {
        Repair repair = repairService.getById(id);
        if (repair == null) {
            throw new RuntimeException("报修记录不存在");
        }
        
        // 检查状态
        String currentStatus = repair.getStatus();
        if (!"pending".equals(currentStatus) && !"assigned".equals(currentStatus)) {
            throw new RuntimeException("只有待派单或已派单的工单可以取消");
        }
        
        String reason = request != null ? request.get("reason") : null;
        
        repair.setStatus(RepairStatus.CANCELLED.name().toLowerCase());
        repair.setComment(reason); // 使用 comment 字段暂存取消原因
        repairService.updateById(repair);
        
        // 生成取消通知
        String content = reason != null ? 
                String.format("您的报修申请已被取消，原因：%s", reason) : 
                "您的报修申请已被取消";
        
        Notice notice = new Notice();
        notice.setTitle("报修取消通知");
        notice.setContent(content);
        notice.setTargetType("all");
        notice.setRepairId(id);
        notice.setCreatedBy(Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName()));
        notice.setCreatedAt(new Date());
        noticeService.save(notice);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "取消成功");
        result.put("data", convertToMap(repair));
        return result;
    }

    /**
     * 将 Repair 转换为包含维修师傅信息的 Map
     */
    private Map<String, Object> convertToMap(Repair repair) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", repair.getId());
        map.put("userId", repair.getUserId());
        map.put("houseId", repair.getHouseId());
        map.put("description", repair.getDescription());
        map.put("status", repair.getStatus());
        // 添加状态描述
        RepairStatus status = RepairStatus.fromString(repair.getStatus());
        if (status != null) {
            map.put("statusDescription", status.getDescription());
        }
        map.put("createdAt", repair.getCreatedAt());
        map.put("completedAt", repair.getCompletedAt());
        map.put("assignedTo", repair.getAssignedTo());
        map.put("assignedAt", repair.getAssignedAt());
        map.put("startedAt", repair.getStartedAt());
        map.put("rating", repair.getRating());
        map.put("comment", repair.getComment());
        
        // 添加维修师傅姓名和技能
        if (repair.getAssignedTo() != null) {
            User assignedUser = userService.getById(repair.getAssignedTo());
            if (assignedUser != null) {
                map.put("assignedUserName", assignedUser.getName());
                
                // 查询维修师傅的技能
                LambdaQueryWrapper<TechnicianSkillRelation> skillQueryWrapper = new LambdaQueryWrapper<>();
                skillQueryWrapper.eq(TechnicianSkillRelation::getUserId, assignedUser.getId());
                List<TechnicianSkillRelation> skillRelations = technicianSkillRelationMapper.selectList(skillQueryWrapper);
                
                if (skillRelations.isEmpty()) {
                    map.put("assignedUserSkills", Collections.emptyList());
                } else {
                    List<Long> skillIds = skillRelations.stream()
                            .map(TechnicianSkillRelation::getSkillId)
                            .collect(Collectors.toList());
                    
                    List<TechnicianSkill> skills = technicianSkillMapper.selectBatchIds(skillIds);
                    map.put("assignedUserSkills", skills);
                }
            } else {
                map.put("assignedUserName", "未知");
                map.put("assignedUserSkills", Collections.emptyList());
            }
        } else {
            map.put("assignedUserName", null);
            map.put("assignedUserSkills", Collections.emptyList());
        }
        
        return map;
    }
}
