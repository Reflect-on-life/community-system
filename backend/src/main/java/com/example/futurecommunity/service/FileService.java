package com.example.futurecommunity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

/**
 * 文件服务类
 * 用于保存告警截图
 */
@Slf4j
@Service
public class FileService {

    @Value("${upload.alarm-image-path:./alarm-images/}")
    private String alarmImagePath;

    /**
     * 保存告警图片
     * 
     * @param base64Image Base64 编码的图片
     * @return 文件相对路径
     */
    public String saveAlarmImage(String base64Image) {
        try {
            // 1. 清理 Base64 前缀
            String cleanBase64 = cleanBase64Image(base64Image);
            
            // 2. 解码图片
            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
            
            // 3. 生成文件名
            String fileName = generateFileName();
            
            // 4. 确保目录存在
            File dir = new File(alarmImagePath);
            if (!dir.exists()) {
                dir.mkdirs();
                log.info("创建告警图片目录：{}", dir.getAbsolutePath());
            }
            
            // 5. 保存图片
            Path filePath = Paths.get(alarmImagePath, fileName);
            Files.write(filePath, imageBytes);
            
            log.info("告警图片保存成功：{}", filePath.toAbsolutePath());
            
            // 6. 返回访问路径（相对于静态资源映射路径）
            return "/monitor/" + fileName;
            
        } catch (IOException e) {
            log.error("保存告警图片失败", e);
            throw new RuntimeException("保存图片失败：" + e.getMessage());
        }
    }

    /**
     * 清理 Base64 图片前缀
     */
    private String cleanBase64Image(String base64Image) {
        if (base64Image.contains(",")) {
            return base64Image.substring(base64Image.indexOf(",") + 1);
        }
        return base64Image;
    }

    /**
     * 生成文件名
     * 格式：alarm_yyyyMMdd_HHmmss_随机 4 位数字.jpg
     */
    private String generateFileName() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String random = String.valueOf((int)(Math.random() * 10000));
        // 补齐 4 位
        while (random.length() < 4) {
            random = "0" + random;
        }
        return "alarm_" + timestamp + "_" + random + ".jpg";
    }
}
