package com.example.futurecommunity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.futurecommunity.model.NeighborMessageLike;

public interface NeighborMessageLikeService extends IService<NeighborMessageLike> {
    boolean isLiked(Long messageId, Long userId);
    void toggleLike(Long messageId, Long userId);
}
