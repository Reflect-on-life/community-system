package com.example.futurecommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.futurecommunity.mapper.ActivitySignupMapper;
import com.example.futurecommunity.model.Activity;
import com.example.futurecommunity.model.ActivitySignup;
import com.example.futurecommunity.model.ResidentHouse;
import com.example.futurecommunity.service.ActivityService;
import com.example.futurecommunity.service.ActivitySignupService;
import com.example.futurecommunity.service.ResidentHouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivitySignupServiceImpl extends ServiceImpl<ActivitySignupMapper, ActivitySignup> implements ActivitySignupService {
    
    @Autowired
    private ActivityService activityService;
    
    @Autowired
    private ResidentHouseService residentHouseService;
    
    @Override
    public boolean signup(Long activityId, Long userId) {
        // 检查用户是否为认证居民
        if (!isCertifiedResident(userId)) {
            return false;
        }
        
        // 检查用户是否已报名
        if (isSignedUp(activityId, userId)) {
            return false;
        }
        
        // 检查活动是否存在
        Activity activity = activityService.getById(activityId);
        if (activity == null) {
            return false;
        }
        
        // 检查活动名额是否已满
        if (activity.getEnrolledCount() >= activity.getQuota()) {
            return false;
        }
        
        // 创建报名记录
        ActivitySignup signup = new ActivitySignup();
        signup.setActivityId(activityId);
        signup.setUserId(userId);
        signup.setStatus("confirmed"); // 已确认
        signup.setSignupTime(LocalDateTime.now());
        
        // 保存报名记录
        if (!save(signup)) {
            return false;
        }
        
        // 更新活动的已报名人数
        activity.setEnrolledCount(activity.getEnrolledCount() + 1);
        activityService.updateById(activity);
        
        return true;
    }
    
    @Override
    public boolean cancelSignup(Long activityId, Long userId) {
        // 检查用户是否已报名
        LambdaQueryWrapper<ActivitySignup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivitySignup::getActivityId, activityId)
                .eq(ActivitySignup::getUserId, userId);
        
        ActivitySignup signup = getOne(queryWrapper);
        if (signup == null) {
            return false;
        }
        
        // 取消报名
        signup.setStatus("cancelled"); // 已取消
        if (!updateById(signup)) {
            return false;
        }
        
        // 更新活动的已报名人数
        Activity activity = activityService.getById(activityId);
        if (activity != null) {
            activity.setEnrolledCount(Math.max(0, activity.getEnrolledCount() - 1));
            activityService.updateById(activity);
        }
        
        return true;
    }
    
    @Override
    public List<ActivitySignup> getSignupsByActivityId(Long activityId) {
        LambdaQueryWrapper<ActivitySignup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivitySignup::getActivityId, activityId);
        return list(queryWrapper);
    }
    
    @Override
    public List<ActivitySignup> getSignupsByUserId(Long userId) {
        LambdaQueryWrapper<ActivitySignup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivitySignup::getUserId, userId);
        return list(queryWrapper);
    }
    
    @Override
    public boolean isSignedUp(Long activityId, Long userId) {
        LambdaQueryWrapper<ActivitySignup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivitySignup::getActivityId, activityId)
                .eq(ActivitySignup::getUserId, userId);
        return count(queryWrapper) > 0;
    }
    
    @Override
    public boolean isCertifiedResident(Long userId) {
        LambdaQueryWrapper<ResidentHouse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ResidentHouse::getUserId, userId)
                .eq(ResidentHouse::getCertified, 1);
        return residentHouseService.count(queryWrapper) > 0;
    }
}
