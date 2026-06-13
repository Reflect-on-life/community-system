package com.example.futurecommunity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("complaint_reply")
public class ComplaintReply {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("complaint_id")
    private Long complaintId;
    @TableField("reply_user_id")
    private Long replyUserId;
    private String content;
    @TableField("created_at")
    private Date createdAt;
}
