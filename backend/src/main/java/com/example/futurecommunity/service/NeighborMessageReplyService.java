package com.example.futurecommunity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.futurecommunity.model.NeighborMessageReply;

import java.util.List;

public interface NeighborMessageReplyService extends IService<NeighborMessageReply> {
    List<NeighborMessageReply> getRepliesByMessageId(Long messageId);
}
