package com.example.futurecommunity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 活动签到模型
 */
@Data
@TableName("activity_checkin")
public class ActivityCheckin {
    /**
     * 签到 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 活动 ID
     */
    @TableField("activity_id")
    private Long activityId;

    /**
     * 用户 ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 签到状态：0-未签到，1-已签到
     */
    @TableField("status")
    private Integer status;

    /**
     * 签到时间
     */
    @TableField("checkin_time")
    private LocalDateTime checkinTime;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
