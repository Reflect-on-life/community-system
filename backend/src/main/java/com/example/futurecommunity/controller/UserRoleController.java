package com.example.futurecommunity.controller;

import com.example.futurecommunity.model.UserRole;
import com.example.futurecommunity.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户角色控制器 - 系统管理端
 */
@RestController
@RequestMapping("/api/admin/user-role")
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;

    /**
     * 分配用户角色
     * 仅管理员可以访问
     * 使用事务管理，确保操作原子性
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @PostMapping("/assign")
    public void assignRoles(@RequestBody RoleAssignRequest request) {
        Long userId = request.getUserId();
        List<Long> roleIds = request.getRoleIds();
        
        // 1. 删除该用户原有角色
        userRoleService.lambdaUpdate()
                .eq(UserRole::getUserId, userId)
                .remove();
        
        // 2. 批量插入新的角色
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                // 检查是否已存在该关联，避免重复插入
                boolean exists = userRoleService.lambdaQuery()
                        .eq(UserRole::getUserId, userId)
                        .eq(UserRole::getRoleId, roleId)
                        .exists();
                
                if (!exists) {
                    UserRole userRole = new UserRole();
                    userRole.setUserId(userId);
                    userRole.setRoleId(roleId);
                    userRoleService.save(userRole);
                }
            }
        }
    }

    /**
     * 获取所有用户角色关联
     * 仅管理员可以访问
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserRole> getUserRoles() {
        return userRoleService.list();
    }

    /**
     * 根据用户ID获取角色列表
     * 仅管理员可以访问
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public List<UserRole> getUserRolesByUserId(@PathVariable Long userId) {
        return userRoleService.lambdaQuery()
                .eq(UserRole::getUserId, userId)
                .list();
    }

    /**
     * 根据角色ID获取用户列表
     * 仅管理员可以访问
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/role/{roleId}")
    public List<UserRole> getUserRolesByRoleId(@PathVariable Long roleId) {
        return userRoleService.lambdaQuery()
                .eq(UserRole::getRoleId, roleId)
                .list();
    }

    /**
     * 创建用户角色关联
     * 仅管理员可以访问
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public UserRole createUserRole(@RequestBody UserRole userRole) {
        userRoleService.save(userRole);
        return userRole;
    }

    /**
     * 删除用户角色关联
     * 仅管理员可以访问
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/user/{userId}/role/{roleId}")
    public void deleteUserRole(@PathVariable Long userId, @PathVariable Long roleId) {
        userRoleService.lambdaUpdate()
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, roleId)
                .remove();
    }

    /**
     * 角色分配请求DTO
     */
    public static class RoleAssignRequest {
        private Long userId;
        private List<Long> roleIds;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public List<Long> getRoleIds() {
            return roleIds;
        }

        public void setRoleIds(List<Long> roleIds) {
            this.roleIds = roleIds;
        }
    }
}
