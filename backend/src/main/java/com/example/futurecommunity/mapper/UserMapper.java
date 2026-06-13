package com.example.futurecommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.futurecommunity.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    @Select("SELECT p.permission_key " +
            "FROM user_role ur " +
            "JOIN role_permission rp ON ur.role_id = rp.role_id " +
            "JOIN permission p ON rp.permission_id = p.id " +
            "WHERE ur.user_id = #{userId}")
    List<String> getUserPermissions(Long userId);
}
