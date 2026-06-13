package com.example.futurecommunity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.futurecommunity.model.HealthRecord;

import java.util.List;

public interface HealthRecordService extends IService<HealthRecord> {
    // 录入居民体检数据
    HealthRecord addHealthRecord(HealthRecord healthRecord);
    
    // 根据用户ID获取健康档案
    List<HealthRecord> getHealthRecordsByUserId(Long userId);
    
    // 获取重点人群列表（高血压/糖尿病）
    List<HealthRecord> getKeyPersons();
}
