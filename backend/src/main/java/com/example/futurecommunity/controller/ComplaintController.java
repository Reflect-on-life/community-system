package com.example.futurecommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.futurecommunity.model.Complaint;
import com.example.futurecommunity.model.ComplaintReply;
import com.example.futurecommunity.service.ComplaintReplyService;
import com.example.futurecommunity.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 投诉控制器
 */
@RestController
@RequestMapping("/api")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private ComplaintReplyService complaintReplyService;

    /**
     * 提交投诉
     * 居民接口
     */
    @PreAuthorize("hasRole('RESIDENT')")
    @PostMapping("/complaints")
    public Complaint submitComplaint(@RequestBody Complaint complaint) {
        // 从 SecurityContextHolder 获取当前用户ID
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(currentUserId);
        
        // 设置用户ID和默认状态
        complaint.setUserId(userId);
        complaint.setStatus("pending");
        complaint.setCreatedAt(new Date());
        
        // 保存投诉
        complaintService.save(complaint);
        
        return complaint;
    }

    /**
     * 查询我的投诉
     * 居民接口
     */
    @PreAuthorize("hasRole('RESIDENT')")
    @GetMapping("/complaints/my")
    public List<Complaint> getMyComplaints() {
        // 从 SecurityContextHolder 获取当前用户ID
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(currentUserId);
        
        // 构建查询条件
        LambdaQueryWrapper<Complaint> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Complaint::getUserId, userId);
        queryWrapper.orderByDesc(Complaint::getCreatedAt);
        
        // 执行查询
        return complaintService.list(queryWrapper);
    }

    /**
     * 查询所有投诉（分页）
     * 物业/管理员接口
     */
    @PreAuthorize("hasAnyRole('PROPERTY', 'ADMIN')")
    @GetMapping("/admin/complaints/page")
    public Page<Complaint> getComplaintsByPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        
        // 创建分页对象
        Page<Complaint> complaintPage = new Page<>(page, size);
        
        // 构建查询条件
        LambdaQueryWrapper<Complaint> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加状态筛选条件
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(Complaint::getStatus, status);
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Complaint::getCreatedAt);
        
        // 执行分页查询
        complaintService.page(complaintPage, queryWrapper);
        
        return complaintPage;
    }

    /**
     * 回复投诉
     * 物业/管理员接口
     */
    @PreAuthorize("hasAnyRole('PROPERTY', 'ADMIN')")
    @Transactional
    @PostMapping("/admin/complaints/{id}/reply")
    public void replyComplaint(@PathVariable Long id, @RequestBody ReplyRequest request) {
        // 从 SecurityContextHolder 获取当前用户ID
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        Long replyUserId = Long.parseLong(currentUserId);
        
        // 查询投诉是否存在
        Complaint complaint = complaintService.getById(id);
        if (complaint == null) {
            throw new RuntimeException("投诉不存在");
        }
        
        // 创建回复记录
        ComplaintReply reply = new ComplaintReply();
        reply.setComplaintId(id);
        reply.setReplyUserId(replyUserId);
        reply.setContent(request.getContent());
        reply.setCreatedAt(new Date());
        
        // 保存回复
        complaintReplyService.save(reply);
        
        // 更新投诉状态
        complaint.setStatus("replied");
        complaintService.updateById(complaint);
    }

    /**
     * 关闭投诉
     * 物业/管理员接口
     */
    @PreAuthorize("hasAnyRole('PROPERTY', 'ADMIN')")
    @PutMapping("/admin/complaints/{id}/close")
    public void closeComplaint(@PathVariable Long id) {
        // 查询投诉是否存在
        Complaint complaint = complaintService.getById(id);
        if (complaint == null) {
            throw new RuntimeException("投诉不存在");
        }
        
        // 更新投诉状态
        complaint.setStatus("closed");
        complaintService.updateById(complaint);
    }

    /**
     * 获取投诉回复
     * 居民接口
     */
    @PreAuthorize("hasRole('RESIDENT')")
    @GetMapping("/complaints/{id}/replies")
    public List<ComplaintReply> getComplaintReplies(@PathVariable Long id) {
        // 构建查询条件
        LambdaQueryWrapper<ComplaintReply> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ComplaintReply::getComplaintId, id);
        queryWrapper.orderByDesc(ComplaintReply::getCreatedAt);
        
        // 执行查询
        return complaintReplyService.list(queryWrapper);
    }

    /**
     * 回复请求体
     */
    public static class ReplyRequest {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}