package com.example.futurecommunity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.futurecommunity.mapper.HealthEducationMapper;
import com.example.futurecommunity.model.HealthEducation;
import com.example.futurecommunity.service.HealthEducationService;
import org.springframework.stereotype.Service;

@Service
public class HealthEducationServiceImpl extends ServiceImpl<HealthEducationMapper, HealthEducation> implements HealthEducationService {
}
