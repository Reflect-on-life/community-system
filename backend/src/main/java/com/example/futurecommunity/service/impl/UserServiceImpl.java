package com.example.futurecommunity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.futurecommunity.mapper.UserMapper;
import com.example.futurecommunity.model.User;
import com.example.futurecommunity.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Override
    public List<String> getUserPermissions(Long userId) {
        return baseMapper.getUserPermissions(userId);
    }
}
