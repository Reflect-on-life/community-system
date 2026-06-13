package com.example.futurecommunity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.futurecommunity.model.ActivityCheckin;

import java.util.List;

/**
 * 活动签到服务接口
 */
public interface ActivityCheckinService extends IService<ActivityCheckin> {

    /**
     * 用户签到
     * @param activityId 活动 ID
     * @param userId 用户 ID
     * @return 是否签到成功
     */
    boolean checkin(Long activityId, Long userId);

    /**
     * 检查用户是否已签到
     * @param activityId 活动 ID
     * @param userId 用户 ID
     * @return 是否已签到
     */
    boolean isCheckedIn(Long activityId, Long userId);

    /**
     * 获取活动的签到列表
     * @param activityId 活动 ID
     * @return 签到列表
     */
    List<ActivityCheckin> getCheckinsByActivityId(Long activityId);

    /**
     * 获取用户的签到记录
     * @param userId 用户 ID
     * @return 签到记录列表
     */
    List<ActivityCheckin> getCheckinsByUserId(Long userId);

    /**
     * 获取活动的签到人数
     * @param activityId 活动 ID
     * @return 签到人数
     */
    int getCheckinCount(Long activityId);
}
