package com.example.futurecommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.futurecommunity.model.User;
import com.example.futurecommunity.model.vo.UserVO;
import com.example.futurecommunity.service.UserService;
import com.example.futurecommunity.common.annotation.OperationLog;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

/**
 * 用户控制器 - 系统管理端
 */
@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 分页查询用户
     * 管理员和医生可以访问
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @GetMapping("/page")
    public Page<UserVO> getUsersByPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type) {
        
        System.out.println("=== 用户分页查询 ===");
        System.out.println("页码：" + page);
        System.out.println("每页大小：" + size);
        System.out.println("用户类型：" + (type != null ? type : "全部"));
        
        // 创建分页对象
        Page<User> userPage = new Page<>(page, size);
        
        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            System.out.println("添加用户类型筛选条件：" + type);
            queryWrapper.eq(User::getType, type);
        } else {
            System.out.println("不添加用户类型筛选条件");
        }
        
        // 执行分页查询
        System.out.println("开始执行分页查询");
        userService.page(userPage, queryWrapper);
        System.out.println("查询完成，总记录数：" + userPage.getTotal());
        System.out.println("查询完成，当前页记录数：" + userPage.getRecords().size());
        
        // 转换为 VO
        Page<UserVO> userVOPage = new Page<>();
        BeanUtils.copyProperties(userPage, userVOPage);
        
        // 转换记录
        userVOPage.setRecords(userPage.getRecords().stream().map(this::convertToVO).toList());
        
        System.out.println("返回用户查询结果，总记录数：" + userVOPage.getTotal());
        System.out.println("===================");
        return userVOPage;
    }

    /**
     * 新增用户
     * 仅管理员可以创建用户
     */
    @OperationLog(action = "创建", target = "#p0.name", detail = "的信息")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public UserVO createUser(@RequestBody User user) {
        System.out.println("=== 新增用户 ===");
        System.out.println("用户姓名：" + user.getName());
        System.out.println("手机号：" + user.getPhone());
        System.out.println("用户类型：" + user.getType());
        
        // 设置默认值
        user.setStatus(1);
        user.setCreatedAt(new Date());
        
        // 保存用户
        userService.save(user);
        
        System.out.println("用户保存成功，用户 ID: " + user.getId());
        System.out.println("===================");
        
        // 转换为 VO 并返回
        return convertToVO(user);
    }

    /**
     * 修改用户
     * 仅管理员可以更新用户
     */
    @OperationLog(action = "更新", target = "#p1.name", detail = "的信息")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public UserVO updateUser(@PathVariable Long id, @RequestBody User user) {
        System.out.println("=== 修改用户 ===");
        System.out.println("用户 ID: " + id);
        System.out.println("更新信息：" + user);
        
        // 设置 ID
        user.setId(id);
        
        // 更新用户
        userService.updateById(user);
        
        // 查询更新后的用户并转换为 VO
        User updatedUser = userService.getById(id);
        System.out.println("用户更新成功");
        System.out.println("===================");
        return convertToVO(updatedUser);
    }

    /**
     * 删除用户
     * 仅管理员可以删除用户
     */
    @OperationLog(action = "删除", target = "#p0", detail = "的账户")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        System.out.println("=== 删除用户 ===");
        System.out.println("删除用户 ID: " + id);
        
        jdbcTemplate.execute((Connection conn) -> {
            conn.setAutoCommit(false);
            java.sql.Statement stmt = conn.createStatement();
            
            try {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
                stmt.execute("DELETE FROM resident_house WHERE user_id = " + id);
                stmt.execute("DELETE FROM user_role WHERE user_id = " + id);
                stmt.execute("DELETE FROM health_record WHERE user_id = " + id);
                stmt.execute("DELETE FROM appointment WHERE user_id = " + id);
                stmt.execute("DELETE FROM repair WHERE user_id = " + id);
                stmt.execute("DELETE FROM express WHERE user_id = " + id);
                stmt.execute("DELETE FROM complaint WHERE user_id = " + id);
                stmt.execute("DELETE FROM notice_read WHERE user_id = " + id);
                stmt.execute("DELETE FROM activity_signup WHERE user_id = " + id);
                stmt.execute("DELETE FROM activity_checkin WHERE user_id = " + id);
                stmt.execute("DELETE FROM activity_comment WHERE user_id = " + id);
                stmt.execute("DELETE FROM activity_photo WHERE user_id = " + id);
                stmt.execute("DELETE FROM neighbor_message WHERE user_id = " + id);
                stmt.execute("DELETE FROM neighbor_message_like WHERE user_id = " + id);
                stmt.execute("DELETE FROM neighbor_message_reply WHERE user_id = " + id);
                stmt.execute("DELETE FROM technician_skill_relation WHERE user_id = " + id);
                stmt.execute("DELETE FROM operation_log WHERE user_id = " + id);
                stmt.execute("UPDATE house SET owner_id = NULL WHERE owner_id = " + id);
                stmt.execute("DELETE FROM user WHERE id = " + id);
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
                
                conn.commit();
                System.out.println("用户及相关数据清理成功");
            } catch (Exception e) {
                conn.rollback();
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
                System.out.println("删除用户失败，已回滚: " + e.getMessage());
                e.printStackTrace();
                throw e;
            } finally {
                stmt.close();
                conn.setAutoCommit(true);
            }
            
            return null;
        });
        
        System.out.println("用户删除成功");
        System.out.println("===================");
    }

    /**
     * 通过手机号查询用户
     * 物业管理员可以访问
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPERTY')")
    @GetMapping("/search/phone")
    public UserVO getUserByPhone(@RequestParam String phone) {
        System.out.println("=== 手机号查询用户 ===");
        System.out.println("查询手机号：" + phone);
        
        User user = userService.lambdaQuery()
                .eq(User::getPhone, phone)
                .one();
        
        if (user == null) {
            System.out.println("未找到该手机号对应的用户：" + phone);
            System.out.println("===================");
            return null;
        }
        
        System.out.println("找到用户：" + user.getName());
        System.out.println("===================");
        return convertToVO(user);
    }

    /**
     * 将 User 实体转换为 UserVO
     */
    private UserVO convertToVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }
}
