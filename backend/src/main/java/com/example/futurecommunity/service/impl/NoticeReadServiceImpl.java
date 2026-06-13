package com.example.futurecommunity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.futurecommunity.mapper.NoticeReadMapper;
import com.example.futurecommunity.model.NoticeRead;
import com.example.futurecommunity.service.NoticeReadService;
import org.springframework.stereotype.Service;

@Service
public class NoticeReadServiceImpl extends ServiceImpl<NoticeReadMapper, NoticeRead> implements NoticeReadService {
}
