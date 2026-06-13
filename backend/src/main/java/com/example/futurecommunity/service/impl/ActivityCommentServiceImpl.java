package com.example.futurecommunity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.futurecommunity.mapper.ActivityCommentMapper;
import com.example.futurecommunity.model.ActivityComment;
import com.example.futurecommunity.service.ActivityCommentService;
import org.springframework.stereotype.Service;

@Service
public class ActivityCommentServiceImpl extends ServiceImpl<ActivityCommentMapper, ActivityComment> implements ActivityCommentService {
}
