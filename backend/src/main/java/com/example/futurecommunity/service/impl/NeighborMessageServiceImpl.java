package com.example.futurecommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.futurecommunity.mapper.NeighborMessageMapper;
import com.example.futurecommunity.model.NeighborMessage;
import com.example.futurecommunity.service.NeighborMessageLikeService;
import com.example.futurecommunity.service.NeighborMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class NeighborMessageServiceImpl extends ServiceImpl<NeighborMessageMapper, NeighborMessage> implements NeighborMessageService {

    @Autowired
    private NeighborMessageLikeService neighborMessageLikeService;

    @Override
    public Page<NeighborMessage> getMessageList(int page, int size) {
        Page<NeighborMessage> messagePage = new Page<>(page, size);
        LambdaQueryWrapper<NeighborMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(NeighborMessage::getCreatedAt);
        return page(messagePage, queryWrapper);
    }

    @Override
    public Page<NeighborMessage> getMessageList(int page, int size, Long userId) {
        Page<NeighborMessage> messagePage = new Page<>(page, size);
        LambdaQueryWrapper<NeighborMessage> queryWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq(NeighborMessage::getUserId, userId);
        }
        queryWrapper.orderByDesc(NeighborMessage::getCreatedAt);
        return page(messagePage, queryWrapper);
    }

    @Override
    public NeighborMessage getMessageById(Long id) {
        return getById(id);
    }

    @Override
    public NeighborMessage createMessage(Long userId, String title, String content) {
        NeighborMessage message = new NeighborMessage();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setReplyCount(0);
        message.setLikeCount(0);
        message.setCreatedAt(new Date());
        save(message);
        return message;
    }

    @Override
    public void incrementReplyCount(Long messageId) {
        NeighborMessage message = getById(messageId);
        if (message != null) {
            message.setReplyCount(message.getReplyCount() + 1);
            updateById(message);
        }
    }

    @Override
    public void toggleLike(Long messageId, Long userId) {
        boolean isLiked = neighborMessageLikeService.isLiked(messageId, userId);
        neighborMessageLikeService.toggleLike(messageId, userId);

        NeighborMessage message = getById(messageId);
        if (message != null) {
            if (isLiked) {
                message.setLikeCount(Math.max(0, message.getLikeCount() - 1));
            } else {
                message.setLikeCount(message.getLikeCount() + 1);
            }
            updateById(message);
        }
    }

    @Override
    public boolean isLiked(Long messageId, Long userId) {
        return neighborMessageLikeService.isLiked(messageId, userId);
    }
}

