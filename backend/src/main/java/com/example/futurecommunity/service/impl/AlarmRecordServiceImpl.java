package com.example.futurecommunity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.futurecommunity.mapper.AlarmRecordMapper;
import com.example.futurecommunity.model.AlarmRecord;
import com.example.futurecommunity.service.AlarmRecordService;
import org.springframework.stereotype.Service;

/**
 * 行为告警记录服务实现类
 */
@Service
public class AlarmRecordServiceImpl extends ServiceImpl<AlarmRecordMapper, AlarmRecord> implements AlarmRecordService {
}
