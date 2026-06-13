package com.example.futurecommunity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.futurecommunity.mapper.HealthReminderMapper;
import com.example.futurecommunity.model.HealthReminder;
import com.example.futurecommunity.service.HealthReminderService;
import org.springframework.stereotype.Service;

@Service
public class HealthReminderServiceImpl extends ServiceImpl<HealthReminderMapper, HealthReminder> implements HealthReminderService {
}
