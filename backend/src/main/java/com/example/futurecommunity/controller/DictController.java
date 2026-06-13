package com.example.futurecommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.futurecommunity.model.Dict;
import com.example.futurecommunity.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 数据字典控制器 - 系统管理端
 */
@RestController
@RequestMapping("/api/admin/dict")
public class DictController {

    @Autowired
    private DictService dictService;

    /**
     * 分页查询数据字典
     * 仅管理员可以访问
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/page")
    public Page<Dict> getDictsByPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type) {
        
        // 创建分页对象
        Page<Dict> dictPage = new Page<>(page, size);
        
        // 构建查询条件
        LambdaQueryWrapper<Dict> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加类型筛选条件
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(Dict::getType, type);
        }
        
        // 执行分页查询
        dictService.page(dictPage, queryWrapper);
        
        return dictPage;
    }

    /**
     * 新增字典项
     * 仅管理员可以访问
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Dict createDict(@RequestBody Dict dict) {
        // 保存字典项
        dictService.save(dict);
        return dict;
    }

    /**
     * 修改字典项
     * 仅管理员可以访问
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Dict updateDict(@PathVariable Long id, @RequestBody Dict dict) {
        // 设置ID
        dict.setId(id);
        
        // 更新字典项
        dictService.updateById(dict);
        
        // 查询更新后的字典项
        return dictService.getById(id);
    }

    /**
     * 删除字典项
     * 仅管理员可以访问
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteDict(@PathVariable Long id) {
        dictService.removeById(id);
    }
}