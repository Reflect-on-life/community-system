package com.example.futurecommunity.service.impl;

import com.example.futurecommunity.config.ModelPresetConfig;
import com.example.futurecommunity.service.VisionService;
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
 * Qwen-VL（通义千问视觉）服务实现
 * 通过阿里云 DashScope API 调用
 */
@Slf4j
@Service
public class QwenVLServiceImpl implements VisionService {

    private final ModelPresetConfig presetConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String DEFAULT_PROMPT =
        "请仔细分析这张社区图片，判断是否存在以下安全风险：\n" +
        "1. 老人摔倒（有人躺在地上、摔倒、倒地不起，特别是老人）\n" +
        "2. 火灾烟雾（有明火、浓烟、燃烧）\n" +
        "3. 垃圾堆积（垃圾桶外有大量垃圾、杂物、塑料袋堆积）\n" +
        "4. 电动车违规充电（电动车在楼道、室内充电）\n" +
        "5. 路面积水（路面有大量积水）\n" +
        "6. 设施损坏（公共设施、器材损坏）\n" +
        "要求：只回答存在的风险类型，不要列举未出现的风险。直接给出结论。";

    public QwenVLServiceImpl(ModelPresetConfig presetConfig) {
        this.presetConfig = presetConfig;
    }

    @Override
    public String analyzeImage(String imageUrl) {
        return analyzeImageWithPrompt(imageUrl, DEFAULT_PROMPT);
    }

    @Override
    public String analyzeImageWithPrompt(String imageUrl, String customPrompt) {
        try {
            log.info("调用 Qwen-VL 分析图片: {}", imageUrl);

            String base64Image = imageToBase64(imageUrl);
            if (base64Image == null || base64Image.isEmpty()) {
                log.error("图片转换 Base64 失败");
                return "";
            }

            var config = presetConfig.getQwenVl();
            String apiUrl = config.getBaseUrl() + "/chat/completions";
            String apiKey = config.getApiKey();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModel());

            List<Map<String, Object>> contentList = new ArrayList<>();

            Map<String, Object> imagePart = new HashMap<>();
            imagePart.put("type", "image_url");
            Map<String, String> imageUrlObj = new HashMap<>();
            imageUrlObj.put("url", "data:image/jpeg;base64," + base64Image);
            imagePart.put("image_url", imageUrlObj);
            contentList.add(imagePart);

            Map<String, Object> textPart = new HashMap<>();
            textPart.put("type", "text");
            textPart.put("text", customPrompt);
            contentList.add(textPart);

            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", contentList);

            requestBody.put("messages", List.of(userMessage));
            requestBody.put("max_tokens", 1000);

            String requestJson = objectMapper.writeValueAsString(requestBody);
            log.debug("Qwen-VL 请求: {}", requestJson);

            URL url = new URI(apiUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestJson.getBytes("UTF-8"));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            InputStream is = (responseCode >= 200 && responseCode < 300)
                ? conn.getInputStream() : conn.getErrorStream();
            String responseStr = new String(is.readAllBytes(), "UTF-8");
            conn.disconnect();

            log.debug("Qwen-VL 响应: {}", responseStr);

            JsonNode root = objectMapper.readTree(responseStr);
            if (root.has("choices") && root.get("choices").size() > 0) {
                JsonNode choice = root.get("choices").get(0);
                if (choice.has("message") && choice.get("message").has("content")) {
                    String content = choice.get("message").get("content").asText();
                    log.info("Qwen-VL 结果: {}", content);
                    return content;
                }
            }

            log.warn("Qwen-VL 未返回有效结果: {}", responseStr);
            return "";

        } catch (Exception e) {
            log.error("Qwen-VL 调用失败: {}", e.getMessage(), e);
            return "";
        }
    }

    @Override
    public String getServiceName() {
        return "Qwen-VL (通义千问)";
    }

    private String imageToBase64(String imageUrl) throws IOException {
        byte[] imageBytes;
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            URL url = new URI(imageUrl).toURL();
            try (InputStream is = url.openStream()) {
                imageBytes = is.readAllBytes();
            }
        } else {
            imageBytes = Files.readAllBytes(Paths.get(imageUrl));
        }
        return Base64.getEncoder().encodeToString(imageBytes);
    }
}
