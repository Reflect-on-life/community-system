package com.example.futurecommunity.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.util.Date;

@Data
public class OperationLogVO {
    private Long id;
    private Long userId;
    private String action;
    private String detail;
    private String method;
    private String ip;
    private String status;
    private String role;
    private Long costTime;
    private Date createdAt;
}