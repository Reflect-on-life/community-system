package com.example.futurecommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.futurecommunity.model.NoticeRead;
import com.example.futurecommunity.model.User;
import com.example.futurecommunity.service.NoticeReadService;
import com.example.futurecommunity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公告阅读率统计控制器
 */
@RestController
@RequestMapping("/api/admin/notice-stats")
public class NoticeStatisticsController {

    @Autowired
    private UserService userService;

    @Autowired
    private NoticeReadService noticeReadService;

    /**
     * 获取公告阅读统计
     * @param noticeId 公告ID
     * @return 统计结果
     */
    @PreAuthorize("hasRole('PROPERTY') or hasRole('ADMIN')")
    @GetMapping("/{noticeId}")
    public Map<String, Object> getNoticeStats(@PathVariable Long noticeId) {
        // 构建响应结果
        Map<String, Object> result = new HashMap<>();
        result.put("noticeId", noticeId);

        // 1. 查询总用户数（status=1）
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(User::getStatus, 1);
        long totalUsers = userService.count(userQueryWrapper);
        result.put("totalUsers", totalUsers);

        // 2. 查询已阅读数
        LambdaQueryWrapper<NoticeRead> readQueryWrapper = new LambdaQueryWrapper<>();
        readQueryWrapper.eq(NoticeRead::getNoticeId, noticeId);
        long readCount = noticeReadService.count(readQueryWrapper);
        result.put("readCount", readCount);

        // 3. 计算未阅读数
        long unreadCount = totalUsers - readCount;
        result.put("unreadCount", unreadCount);

        // 4. 计算阅读率
        double readRate = totalUsers > 0 ? (double) readCount / totalUsers : 0;
        result.put("readRate", readRate);

        // 5. 统计不同角色的阅读数量
        Map<String, Integer> roleStats = new HashMap<>();

        // 先获取所有已阅读的用户ID
        List<NoticeRead> noticeReads = noticeReadService.list(readQueryWrapper);
        for (NoticeRead noticeRead : noticeReads) {
            Long userId = noticeRead.getUserId();
            User user = userService.getById(userId);
            if (user != null) {
                String userType = user.getType();
                roleStats.put(userType, roleStats.getOrDefault(userType, 0) + 1);
            }
        }

        result.put("roleStats", roleStats);

        return result;
    }
}