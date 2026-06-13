package com.example.futurecommunity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.futurecommunity.mapper.RiskEventMapper;
import com.example.futurecommunity.model.RiskEvent;
import com.example.futurecommunity.service.RiskEventService;
import org.springframework.stereotype.Service;

@Service
public class RiskEventServiceImpl extends ServiceImpl<RiskEventMapper, RiskEvent> implements RiskEventService {
}