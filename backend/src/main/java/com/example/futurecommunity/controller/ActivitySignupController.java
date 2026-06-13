package com.example.futurecommunity.controller;

import com.example.futurecommunity.model.ActivitySignup;
import com.example.futurecommunity.service.ActivitySignupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动报名控制器
 */
@RestController
@RequestMapping("/api/activity-signup")
public class ActivitySignupController {

    private static final Logger logger = LoggerFactory.getLogger(ActivitySignupController.class);

    @Autowired
    private ActivitySignupService activitySignupService;

    /**
     * 用户报名活动
     * @param activityId 活动 ID
     * @param userId 用户 ID
     * @return 是否报名成功
     */
    @PreAuthorize("hasRole('RESIDENT')")
    @PostMapping("/signup")
    public boolean signup(@RequestParam Long activityId, @RequestParam Long userId) {
        logger.info("用户报名活动，activityId: {}, userId: {}", activityId, userId);
        try {
            boolean result = activitySignupService.signup(activityId, userId);
            if (result) {
                logger.info("报名成功，activityId: {}, userId: {}", activityId, userId);
            } else {
                logger.warn("报名失败，activityId: {}, userId: {}", activityId, userId);
            }
            return result;
        } catch (Exception e) {
            logger.error("报名异常，activityId: {}, userId: {}", activityId, userId, e);
            throw e;
        }
    }

    /**
     * 用户取消报名
     * @param activityId 活动 ID
     * @param userId 用户 ID
     * @return 是否取消成功
     */
    @PreAuthorize("hasRole('RESIDENT')")
    @PostMapping("/cancel")
    public boolean cancelSignup(@RequestParam Long activityId, @RequestParam Long userId) {
        logger.info("用户取消报名，activityId: {}, userId: {}", activityId, userId);
        try {
            boolean result = activitySignupService.cancelSignup(activityId, userId);
            if (result) {
                logger.info("取消报名成功，activityId: {}, userId: {}", activityId, userId);
            } else {
                logger.warn("取消报名失败，activityId: {}, userId: {}", activityId, userId);
            }
            return result;
        } catch (Exception e) {
            logger.error("取消报名异常，activityId: {}, userId: {}", activityId, userId, e);
            throw e;
        }
    }

    /**
     * 获取活动的报名列表
     * @param activityId 活动 ID
     * @return 报名列表
     */
    @GetMapping("/activity/{activityId}")
    public List<ActivitySignup> getSignupsByActivityId(@PathVariable Long activityId) {
        logger.info("获取活动报名列表，activityId: {}", activityId);
        try {
            return activitySignupService.getSignupsByActivityId(activityId);
        } catch (Exception e) {
            logger.error("获取活动报名列表失败，activityId: {}", activityId, e);
            throw e;
        }
    }

    /**
     * 获取用户的报名列表
     * @param userId 用户 ID
     * @return 报名列表
     */
    @GetMapping("/user/{userId}")
    public List<ActivitySignup> getSignupsByUserId(@PathVariable Long userId) {
        logger.info("获取用户报名列表，userId: {}", userId);
        try {
            return activitySignupService.getSignupsByUserId(userId);
        } catch (Exception e) {
            logger.error("获取用户报名列表失败，userId: {}", userId, e);
            throw e;
        }
    }

    /**
     * 检查用户是否已报名
     * @param activityId 活动 ID
     * @param userId 用户 ID
     * @return 是否已报名
     */
    @GetMapping("/check")
    public boolean isSignedUp(@RequestParam Long activityId, @RequestParam Long userId) {
        logger.info("检查用户报名状态，activityId: {}, userId: {}", activityId, userId);
        try {
            return activitySignupService.isSignedUp(activityId, userId);
        } catch (Exception e) {
            logger.error("检查报名状态失败，activityId: {}, userId: {}", activityId, userId, e);
            throw e;
        }
    }
}
