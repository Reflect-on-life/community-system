package com.example.futurecommunity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("activity_comment")
public class ActivityComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("activity_id")
    private Long activityId;
    @TableField("user_id")
    private Long userId;
    private String content;
    private Integer rating;
    @TableField("created_at")
    private Date createdAt;
}
