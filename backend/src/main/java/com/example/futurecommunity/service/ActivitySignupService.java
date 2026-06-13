package com.example.futurecommunity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.futurecommunity.model.ActivitySignup;

import java.util.List;

public interface ActivitySignupService extends IService<ActivitySignup> {
    // 活动报名
    boolean signup(Long activityId, Long userId);
    
    // 取消报名
    boolean cancelSignup(Long activityId, Long userId);
    
    // 根据活动ID获取报名列表
    List<ActivitySignup> getSignupsByActivityId(Long activityId);
    
    // 根据用户ID获取报名列表
    List<ActivitySignup> getSignupsByUserId(Long userId);
    
    // 检查用户是否已报名
    boolean isSignedUp(Long activityId, Long userId);
    
    // 检查用户是否为认证居民
    boolean isCertifiedResident(Long userId);
}
