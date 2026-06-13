package com.example.futurecommunity.controller;

import com.example.futurecommunity.model.Activity;
import com.example.futurecommunity.model.ActivitySignup;
import com.example.futurecommunity.model.ActivityPhoto;
import com.example.futurecommunity.model.ActivityComment;
import com.example.futurecommunity.model.ActivityCheckin;
import com.example.futurecommunity.model.ResidentHouse;
import com.example.futurecommunity.service.ActivityService;
import com.example.futurecommunity.service.ActivityCheckinService;
import com.example.futurecommunity.service.ResidentHouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动控制器
 */
@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    private static final Logger logger = LoggerFactory.getLogger(ActivityController.class);

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityCheckinService activityCheckinService;

    @Autowired
    private ResidentHouseService residentHouseService;

    /**
     * 发布活动
     * 物业用户可以发布活动
     * @param activity 活动信息
     * @return 发布成功的活动
     */
    @PreAuthorize("hasRole('PROPERTY')")
    @PostMapping("/publish")
    public Activity publishActivity(@RequestBody Activity activity) {
        return activityService.publishActivity(activity);
    }

    /**
     * 获取活动列表
     * 所有用户都可以获取活动列表
     * @return 活动列表
     */
    @GetMapping("/list")
    public List<Activity> getActivityList() {
        return activityService.getActivityList();
    }

    /**
     * 根据 ID 获取活动详情
     * 所有用户都可以获取活动详情
     * @param id 活动 ID
     * @return 活动详情
     */
    @GetMapping("/detail/{id}")
    public Activity getActivityById(@PathVariable Long id) {
        return activityService.getActivityById(id);
    }

    /**
     * 更新活动
     * 物业用户可以更新活动
     * @param activity 活动信息
     * @return 更新后的活动
     */
    @PreAuthorize("hasRole('PROPERTY')")
    @PutMapping
    public Activity updateActivity(@RequestBody Activity activity) {
        return activityService.updateActivity(activity);
    }

    /**
     * 删除活动
     * 物业用户可以删除活动
     * @param id 活动 ID
     */
    @PreAuthorize("hasRole('PROPERTY')")
    @DeleteMapping("/{id}")
    public void deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
    }

    /**
     * 居民报名活动
     * 只有已认证房产的居民才能报名
     * @param activityId 活动ID
     * @param userId 用户ID
     * @return 报名信息
     */
    @PreAuthorize("hasRole('RESIDENT')")
    @PostMapping("/signup")
    public ActivitySignup signupActivity(@RequestParam Long activityId, @RequestParam Long userId) {
        // 检查用户是否已认证房产
        List<ResidentHouse> residentHouses = residentHouseService.lambdaQuery()
                .eq(ResidentHouse::getUserId, userId)
                .eq(ResidentHouse::getCertified, 1)
                .list();

        if (residentHouses.isEmpty()) {
            throw new RuntimeException("只有已认证房产的居民才能报名活动");
        }

        // 报名活动
        return activityService.signupActivity(activityId, userId);
    }

    /**
     * 检查用户是否已报名活动
     * @param activityId 活动ID
     * @param userId 用户ID
     * @return 是否已报名
     */
    @GetMapping("/check-signup")
    public boolean checkUserSignedUp(@RequestParam Long activityId, @RequestParam Long userId) {
        return activityService.checkUserSignedUp(activityId, userId);
    }

    /**
     * 获取用户的活动报名记录
     * @param userId 用户ID
     * @return 活动报名记录列表
     */
    @GetMapping("/user-signups/{userId}")
    public List<ActivitySignup> getUserSignups(@PathVariable Long userId) {
        logger.info("接收到获取用户活动报名记录的请求，userId: {}", userId);
        try {
            List<ActivitySignup> signups = activityService.getUserSignups(userId);
            logger.info("获取用户活动报名记录成功，userId: {}, 记录数: {}", userId, signups.size());
            return signups;
        } catch (Exception e) {
            logger.error("获取用户活动报名记录失败，userId: {}", userId, e);
            throw e;
        }
    }

    /**
     * 上传活动照片
     * 居民用户可以上传活动照片
     * @param activityId 活动ID
     * @param userId 用户ID
     * @param photoUrl 照片URL
     * @return 活动照片信息
     */
    @PreAuthorize("hasRole('RESIDENT')")
    @PostMapping("/upload-photo")
    public ActivityPhoto uploadActivityPhoto(@RequestParam Long activityId, @RequestParam Long userId, @RequestParam String photoUrl) {
        return activityService.uploadActivityPhoto(activityId, userId, photoUrl);
    }

    /**
     * 获取活动照片列表
     * 所有用户都可以获取活动照片列表
     * @param activityId 活动ID
     * @return 活动照片列表
     */
    @GetMapping("/photos/{activityId}")
    public List<ActivityPhoto> getActivityPhotos(@PathVariable Long activityId) {
        return activityService.getActivityPhotos(activityId);
    }

    /**
     * 发表活动评价
     * 只有已报名活动的居民才能评价
     * @param activityId 活动ID
     * @param userId 用户ID
     * @param content 评价内容
     * @param rating 评分
     * @return 活动评价信息
     */
    @PreAuthorize("hasRole('RESIDENT')")
    @PostMapping("/add-comment")
    public ActivityComment addActivityComment(@RequestParam Long activityId, @RequestParam Long userId, @RequestParam String content, @RequestParam Integer rating) {
        return activityService.addActivityComment(activityId, userId, content, rating);
    }

    /**
     * 获取活动评价列表
     * 所有用户都可以获取活动评价列表
     * @param activityId 活动 ID
     * @return 活动评价列表
     */
    @GetMapping("/comments/{activityId}")
    public List<ActivityComment> getActivityComments(@PathVariable Long activityId) {
        return activityService.getActivityComments(activityId);
    }

    /**
     * 删除活动评价
     * 物业用户可以删除不文明的评论
     * @param id 评价 ID
     */
    @PreAuthorize("hasRole('PROPERTY')")
    @DeleteMapping("/comment/{id}")
    public void deleteActivityComment(@PathVariable Long id) {
        activityService.deleteActivityComment(id);
    }

    /**
     * 删除活动照片
     * 物业用户可以删除不当照片
     * @param id 照片 ID
     */
    @PreAuthorize("hasRole('PROPERTY')")
    @DeleteMapping("/photo/{id}")
    public void deleteActivityPhoto(@PathVariable Long id) {
        activityService.deleteActivityPhoto(id);
    }

    /**
     * 用户签到
     * 只有已报名活动的居民才能签到
     * @param activityId 活动 ID
     * @param userId 用户 ID
     * @return 是否签到成功
     */
    @PreAuthorize("hasRole('RESIDENT')")
    @PostMapping("/checkin")
    public boolean checkin(@RequestParam Long activityId, @RequestParam Long userId) {
        logger.info("用户签到，activityId: {}, userId: {}", activityId, userId);
        try {
            return activityCheckinService.checkin(activityId, userId);
        } catch (Exception e) {
            logger.error("签到失败，activityId: {}, userId: {}", activityId, userId, e);
            throw e;
        }
    }

    /**
     * 检查用户是否已签到
     * @param activityId 活动 ID
     * @param userId 用户 ID
     * @return 是否已签到
     */
    @GetMapping("/check-checkin")
    public boolean isCheckedIn(@RequestParam Long activityId, @RequestParam Long userId) {
        return activityCheckinService.isCheckedIn(activityId, userId);
    }

    /**
     * 获取活动的签到列表
     * @param activityId 活动 ID
     * @return 签到列表
     */
    @GetMapping("/checkins/{activityId}")
    public List<ActivityCheckin> getCheckinsByActivityId(@PathVariable Long activityId) {
        return activityCheckinService.getCheckinsByActivityId(activityId);
    }

    /**
     * 获取用户的签到记录
     * @param userId 用户 ID
     * @return 签到记录列表
     */
    @GetMapping("/user-checkins/{userId}")
    public List<ActivityCheckin> getCheckinsByUserId(@PathVariable Long userId) {
        return activityCheckinService.getCheckinsByUserId(userId);
    }
}
