package com.example.futurecommunity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.futurecommunity.model.User;

import java.util.List;

public interface UserService extends IService<User> {
    
    List<String> getUserPermissions(Long userId);
}
