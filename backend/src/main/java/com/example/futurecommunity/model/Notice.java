package com.example.futurecommunity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("notice")
public class Notice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    @TableField("target_type")
    private String targetType;
    @TableField("created_by")
    private Long createdBy;
    @TableField("repair_id")
    private Long repairId;
    @TableField("created_at")
    private Date createdAt;
}
