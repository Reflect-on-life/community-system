package com.example.futurecommunity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.futurecommunity.mapper.HouseMapper;
import com.example.futurecommunity.model.House;
import com.example.futurecommunity.service.HouseService;
import org.springframework.stereotype.Service;

@Service
public class HouseServiceImpl extends ServiceImpl<HouseMapper, House> implements HouseService {
}
