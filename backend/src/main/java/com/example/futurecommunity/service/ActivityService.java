package com.example.futurecommunity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.futurecommunity.model.Activity;
import com.example.futurecommunity.model.ActivitySignup;
import com.example.futurecommunity.model.ActivityPhoto;
import com.example.futurecommunity.model.ActivityComment;

import java.util.List;

/**
 * 活动服务接口
 */
public interface ActivityService extends IService<Activity> {
    /**
     * 发布活动
     * @param activity 活动信息
     * @return 发布成功的活动
     */
    Activity publishActivity(Activity activity);

    /**
     * 获取活动列表
     * @return 活动列表
     */
    List<Activity> getActivityList();

    /**
     * 根据ID获取活动详情
     * @param id 活动ID
     * @return 活动详情
     */
    Activity getActivityById(Long id);

    /**
     * 居民报名活动
     * @param activityId 活动ID
     * @param userId 用户ID
     * @return 报名信息
     */
    ActivitySignup signupActivity(Long activityId, Long userId);

    /**
     * 检查用户是否已报名活动
     * @param activityId 活动ID
     * @param userId 用户ID
     * @return 是否已报名
     */
    boolean checkUserSignedUp(Long activityId, Long userId);

    /**
     * 获取用户的活动报名记录
     * @param userId 用户ID
     * @return 活动报名记录列表
     */
    List<ActivitySignup> getUserSignups(Long userId);

    /**
     * 上传活动照片
     * @param activityId 活动ID
     * @param userId 用户ID
     * @param photoUrl 照片URL
     * @return 活动照片信息
     */
    ActivityPhoto uploadActivityPhoto(Long activityId, Long userId, String photoUrl);

    /**
     * 获取活动照片列表
     * @param activityId 活动ID
     * @return 活动照片列表
     */
    List<ActivityPhoto> getActivityPhotos(Long activityId);

    /**
     * 发表活动评价
     * @param activityId 活动ID
     * @param userId 用户ID
     * @param content 评价内容
     * @param rating 评分
     * @return 活动评价信息
     */
    ActivityComment addActivityComment(Long activityId, Long userId, String content, Integer rating);

    /**
     * 获取活动评价列表
     * @param activityId 活动 ID
     * @return 活动评价列表
     */
    List<ActivityComment> getActivityComments(Long activityId);

    /**
     * 删除活动评价
     * @param id 评价 ID
     */
    void deleteActivityComment(Long id);

    /**
     * 删除活动照片
     * @param id 照片 ID
     */
    void deleteActivityPhoto(Long id);

    /**
     * 更新活动
     * @param activity 活动信息
     * @return 更新后的活动
     */
    Activity updateActivity(Activity activity);

    /**
     * 删除活动
     * @param id 活动 ID
     */
    void deleteActivity(Long id);
}

