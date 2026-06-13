package com.example.futurecommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.futurecommunity.mapper.NeighborMessageReplyMapper;
import com.example.futurecommunity.model.NeighborMessageReply;
import com.example.futurecommunity.service.NeighborMessageReplyService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NeighborMessageReplyServiceImpl extends ServiceImpl<NeighborMessageReplyMapper, NeighborMessageReply> implements NeighborMessageReplyService {
    @Override
    public List<NeighborMessageReply> getRepliesByMessageId(Long messageId) {
        LambdaQueryWrapper<NeighborMessageReply> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NeighborMessageReply::getMessageId, messageId)
                .orderByAsc(NeighborMessageReply::getCreatedAt);
        return list(queryWrapper);
    }
}
