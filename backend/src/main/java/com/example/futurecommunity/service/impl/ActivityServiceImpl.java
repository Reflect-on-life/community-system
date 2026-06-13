package com.example.futurecommunity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.futurecommunity.mapper.ActivityMapper;
import com.example.futurecommunity.mapper.ActivitySignupMapper;
import com.example.futurecommunity.mapper.ActivityPhotoMapper;
import com.example.futurecommunity.mapper.ActivityCommentMapper;
import com.example.futurecommunity.model.Activity;
import com.example.futurecommunity.model.ActivitySignup;
import com.example.futurecommunity.model.ActivityPhoto;
import com.example.futurecommunity.model.ActivityComment;
import com.example.futurecommunity.service.ActivityService;
import com.example.futurecommunity.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 活动服务实现类
 */
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {



    @Autowired
    private ActivitySignupMapper activitySignupMapper;

    @Autowired
    private ActivityPhotoMapper activityPhotoMapper;

    @Autowired
    private ActivityCommentMapper activityCommentMapper;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 发布活动
     * @param activity 活动信息
     * @return 发布成功的活动
     */
    @Override
    public Activity publishActivity(Activity activity) {
        // 设置初始值
        activity.setEnrolledCount(0);
        activity.setStatus(0); // 0-未开始
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());

        // 保存活动
        save(activity);

        // 将活动库存存入Redis
        String stockKey = "activity:stock:" + activity.getId();
        redisUtil.set(stockKey, activity.getQuota(), 7, TimeUnit.DAYS);

        return activity;
    }

    /**
     * 获取活动列表
     * @return 活动列表
     */
    @Override
    public List<Activity> getActivityList() {
        // 按活动开始时间倒序排序（最近的放前面）
        return list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Activity>()
                .orderByDesc(Activity::getStartTime));
    }

    /**
     * 根据ID获取活动详情
     * @param id 活动ID
     * @return 活动详情
     */
    @Override
    public Activity getActivityById(Long id) {
        return getById(id);
    }

    /**
     * 居民报名活动
     * @param activityId 活动ID
     * @param userId 用户ID
     * @return 报名信息
     */
    @Override
    @Transactional
    public ActivitySignup signupActivity(Long activityId, Long userId) {
        // 检查活动是否存在
        Activity activity = getById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        // 检查活动是否已结束
        if (activity.getStatus() == 2) {
            throw new RuntimeException("活动已结束");
        }

        // 检查用户是否已报名
        if (checkUserSignedUp(activityId, userId)) {
            throw new RuntimeException("您已报名该活动");
        }

        // 预减库存
        String stockKey = "activity:stock:" + activityId;
        Object stockObj = redisUtil.get(stockKey);
        Long stock = stockObj instanceof Number ? ((Number) stockObj).longValue() : null;
        if (stock == null || stock <= 0) {
            throw new RuntimeException("活动名额已满");
        }

        // 使用Redis的decrement操作进行原子扣减
        Long remainStock = redisUtil.decrement(stockKey);
        if (remainStock < 0) {
            // 库存不足，恢复库存
            redisUtil.increment(stockKey);
            throw new RuntimeException("活动名额已满");
        }

        try {
            // 创建报名记录
            ActivitySignup signup = new ActivitySignup();
            signup.setActivityId(activityId);
            signup.setUserId(userId);
            signup.setStatus("confirmed"); // 已确认
            signup.setSignupTime(LocalDateTime.now());

            // 保存报名记录
            activitySignupMapper.insert(signup);

            // 更新活动已报名人数
            activity.setEnrolledCount(activity.getEnrolledCount() + 1);
            activity.setUpdatedAt(LocalDateTime.now());
            updateById(activity);

            return signup;
        } catch (Exception e) {
            // 发生异常，恢复库存
            redisUtil.increment(stockKey);
            throw e;
        }
    }

    /**
     * 检查用户是否已报名活动
     * @param activityId 活动ID
     * @param userId 用户ID
     * @return 是否已报名
     */
    @Override
    public boolean checkUserSignedUp(Long activityId, Long userId) {
        // 查询用户是否已报名该活动
        Long count = activitySignupMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ActivitySignup>()
                        .eq(ActivitySignup::getActivityId, activityId)
                        .eq(ActivitySignup::getUserId, userId)
        );
        return count > 0;
    }

    /**
     * 获取用户的活动报名记录
     * @param userId 用户ID
     * @return 活动报名记录列表
     */
    @Override
    public List<ActivitySignup> getUserSignups(Long userId) {
        return activitySignupMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ActivitySignup>()
                        .eq(ActivitySignup::getUserId, userId)
        );
    }

    /**
     * 上传活动照片
     * @param activityId 活动ID
     * @param userId 用户ID
     * @param photoUrl 照片URL
     * @return 活动照片信息
     */
    @Override
    public ActivityPhoto uploadActivityPhoto(Long activityId, Long userId, String photoUrl) {
        // 检查活动是否存在
        Activity activity = getById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        // 创建照片记录
        ActivityPhoto photo = new ActivityPhoto();
        photo.setActivityId(activityId);
        photo.setUserId(userId);
        photo.setUrl(photoUrl);
        photo.setUploadedAt(new Date());

        // 保存照片记录
        activityPhotoMapper.insert(photo);

        return photo;
    }

    /**
     * 获取活动照片列表
     * @param activityId 活动ID
     * @return 活动照片列表
     */
    @Override
    public List<ActivityPhoto> getActivityPhotos(Long activityId) {
        return activityPhotoMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ActivityPhoto>()
                        .eq(ActivityPhoto::getActivityId, activityId)
        );
    }

    /**
     * 发表活动评价
     * @param activityId 活动ID
     * @param userId 用户ID
     * @param content 评价内容
     * @param rating 评分
     * @return 活动评价信息
     */
    @Override
    public ActivityComment addActivityComment(Long activityId, Long userId, String content, Integer rating) {
        // 检查活动是否存在
        Activity activity = getById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        // 检查用户是否已报名该活动
        if (!checkUserSignedUp(activityId, userId)) {
            throw new RuntimeException("只有报名过该活动的用户才能评价");
        }

        // 创建评价记录
        ActivityComment comment = new ActivityComment();
        comment.setActivityId(activityId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setRating(rating);
        comment.setCreatedAt(new Date());

        // 保存评价记录
        activityCommentMapper.insert(comment);

        return comment;
    }

    /**
     * 获取活动评价列表
     * @param activityId 活动 ID
     * @return 活动评价列表
     */
    @Override
    public List<ActivityComment> getActivityComments(Long activityId) {
        return activityCommentMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ActivityComment>()
                        .eq(ActivityComment::getActivityId, activityId)
                        .orderByDesc(ActivityComment::getCreatedAt)
        );
    }

    /**
     * 删除活动评价
     * @param id 评价 ID
     */
    @Override
    @Transactional
    public void deleteActivityComment(Long id) {
        activityCommentMapper.deleteById(id);
    }

    /**
     * 删除活动照片
     * @param id 照片 ID
     */
    @Override
    @Transactional
    public void deleteActivityPhoto(Long id) {
        activityPhotoMapper.deleteById(id);
    }

    /**
     * 更新活动
     * @param activity 活动信息
     * @return 更新后的活动
     */
    @Override
    @Transactional
    public Activity updateActivity(Activity activity) {
        // 检查活动是否存在
        Activity existActivity = getById(activity.getId());
        if (existActivity == null) {
            throw new RuntimeException("活动不存在");
        }
        
        // 更新活动信息
        existActivity.setTitle(activity.getTitle());
        existActivity.setDescription(activity.getDescription());
        existActivity.setPosterUrl(activity.getPosterUrl());
        existActivity.setStartTime(activity.getStartTime());
        existActivity.setEndTime(activity.getEndTime());
        existActivity.setLocation(activity.getLocation());
        existActivity.setQuota(activity.getQuota());
        existActivity.setUpdatedAt(LocalDateTime.now());
        
        updateById(existActivity);
        return existActivity;
    }

    /**
     * 删除活动
     * @param id 活动 ID
     */
    @Override
    @Transactional
    public void deleteActivity(Long id) {
        // 检查活动是否存在
        Activity activity = getById(id);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        
        // 删除活动
        removeById(id);
        
        // 清理 Redis 中的库存信息
        String stockKey = "activity:stock:" + id;
        redisUtil.delete(stockKey);
    }
}

