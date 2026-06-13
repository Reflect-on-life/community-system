package com.example.futurecommunity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("neighbor_message_reply")
public class NeighborMessageReply {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("message_id")
    private Long messageId;
    @TableField("user_id")
    private Long userId;
    private String content;
    @TableField("created_at")
    private Date createdAt;
}
