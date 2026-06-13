package com.example.futurecommunity.service.impl;

import com.example.futurecommunity.config.GlmConfig;
import com.example.futurecommunity.service.GLMService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GLM AI 图像识别服务实现类
 * 调用 GLM-4V 视觉理解模型分析社区安全风险
 */
@Slf4j
@Service
public class GLMServiceImpl implements GLMService {
    
    private final GlmConfig glmConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 默认提示词 - 要求直接给出结论，重点强调老人摔倒和垃圾堆积的识别
    private static final String DEFAULT_PROMPT = "请仔细分析这张社区图片，判断是否存在以下安全风险：\n\n1. 老人摔倒（有人躺在地上、摔倒、倒地不起，特别是老人）\n2. 火灾烟雾（有明火、浓烟、燃烧）\n3. 垃圾堆积（垃圾桶外有大量垃圾、杂物、塑料袋堆积）\n4. 电动车违规充电（电动车在楼道、室内充电）\n5. 路面积水（路面有大量积水）\n6. 设施损坏（公共设施、器材损坏）\n\n要求:\n1. 首先检查是否有人（尤其是老人）躺在地上或摔倒，这是最重要的\n2. 其次检查是否有垃圾、杂物、塑料袋堆积在地面或垃圾桶周围\n3. 只回答存在的风险类型，不要列举未出现的风险\n4. 如果存在风险，直接回答：'存在【风险类型】,具体情况是...'\n5. 如果确实没有任何风险，再回答'无明显安全风险'\n\n图片内容分析:";
    
    public GLMServiceImpl(GlmConfig glmConfig) {
        this.glmConfig = glmConfig;
    }
    
    @Override
    public String analyzeImage(String imageUrl) {
        return analyzeImageWithPrompt(imageUrl, DEFAULT_PROMPT);
    }
    
    @Override
    public String analyzeImageWithPrompt(String imageUrl, String customPrompt) {
        try {
            log.info("开始调用 GLM-4V 分析图片：{}", imageUrl);
            
            // 将图片转换为 Base64
            String base64Image = imageToBase64(imageUrl);
            if (base64Image == null || base64Image.isEmpty()) {
                log.error("图片转换 Base64 失败");
                return "";
            }
            log.info("图片 Base64 长度：{}", base64Image.length());
            
            // 构建请求
            String apiUrl = glmConfig.getBaseUrl() + "/chat/completions";
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", glmConfig.getModel());
            
            // 构建消息 - 使用标准的 OpenAI 多模态格式
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            
            // 使用数组格式，但尝试不同的字段名
            List<Object> contentList = new ArrayList<>();
            
            // 文本部分
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("type", "text");
            textPart.put("text", customPrompt);
            contentList.add(textPart);
            
            // 图片部分 - 尝试不带 data:image/png;base64 前缀
            Map<String, Object> imagePart = new HashMap<>();
            imagePart.put("type", "image_url");
            Map<String, String> imageUrlObj = new HashMap<>();
            imageUrlObj.put("url", "data:image/png;base64," + base64Image);
            imagePart.put("image_url", imageUrlObj);
            contentList.add(imagePart);
            
            userMessage.put("content", contentList);
            
            requestBody.put("messages", new Object[]{userMessage});
            
            log.info("发送请求到：{}", apiUrl);
            log.info("请求模型：{}", glmConfig.getModel());
            
            // 发送 HTTP 请求
            String response = sendHttpPost(apiUrl, requestBody);
            
            // 打印完整响应（前500字符）
            if (response != null && !response.isEmpty()) {
                log.info("GLM API 原始响应（长度={}）：{}", response.length(), 
                    response.substring(0, Math.min(500, response.length())));
            } else {
                log.error("GLM API 返回空响应！URL: {}", apiUrl);
                return "GLM API 调用失败：返回空响应";
            }
            
            // 解析响应
            String result = parseResponse(response);
            
            log.info("GLM-4V 分析结果：{}", result != null ? result : "(空)");
            return result;
            
        } catch (Exception e) {
            log.error("GLM-4V 调用失败：{}", e.getMessage(), e);
            return "";
        }
    }
    
    /**
     * 将图片转换为 Base64
     */
    private String imageToBase64(String imagePath) {
        try {
            // 如果是本地文件路径
            if (imagePath.startsWith("c:") || imagePath.startsWith("/")) {
                byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
                return Base64.getEncoder().encodeToString(imageBytes);
            }
            // 如果是 URL，先下载再转换
            else if (imagePath.startsWith("http")) {
                URL url = new URL(imagePath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(10000);
                
                try (InputStream inputStream = connection.getInputStream();
                     ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    return Base64.getEncoder().encodeToString(outputStream.toByteArray());
                }
            }
        } catch (Exception e) {
            log.error("图片转 Base64 失败：{}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 发送 HTTP POST 请求
     */
    private String sendHttpPost(String apiUrl, Map<String, Object> requestBody) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + glmConfig.getApiKey());
        connection.setDoOutput(true);
        
        // 发送请求体
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        
        // 读取响应
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                connection.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        
        return response.toString();
    }
    
    /**
     * 解析 GLM 响应
     */
    private String parseResponse(String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            
            if (rootNode.has("choices") && rootNode.get("choices").isArray() && rootNode.get("choices").size() > 0) {
                JsonNode firstChoice = rootNode.get("choices").get(0);
                
                if (firstChoice.has("message")) {
                    JsonNode message = firstChoice.get("message");
                    if (message.has("content")) {
                        return message.get("content").asText();
                    }
                }
            }
            
            // 如果有错误信息
            if (rootNode.has("error")) {
                JsonNode error = rootNode.get("error");
                String errorMsg = error.has("message") ? error.get("message").asText() : "未知错误";
                log.error("GLM API 错误：{}", errorMsg);
                return "AI 分析失败：" + errorMsg;
            }
            
        } catch (Exception e) {
            log.error("解析 GLM 响应失败：{}", e.getMessage());
        }
        
        return "";
    }
}
