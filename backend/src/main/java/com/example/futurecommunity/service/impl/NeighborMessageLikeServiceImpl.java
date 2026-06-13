package com.example.futurecommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.futurecommunity.mapper.NeighborMessageLikeMapper;
import com.example.futurecommunity.model.NeighborMessageLike;
import com.example.futurecommunity.service.NeighborMessageLikeService;
import org.springframework.stereotype.Service;

@Service
public class NeighborMessageLikeServiceImpl extends ServiceImpl<NeighborMessageLikeMapper, NeighborMessageLike> implements NeighborMessageLikeService {
    @Override
    public boolean isLiked(Long messageId, Long userId) {
        LambdaQueryWrapper<NeighborMessageLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NeighborMessageLike::getMessageId, messageId)
                .eq(NeighborMessageLike::getUserId, userId);
        return count(queryWrapper) > 0;
    }

    @Override
    public void toggleLike(Long messageId, Long userId) {
        LambdaQueryWrapper<NeighborMessageLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NeighborMessageLike::getMessageId, messageId)
                .eq(NeighborMessageLike::getUserId, userId);
        NeighborMessageLike like = getOne(queryWrapper);
        if (like != null) {
            removeById(like.getId());
        } else {
            NeighborMessageLike newLike = new NeighborMessageLike();
            newLike.setMessageId(messageId);
            newLike.setUserId(userId);
            save(newLike);
        }
    }
}
