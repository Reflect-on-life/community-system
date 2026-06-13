package com.example.futurecommunity.service.impl;

import com.example.futurecommunity.config.ModelPresetConfig;
import com.example.futurecommunity.service.YoloDetectorService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * YOLOv11 物体检测服务实现
 * 通过本地 Python Flask 微服务调用
 */
@Slf4j
@Service
public class YoloDetectorServiceImpl implements YoloDetectorService {

    private final ModelPresetConfig presetConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Boolean cachedAvailable = null;

    public YoloDetectorServiceImpl(ModelPresetConfig presetConfig) {
        this.presetConfig = presetConfig;
    }

    @Override
    public List<Map<String, Object>> detect(String imagePath) {
        try {
            String serviceUrl = presetConfig.getYolo().getServiceUrl() + "/detect";
            String base64Image = imageToBase64(imagePath);

            Map<String, String> reqBody = new HashMap<>();
            reqBody.put("image", base64Image);

            String requestJson = objectMapper.writeValueAsString(reqBody);

            URL url = new URI(serviceUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestJson.getBytes("UTF-8"));
                os.flush();
            }

            int code = conn.getResponseCode();
            if (code != 200) {
                log.warn("YOLO 服务返回 {}", code);
                cachedAvailable = false;
                return List.of();
            }

            String responseStr = new String(conn.getInputStream().readAllBytes(), "UTF-8");
            conn.disconnect();

            JsonNode root = objectMapper.readTree(responseStr);
            List<Map<String, Object>> results = new ArrayList<>();

            if (root.has("detections") && root.get("detections").isArray()) {
                for (JsonNode det : root.get("detections")) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("label", det.has("label") ? det.get("label").asText() : "unknown");
                    item.put("confidence", det.has("confidence") ? det.get("confidence").asDouble() : 0.0);
                    results.add(item);
                }
            }

            cachedAvailable = true;
            log.info("YOLO 检测到 {} 个物体", results.size());
            return results;

        } catch (Exception e) {
            log.warn("YOLO 服务调用失败: {}", e.getMessage());
            cachedAvailable = false;
            return List.of();
        }
    }

    @Override
    public String detectSummary(String imagePath) {
        List<Map<String, Object>> detections = detect(imagePath);
        if (detections.isEmpty()) {
            return "YOLO: 未检测到物体";
        }

        StringBuilder sb = new StringBuilder("YOLO检测: ");
        for (Map<String, Object> det : detections) {
            String label = (String) det.get("label");
            Double conf = (Double) det.get("confidence");
            sb.append(label).append("(")
              .append(String.format("%.0f%%", conf * 100)).append(") ");
        }
        return sb.toString().trim();
    }

    @Override
    public boolean isAvailable() {
        if (cachedAvailable != null && cachedAvailable) return true;
        try {
            String healthUrl = presetConfig.getYolo().getServiceUrl() + "/health";
            URL url = new URI(healthUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            int code = conn.getResponseCode();
            conn.disconnect();
            cachedAvailable = (code == 200);
            return cachedAvailable;
        } catch (Exception e) {
            cachedAvailable = false;
            return false;
        }
    }

    private String imageToBase64(String imagePath) throws IOException {
        byte[] imageBytes;
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            URL url = new URI(imagePath).toURL();
            try (InputStream is = url.openStream()) {
                imageBytes = is.readAllBytes();
            }
        } else {
            imageBytes = Files.readAllBytes(Paths.get(imagePath));
        }
        return Base64.getEncoder().encodeToString(imageBytes);
    }
}
