package com.example.futurecommunity.dto;

import lombok.Data;

/**
 * 密码修改请求 DTO
 */
@Data
public class PasswordChangeRequest {
    
    /**
     * 原密码
     */
    private String oldPassword;
    
    /**
     * 新密码
     */
    private String newPassword;
}
