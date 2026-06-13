package com.example.futurecommunity.controller;

import com.example.futurecommunity.model.Role;
import com.example.futurecommunity.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色控制器
 */
@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 获取所有角色
     * 仅管理员可以访问
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Role> getRoles() {
        return roleService.list();
    }

    /**
     * 根据ID获取角色
     * 仅管理员可以访问
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public Role getRoleById(@PathVariable Long id) {
        return roleService.getById(id);
    }

    /**
     * 创建角色
     * 仅管理员可以创建角色
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Role createRole(@RequestBody Role role) {
        roleService.save(role);
        return role;
    }

    /**
     * 更新角色
     * 仅管理员可以更新角色
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Role updateRole(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        roleService.updateById(role);
        return role;
    }

    /**
     * 删除角色
     * 仅管理员可以删除角色
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteRole(@PathVariable Long id) {
        roleService.removeById(id);
    }
}
