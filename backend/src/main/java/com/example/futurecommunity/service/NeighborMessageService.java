package com.example.futurecommunity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.futurecommunity.model.NeighborMessage;

public interface NeighborMessageService extends IService<NeighborMessage> {
    Page<NeighborMessage> getMessageList(int page, int size);
    Page<NeighborMessage> getMessageList(int page, int size, Long userId);
    NeighborMessage getMessageById(Long id);
    NeighborMessage createMessage(Long userId, String title, String content);
    void incrementReplyCount(Long messageId);
    void toggleLike(Long messageId, Long userId);
    boolean isLiked(Long messageId, Long userId);
}
