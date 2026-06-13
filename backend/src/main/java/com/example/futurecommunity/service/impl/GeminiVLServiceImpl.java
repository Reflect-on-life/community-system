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
 * Gemini 2.5 Flash 视觉服务实现
 * 通过 Google Generative AI API 调用
 */
@Slf4j
@Service
public class GeminiVLServiceImpl implements VisionService {

    private final ModelPresetConfig presetConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String DEFAULT_PROMPT =
        "请仔细分析这张社区图片，判断是否存在以下安全风险，只回答实际观察到的内容：\n" +
        "1. 老人摔倒（有人躺在地上、摔倒、倒地不起）\n" +
        "2. 火灾烟雾（明火、浓烟、燃烧）\n" +
        "3. 垃圾堆积（垃圾桶外大量垃圾、杂物堆积）\n" +
        "4. 电动车违规充电（楼道、室内充电）\n" +
        "5. 路面积水（大量积水）\n" +
        "6. 设施损坏（公共设施损坏）\n" +
        "要求：只回答实际存在的风险类型，如无风险则回‘无明显安全风险'。";

    public GeminiVLServiceImpl(ModelPresetConfig presetConfig) {
        this.presetConfig = presetConfig;
    }

    @Override
    public String analyzeImage(String imageUrl) {
        return analyzeImageWithPrompt(imageUrl, DEFAULT_PROMPT);
    }

    @Override
    public String analyzeImageWithPrompt(String imageUrl, String customPrompt) {
        try {
            log.info("调用 Gemini 2.5 Flash 分析图片: {}", imageUrl);

            String base64Image = imageToBase64(imageUrl);
            if (base64Image == null || base64Image.isEmpty()) {
                log.error("图片转换 Base64 失败");
                return "";
            }

            var config = presetConfig.getGemini();
            String apiUrl = config.getBaseUrl() + "/models/" + config.getModel()
                + ":generateContent?key=" + config.getApiKey();

            Map<String, Object> requestBody = new HashMap<>();

            Map<String, Object> inlineData = new HashMap<>();
            inlineData.put("mime_type", "image/jpeg");
            inlineData.put("data", base64Image);

            Map<String, Object> imagePart = new HashMap<>();
            imagePart.put("inline_data", inlineData);

            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", customPrompt);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(textPart, imagePart));

            requestBody.put("contents", List.of(content));

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("maxOutputTokens", 1000);
            generationConfig.put("temperature", 0.2);
            requestBody.put("generationConfig", generationConfig);

            String requestJson = objectMapper.writeValueAsString(requestBody);

            URL url = new URI(apiUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
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

            log.debug("Gemini 响应状态: {}, 内容: {}", responseCode, responseStr);

            JsonNode root = objectMapper.readTree(responseStr);
            if (root.has("candidates") && root.get("candidates").size() > 0) {
                JsonNode candidate = root.get("candidates").get(0);
                if (candidate.has("content") && candidate.get("content").has("parts")) {
                    JsonNode parts = candidate.get("content").get("parts");
                    if (parts.size() > 0 && parts.get(0).has("text")) {
                        String text = parts.get(0).get("text").asText();
                        log.info("Gemini 结果: {}", text);
                        return text;
                    }
                }
            }

            if (root.has("error")) {
                log.error("Gemini API 错误: {}", root.get("error").get("message").asText());
            }

            return "";

        } catch (Exception e) {
            log.error("Gemini 调用失败: {}", e.getMessage(), e);
            return "";
        }
    }

    @Override
    public String getServiceName() {
        return "Gemini 2.5 Flash";
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
