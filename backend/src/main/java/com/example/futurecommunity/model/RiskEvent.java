package com.example.futurecommunity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("risk_event")
public class RiskEvent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    @TableField("risk_type")
    private String riskType;
    @TableField("risk_level")
    private String riskLevel;
    @TableField("image_url")
    private String imageUrl;
    private String status;
    @TableField("is_read")
    private Integer isRead;
    @TableField("reporter_id")
    private Long reporterId;
    @TableField("handler_id")
    private Long handlerId;
    @TableField("create_time")
    private Date createTime;
    @TableField("update_time")
    private Date updateTime;
    @TableField("ai_result")
    private String aiResult;
    @TableField("ai_confidence")
    private Double aiConfidence;
    
    @TableField("glm_result")
    private String glmResult;
    
    @TableField("baidu_labels")
    private String baiduLabels;
    
    private String remark;
    private String location;
    
    @TableField("camera_id")
    private String cameraId;
}