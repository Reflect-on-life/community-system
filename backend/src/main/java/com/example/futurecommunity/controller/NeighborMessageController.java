package com.example.futurecommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.futurecommunity.model.NeighborMessage;
import com.example.futurecommunity.model.NeighborMessageReply;
import com.example.futurecommunity.model.User;
import com.example.futurecommunity.service.NeighborMessageLikeService;
import com.example.futurecommunity.service.NeighborMessageReplyService;
import com.example.futurecommunity.service.NeighborMessageService;
import com.example.futurecommunity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/neighbor-messages")
public class NeighborMessageController {
    @Autowired
    private NeighborMessageService neighborMessageService;

    @Autowired
    private NeighborMessageReplyService neighborMessageReplyService;

    @Autowired
    private NeighborMessageLikeService neighborMessageLikeService;

    @Autowired
    private UserService userService;

    /**
     * 获取消息列表（分页）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMessageList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId) {
        Page<NeighborMessage> messagePage;
        if (userId != null) {
            messagePage = neighborMessageService.getMessageList(page, size, userId);
        } else {
            messagePage = neighborMessageService.getMessageList(page, size);
        }
        
        // 为每个消息添加发布者的姓名和头像信息
        List<Map<String, Object>> recordsWithUserInfo = new ArrayList<>();
        for (NeighborMessage message : messagePage.getRecords()) {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("id", message.getId());
            messageMap.put("userId", message.getUserId());
            messageMap.put("title", message.getTitle());
            messageMap.put("content", message.getContent());
            messageMap.put("replyCount", message.getReplyCount());
            messageMap.put("likeCount", message.getLikeCount());
            messageMap.put("createdAt", message.getCreatedAt());
            
            // 查询发布者信息
            User user = userService.getById(message.getUserId());
            if (user != null) {
                messageMap.put("userName", user.getName());
                messageMap.put("avatar", user.getAvatar());
            }
            
            recordsWithUserInfo.add(messageMap);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", recordsWithUserInfo);
        result.put("total", messagePage.getTotal());
        return ResponseEntity.ok(result);
    }

    /**
     * 发布新帖
     */
    @PostMapping
    public ResponseEntity<NeighborMessage> createMessage(@RequestBody Map<String, Object> request) {
        Long userId = Long.parseLong(request.get("userId").toString());
        String title = request.get("title").toString();
        String content = request.get("content").toString();
        NeighborMessage message = neighborMessageService.createMessage(userId, title, content);
        return ResponseEntity.ok(message);
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMessageById(@PathVariable Long id) {
        NeighborMessage message = neighborMessageService.getMessageById(id);
        
        // 为帖子添加发布者的姓名和头像信息
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("id", message.getId());
        messageMap.put("userId", message.getUserId());
        messageMap.put("title", message.getTitle());
        messageMap.put("content", message.getContent());
        messageMap.put("replyCount", message.getReplyCount());
        messageMap.put("likeCount", message.getLikeCount());
        messageMap.put("createdAt", message.getCreatedAt());
        
        // 查询发布者信息
        User user = userService.getById(message.getUserId());
        if (user != null) {
            messageMap.put("userName", user.getName());
            messageMap.put("avatar", user.getAvatar());
        }
        
        return ResponseEntity.ok(messageMap);
    }

    /**
     * 获取评论列表
     */
    @GetMapping("/{id}/replies")
    public ResponseEntity<List<Map<String, Object>>> getRepliesByMessageId(@PathVariable Long id) {
        List<NeighborMessageReply> replies = neighborMessageReplyService.getRepliesByMessageId(id);
        
        // 为每个评论添加发布者的姓名和头像信息
        List<Map<String, Object>> repliesWithUserInfo = new ArrayList<>();
        for (NeighborMessageReply reply : replies) {
            Map<String, Object> replyMap = new HashMap<>();
            replyMap.put("id", reply.getId());
            replyMap.put("messageId", reply.getMessageId());
            replyMap.put("userId", reply.getUserId());
            replyMap.put("content", reply.getContent());
            replyMap.put("createdAt", reply.getCreatedAt());
            
            // 查询发布者信息
            User user = userService.getById(reply.getUserId());
            if (user != null) {
                replyMap.put("userName", user.getName());
                replyMap.put("avatar", user.getAvatar());
            }
            
            repliesWithUserInfo.add(replyMap);
        }
        
        return ResponseEntity.ok(repliesWithUserInfo);
    }

    /**
     * 发表评论
     */
    @PostMapping("/{id}/reply")
    public ResponseEntity<NeighborMessageReply> createReply(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        Long userId = Long.parseLong(request.get("userId").toString());
        String content = request.get("content").toString();
        NeighborMessageReply reply = new NeighborMessageReply();
        reply.setMessageId(id);
        reply.setUserId(userId);
        reply.setContent(content);
        neighborMessageReplyService.save(reply);
        neighborMessageService.incrementReplyCount(id);
        return ResponseEntity.ok(reply);
    }

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        Long userId = Long.parseLong(request.get("userId").toString());
        neighborMessageService.toggleLike(id, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return ResponseEntity.ok(result);
    }

    /**
     * 检查用户是否已经点赞
     */
    @GetMapping("/{id}/is-liked")
    public ResponseEntity<Map<String, Object>> checkIsLiked(
            @PathVariable Long id,
            @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("liked", neighborMessageService.isLiked(id, userId));
        return ResponseEntity.ok(result);
    }

    /**
     * 修改帖子
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateMessage(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        NeighborMessage message = neighborMessageService.getMessageById(id);
        if (message == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "帖子不存在");
            return ResponseEntity.status(404).body(result);
        }
        if (request.containsKey("title")) {
            message.setTitle(request.get("title").toString());
        }
        if (request.containsKey("content")) {
            message.setContent(request.get("content").toString());
        }
        neighborMessageService.updateById(message);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "修改成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMessage(@PathVariable Long id) {
        neighborMessageReplyService.remove(
            new LambdaQueryWrapper<com.example.futurecommunity.model.NeighborMessageReply>()
                .eq(com.example.futurecommunity.model.NeighborMessageReply::getMessageId, id)
        );
        neighborMessageLikeService.remove(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.example.futurecommunity.model.NeighborMessageLike>()
                .eq(com.example.futurecommunity.model.NeighborMessageLike::getMessageId, id)
        );
        neighborMessageService.removeById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "删除成功");
        return ResponseEntity.ok(result);
    }
}
