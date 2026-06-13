package com.example.futurecommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.futurecommunity.mapper.HealthRecordMapper;
import com.example.futurecommunity.model.HealthRecord;
import com.example.futurecommunity.service.HealthRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HealthRecordServiceImpl extends ServiceImpl<HealthRecordMapper, HealthRecord> implements HealthRecordService {
    @Override
    public HealthRecord addHealthRecord(HealthRecord healthRecord) {
        // 保存健康档案
        save(healthRecord);
        return healthRecord;
    }
    
    @Override
    public List<HealthRecord> getHealthRecordsByUserId(Long userId) {
        // 根据用户ID查询健康档案
        LambdaQueryWrapper<HealthRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HealthRecord::getUserId, userId);
        return list(queryWrapper);
    }
    
    @Override
    public List<HealthRecord> getKeyPersons() {
        // 查询重点人群（高血压/糖尿病）
        LambdaQueryWrapper<HealthRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(HealthRecord::getChronicDiseaseTypeId, 1, 2); // 1-高血压，2-糖尿病
        return list(queryWrapper);
    }
}
