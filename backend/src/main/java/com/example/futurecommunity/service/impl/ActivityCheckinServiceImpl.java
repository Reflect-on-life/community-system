package com.example.futurecommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.futurecommunity.mapper.ActivityCheckinMapper;
import com.example.futurecommunity.model.Activity;
import com.example.futurecommunity.model.ActivityCheckin;
import com.example.futurecommunity.model.ActivitySignup;
import com.example.futurecommunity.service.ActivityCheckinService;
import com.example.futurecommunity.service.ActivityService;
import com.example.futurecommunity.service.ActivitySignupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动签到服务实现类
 */
@Service
public class ActivityCheckinServiceImpl extends ServiceImpl<ActivityCheckinMapper, ActivityCheckin> implements ActivityCheckinService {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivitySignupService activitySignupService;

    @Override
    @Transactional
    public boolean checkin(Long activityId, Long userId) {
        // 检查活动是否存在
        Activity activity = activityService.getById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        // 检查用户是否已报名该活动
        if (!activitySignupService.isSignedUp(activityId, userId)) {
            throw new RuntimeException("您未报名该活动，无法签到");
        }

        // 检查用户是否已签到
        if (isCheckedIn(activityId, userId)) {
            throw new RuntimeException("您已签到，无需重复签到");
        }

        // 创建签到记录
        ActivityCheckin checkin = new ActivityCheckin();
        checkin.setActivityId(activityId);
        checkin.setUserId(userId);
        checkin.setStatus(1); // 1-已签到
        checkin.setCheckinTime(LocalDateTime.now());
        checkin.setCreatedAt(LocalDateTime.now());

        return save(checkin);
    }

    @Override
    public boolean isCheckedIn(Long activityId, Long userId) {
        LambdaQueryWrapper<ActivityCheckin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityCheckin::getActivityId, activityId)
                .eq(ActivityCheckin::getUserId, userId)
                .eq(ActivityCheckin::getStatus, 1);
        return count(queryWrapper) > 0;
    }

    @Override
    public List<ActivityCheckin> getCheckinsByActivityId(Long activityId) {
        LambdaQueryWrapper<ActivityCheckin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityCheckin::getActivityId, activityId)
                .orderByDesc(ActivityCheckin::getCheckinTime);
        return list(queryWrapper);
    }

    @Override
    public List<ActivityCheckin> getCheckinsByUserId(Long userId) {
        LambdaQueryWrapper<ActivityCheckin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityCheckin::getUserId, userId)
                .orderByDesc(ActivityCheckin::getCheckinTime);
        return list(queryWrapper);
    }

    @Override
    public int getCheckinCount(Long activityId) {
        LambdaQueryWrapper<ActivityCheckin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityCheckin::getActivityId, activityId)
                .eq(ActivityCheckin::getStatus, 1);
        return Math.toIntExact(count(queryWrapper));
    }
}
