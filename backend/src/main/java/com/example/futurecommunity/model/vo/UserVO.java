package com.example.futurecommunity.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 用户VO类，用于返回给前端，不包含密码字段
 */
@Data
public class UserVO {
    private Long id;
    private String phone;
    private String name;
    private String gender;
    private Date birthday;
    private String idCard;
    private String type;
    private Integer status;
    private String tags;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
}
