package com.example.futurecommunity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.futurecommunity.mapper.ActivityPhotoMapper;
import com.example.futurecommunity.model.ActivityPhoto;
import com.example.futurecommunity.service.ActivityPhotoService;
import org.springframework.stereotype.Service;

@Service
public class ActivityPhotoServiceImpl extends ServiceImpl<ActivityPhotoMapper, ActivityPhoto> implements ActivityPhotoService {
}
