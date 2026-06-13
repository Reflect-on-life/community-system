package com.example.futurecommunity.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("notice_read")
public class NoticeRead {
    @TableField("notice_id")
    private Long noticeId;
    @TableField("user_id")
    private Long userId;
    private Date readAt;
}
