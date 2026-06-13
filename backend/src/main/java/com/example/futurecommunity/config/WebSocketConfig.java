package com.example.futurecommunity.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 配置类
 * 开启 STOMP over WebSocket，支持实时消息推送
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 开启简单的内存消息代理
        // 客户端订阅 /topic 和 /queue 开头的目的地
        registry.enableSimpleBroker("/topic", "/queue");
        
        // 设置应用目标前缀
        // 客户端发送消息到 /app 开头的目的地，由 @MessageMapping 注解的方法处理
        registry.setApplicationDestinationPrefixes("/app");
        
        // 设置用户目的地前缀（可选，用于点对点消息）
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * 注册 STOMP 端点
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册 WebSocket 端点，客户端通过这个端点连接 WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 允许所有来源跨域
                .withSockJS(); // 启用 SockJS 回退支持
        
        // 也注册一个不带 SockJS 的端点（原生 WebSocket）
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    // 注意：CORS 配置已在 WebConfig.java 中统一配置
    // WebSocket 的跨域通过 setAllowedOriginPatterns("*") 处理
}
