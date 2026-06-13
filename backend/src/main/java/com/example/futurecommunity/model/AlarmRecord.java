package com.example.futurecommunity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 行为告警记录实体类
 * 用于存储摄像头行为识别产生的告警记录（打架、发传单等）
 */
@Data
@TableName("alarm_record")
public class AlarmRecord {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 告警类型：打架/发传单/其他
     */
    @TableField("alarm_type")
    private String alarmType;

    /**
     * 置信度（0-1）
     */
    @TableField("confidence")
    private Double confidence;

    /**
     * 截图保存路径
     */
    @TableField("image_path")
    private String imagePath;

    /**
     * AI 风险分析详情
     */
    @TableField("description")
    private String description;

    /**
     * 摄像头位置描述
     */
    @TableField("location")
    private String location;

    /**
     * 告警时间
     */
    @TableField("alarm_time")
    private Date alarmTime;

    /**
     * 是否已读：0-未读，1-已读
     */
    @TableField("is_read")
    private Integer isRead;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private Date createdAt;
}
