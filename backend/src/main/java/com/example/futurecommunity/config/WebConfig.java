package com.example.futurecommunity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path:./upload}")
    private String uploadPath;

    @Value("${upload.alarm-image-path:./upload/monitor}")
    private String alarmImagePath;

    // 获取绝对路径（解决不同启动方式导致的路径问题）
    private String resolveAbsolutePath(String relativePath) {
        // 如果已经是绝对路径，直接返回
        File file = new File(relativePath);
        if (file.isAbsolute()) {
            return relativePath;
        }
        // 尝试多个可能的基准目录
        Path[] basePaths = {
            Paths.get(System.getProperty("user.dir")),                           // 当前工作目录
            Paths.get(System.getProperty("user.dir"), ".."),                     // 父目录
            Paths.get(System.getProperty("user.dir"), "..", "..")               // 祖父目录
        };
        
        for (Path basePath : basePaths) {
            Path resolved = basePath.resolve(relativePath).normalize();
            if (resolved.toFile().exists()) {
                return resolved.toString();
            }
        }
        
        // 如果都找不到，默认使用当前工作目录
        return Paths.get(System.getProperty("user.dir")).resolve(relativePath).normalize().toString();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 使用绝对路径来配置静态资源映射
        String absoluteUploadPath = resolveAbsolutePath(uploadPath);
        String absoluteAlarmPath = resolveAbsolutePath(alarmImagePath);
        
        // 配置静态资源映射，使 /upload/** 路径可以访问本地文件系统中的图片
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + absoluteUploadPath + "/")
                .setCachePeriod(0);

        // 配置告警图片资源映射，使 /monitor/** 路径可以访问告警截图
        registry.addResourceHandler("/monitor/**")
                .addResourceLocations("file:" + absoluteAlarmPath + "/")
                .setCachePeriod(0);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 为 API 接口添加 CORS 配置，允许前端（5173/3000 端口）访问
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        // 为 WebSocket 添加 CORS 配置
        registry.addMapping("/ws/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        // 为上传目录添加 CORS 配置，允许前端（5173 端口）访问图片资源
        registry.addMapping("/upload/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                .allowedMethods("GET", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        // 为告警图片添加 CORS 配置
        registry.addMapping("/monitor/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                .allowedMethods("GET", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}