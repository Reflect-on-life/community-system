package com.example.futurecommunity.service.impl;

import com.example.futurecommunity.model.BehaviorDetectionResult;
import com.example.futurecommunity.service.BehaviorDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 行为识别服务实现类
 * 基于百度云人体分析 API 实现打架、发传单等行为识别
 */
@Slf4j
@Service
public class BehaviorDetectionServiceImpl implements BehaviorDetectionService {

    @Value("${baidu.ai.api-key}")
    private String apiKey;

    @Value("${baidu.ai.secret-key}")
    private String secretKey;

    /**
     * access_token 缓存（本地变量缓存）
     * 格式：{ "token": "xxx", "expireTime": 时间戳 }
     */
    private final Map<String, Object> tokenCache = new ConcurrentHashMap<>();

    /**
     * 调用频率限制计数器
     * 格式：{ "timestamp": 时间戳，"count": 次数 }
     */
    private final Map<String, Object> rateLimitCounter = new ConcurrentHashMap<>();

    /**
     * 并发请求控制标志
     * true 表示当前有请求正在处理，需要跳过新请求
     */
    private volatile boolean isProcessing = false;

    /**
     * 打架检测 IoU 阈值（可从配置读取）
     */
    @Value("${detection.fighting-iou-threshold:0.1}")
    private double fightingIouThreshold;

    /**
     * 最低置信度过滤（可从配置读取）
     */
    @Value("${detection.min-confidence:0.6}")
    private double minConfidence;

    /**
     * 百度云人体分析接口地址
     */
    private static final String BODY_ANALYSIS_URL = 
        "https://aip.baidubce.com/rest/2.0/image-classify/v1/body_analysis";

    /**
     * access_token 获取接口地址
     */
    private static final String TOKEN_URL = 
        "https://aip.baidubce.com/oauth/2.0/token";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public BehaviorDetectionResult detectBehavior(String base64Image) {
        try {
            // 1. 检查调用频率限制
            if (!checkRateLimit()) {
                log.warn("QPS 超限，跳过本次检测");
                return BehaviorDetectionResult.error("调用频率超限，请稍后重试");
            }

            // 2. 设置并发标志
            isProcessing = true;

            // 3. 调用百度云人体分析接口
            JSONObject response = callBodyAnalysisApi(base64Image);
            
            if (response == null) {
                isProcessing = false;
                return BehaviorDetectionResult.error("调用百度 AI 接口失败");
            }

            // 4. 解析识别结果
            BehaviorDetectionResult result = parseResponse(response);
            result.setRawResult(response.toString());

            // 5. 行为判断
            if (result.getPersonNum() != null && result.getPersonNum() >= 0) {
                log.info("检测到人数：{}, 开始行为分析", result.getPersonNum());
                
                // 打架检测
                if (isFighting(result)) {
                    log.info("✅ 检测到打架行为！");
                    isProcessing = false;
                    return BehaviorDetectionResult.alarm("打架", result.getConfidence());
                }
                
                // 发传单检测
                if (isDistributingFlyers(result)) {
                    log.info("✅ 检测到发传单行为！");
                    isProcessing = false;
                    return BehaviorDetectionResult.alarm("发传单", result.getConfidence());
                }
                
                log.debug("场景正常，无异常行为");
            } else {
                log.warn("未检测到有效人数，person_num={}", result.getPersonNum());
            }

            // 6. 无异常行为
            result.setAlarm(false);
            result.setAlarmType("无");
            result.setConfidence(0.0);
            isProcessing = false;
            
            return result;

        } catch (Exception e) {
            log.error("行为识别异常", e);
            isProcessing = false;
            return BehaviorDetectionResult.error("识别异常：" + e.getMessage());
        }
    }

    @Override
    public BehaviorDetectionResult detectBehaviorByPath(String imagePath) {
        // 将图片文件转为 Base64 后调用
        try {
            byte[] imageBytes = java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get(imagePath)
            );
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            return detectBehavior(base64Image);
        } catch (Exception e) {
            log.error("读取图片文件失败：{}", imagePath, e);
            return BehaviorDetectionResult.error("读取图片文件失败：" + e.getMessage());
        }
    }

    /**
     * 检查调用频率限制（简单计数器限流 + 并发控制）
     */
    private boolean checkRateLimit() {
        // 1. 检查是否有并发请求正在处理
        if (isProcessing) {
            log.debug("上一次请求还未返回，跳过本次检测");
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - 1000; // 1 秒窗口
        
        synchronized (rateLimitCounter) {
            Long count = (Long) rateLimitCounter.get("count");
            Long timestamp = (Long) rateLimitCounter.get("timestamp");
            
            if (timestamp == null || timestamp < windowStart) {
                // 重置计数器
                rateLimitCounter.put("count", 1L);
                rateLimitCounter.put("timestamp", currentTime);
                return true;
            }
            
            if (count != null && count >= 1) {
                // 超过 1 秒 1 次，限流
                return false;
            }
            
            rateLimitCounter.put("count", count + 1);
            return true;
        }
    }

    /**
     * 调用百度云人体分析 API
     */
    private JSONObject callBodyAnalysisApi(String base64Image) {
        try {
            // 1. 获取 access_token
            String accessToken = getAccessToken();
            if (accessToken == null) {
                log.error("获取 access_token 失败");
                return null;
            }

            // 2. 构建请求 URL
            String url = BODY_ANALYSIS_URL + "?access_token=" + accessToken;

            // 3. 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 4. 构建请求体（图片需要去除 data:image/jpeg;base64, 前缀）
            String cleanBase64 = cleanBase64Image(base64Image);
            String requestBody = "image=" + java.net.URLEncoder.encode(cleanBase64, "UTF-8");

            // 5. 发送 POST 请求
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            // 6. 解析响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JSONObject jsonResponse = new JSONObject(response.getBody());
                log.info("百度 API 返回：person_num={}, response={}", 
                    jsonResponse.optInt("person_num"), 
                    jsonResponse.optString("person_num"));
                return jsonResponse;
            } else {
                log.error("百度 API 返回错误：{}", response.getStatusCode());
                return null;
            }

        } catch (Exception e) {
            log.error("调用人体分析 API 失败", e);
            return null;
        }
    }

    /**
     * 清理 Base64 图片（去除前缀）
     */
    private String cleanBase64Image(String base64Image) {
        if (base64Image.contains(",")) {
            return base64Image.substring(base64Image.indexOf(",") + 1);
        }
        return base64Image;
    }

    /**
     * 获取 access_token（带缓存）
     */
    @Cacheable(value = "baiduToken", key = "'access_token'", unless = "#result == null")
    public String getAccessToken() {
        try {
            // 检查本地缓存
            synchronized (tokenCache) {
                String token = (String) tokenCache.get("token");
                Long expireTime = (Long) tokenCache.get("expireTime");
                
                if (token != null && expireTime != null && 
                    System.currentTimeMillis() < expireTime) {
                    log.debug("使用缓存的 access_token");
                    return token;
                }
            }

            // 1. 构建请求 URL
            String url = TOKEN_URL + 
                "?grant_type=client_credentials" +
                "&client_id=" + apiKey +
                "&client_secret=" + secretKey;

            // 2. 发送 GET 请求
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            // 3. 解析响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JSONObject json = new JSONObject(response.getBody());
                
                if (json.has("access_token")) {
                    String token = json.getString("access_token");
                    // 缓存 29 天（百度官方有效期 30 天，留 1 天缓冲）
                    long expireTime = System.currentTimeMillis() + 29L * 24 * 60 * 60 * 1000;
                    
                    synchronized (tokenCache) {
                        tokenCache.put("token", token);
                        tokenCache.put("expireTime", expireTime);
                    }
                    
                    log.info("获取新的 access_token 成功，有效期 29 天");
                    return token;
                } else {
                    log.error("获取 access_token 失败：{}", json.toString());
                    return null;
                }
            } else {
                log.error("获取 access_token 失败：{}", response.getStatusCode());
                return null;
            }

        } catch (Exception e) {
            log.error("获取 access_token 异常", e);
            return null;
        }
    }

    /**
     * 解析百度 API 响应
     */
    private BehaviorDetectionResult parseResponse(JSONObject response) {
        BehaviorDetectionResult result = new BehaviorDetectionResult();
        
        try {
            // 1. 获取人数
            if (response.has("person_num")) {
                int personNum = response.getInt("person_num");
                result.setPersonNum(personNum);
                log.info("检测到人数：{}", personNum);
            }

            // 2. 获取人体信息
            if (response.has("person_info")) {
                JSONArray personInfoArray = response.getJSONArray("person_info");
                
                if (personInfoArray != null && personInfoArray.length() > 0) {
                    // 解析每个人体信息
                    List<Map<String, Object>> persons = new ArrayList<>();
                    double totalConfidence = 0.0;
                    
                    for (int i = 0; i < personInfoArray.length(); i++) {
                        JSONObject personObj = personInfoArray.getJSONObject(i);
                        Map<String, Object> person = new HashMap<>();
                        
                        // 解析 location（边界框）
                        if (personObj.has("location")) {
                            JSONObject location = personObj.getJSONObject("location");
                            person.put("location", location);
                        }
                        
                        // 解析身体关键点（用于发传单检测）
                        if (personObj.has("body_parts")) {
                            JSONObject bodyParts = personObj.getJSONObject("body_parts");
                            person.put("body_parts", bodyParts);
                        }
                        
                        // 置信度
                        if (personObj.has("score")) {
                            double score = personObj.getDouble("score");
                            person.put("score", score);
                            totalConfidence += score;
                        }
                        
                        persons.add(person);
                    }
                    
                    // 设置平均置信度
                    if (persons.size() > 0) {
                        result.setConfidence(totalConfidence / persons.size());
                    }
                    
                    // 保存解析后的人体信息供后续判断使用
                    result.setRawResult(new JSONArray(persons).toString());
                }
            }

        } catch (Exception e) {
            log.error("解析响应失败", e);
        }
        
        return result;
    }

    /**
     * 打架检测逻辑
     * 检测到 2 人及以上，且人体边界框有重叠（IoU > 阈值）时，判定为打架
     */
    private boolean isFighting(BehaviorDetectionResult result) {
        try {
            // 1. 检查人数
            if (result.getPersonNum() == null || result.getPersonNum() < 2) {
                return false;
            }

            // 2. 解析人体信息
            JSONArray persons = new JSONArray(result.getRawResult());
            if (persons == null || persons.length() < 2) {
                return false;
            }

            // 3. 两两计算 IoU
            for (int i = 0; i < persons.length(); i++) {
                for (int j = i + 1; j < persons.length(); j++) {
                    JSONObject person1 = persons.getJSONObject(i);
                    JSONObject person2 = persons.getJSONObject(j);
                    
                    if (person1.has("location") && person2.has("location")) {
                        JSONObject loc1 = person1.getJSONObject("location");
                        JSONObject loc2 = person2.getJSONObject("location");
                        
                        double iou = calculateIoU(loc1, loc2);
                        log.debug("计算 IoU: person{} 和 person{} = {}", i, j, iou);
                        
                        if (iou > fightingIouThreshold) {
                            log.info("检测到打架行为！IoU={}", iou);
                            return true;
                        }
                    }
                }
            }

            return false;

        } catch (Exception e) {
            log.error("打架检测异常", e);
            return false;
        }
    }

    /**
     * 发传单检测逻辑
     * 检测到 2 人及以上，且至少有 1 人的手部关键点在身体中线附近交叉
     */
    private boolean isDistributingFlyers(BehaviorDetectionResult result) {
        try {
            // 1. 检查人数
            if (result.getPersonNum() == null || result.getPersonNum() < 2) {
                return false;
            }

            // 2. 解析人体信息
            JSONArray persons = new JSONArray(result.getRawResult());
            if (persons == null || persons.length() < 2) {
                return false;
            }

            // 3. 检查每个人的手部关键点
            for (int i = 0; i < persons.length(); i++) {
                JSONObject person = persons.getJSONObject(i);
                
                if (person.has("body_parts")) {
                    JSONObject bodyParts = person.getJSONObject("body_parts");
                    
                    // 检查手腕和手肘关键点
                    boolean hasLeftHand = bodyParts.has("left_hand");
                    boolean hasRightHand = bodyParts.has("right_hand");
                    boolean hasLeftElbow = bodyParts.has("left_elbow");
                    boolean hasRightElbow = bodyParts.has("right_elbow");
                    
                    if (hasLeftHand && hasRightHand) {
                        JSONObject leftHand = bodyParts.getJSONObject("left_hand");
                        JSONObject rightHand = bodyParts.getJSONObject("right_hand");
                        
                        // 简化判断：如果两只手都在身体前方（x 坐标接近），判定为疑似发传单
                        // 注：这里使用简化逻辑，实际项目中可以根据身体中线精确计算
                        double leftX = leftHand.optDouble("x", 0);
                        double rightX = rightHand.optDouble("x", 0);
                        double diff = Math.abs(leftX - rightX);
                        
                        // 如果两手 x 坐标差值较小（在身体中线附近），判定为发传单
                        if (diff < 50) { // 阈值可根据实际情况调整
                            log.info("检测到疑似发传单行为！手部交叉 diff={}", diff);
                            return true;
                        }
                    }
                }
            }

            return false;

        } catch (Exception e) {
            log.error("发传单检测异常", e);
            return false;
        }
    }

    /**
     * 计算两个边界框的 IoU（交并比）
     * 
     * @param loc1 边界框 1 {left, top, width, height}
     * @param loc2 边界框 2 {left, top, width, height}
     * @return IoU 值（0-1）
     */
    private double calculateIoU(JSONObject loc1, JSONObject loc2) {
        try {
            // 1. 获取边界框坐标
            int left1 = loc1.optInt("left", 0);
            int top1 = loc1.optInt("top", 0);
            int width1 = loc1.optInt("width", 0);
            int height1 = loc1.optInt("height", 0);
            
            int left2 = loc2.optInt("left", 0);
            int top2 = loc2.optInt("top", 0);
            int width2 = loc2.optInt("width", 0);
            int height2 = loc2.optInt("height", 0);
            
            // 2. 计算交集区域
            int left = Math.max(left1, left2);
            int top = Math.max(top1, top2);
            int right = Math.min(left1 + width1, left2 + width2);
            int bottom = Math.min(top1 + height1, top2 + height2);
            
            // 3. 判断是否相交
            if (left >= right || top >= bottom) {
                return 0.0;
            }
            
            // 4. 计算交集面积
            int intersectionArea = (right - left) * (bottom - top);
            
            // 5. 计算并集面积
            int area1 = width1 * height1;
            int area2 = width2 * height2;
            int unionArea = area1 + area2 - intersectionArea;
            
            // 6. 计算 IoU
            double iou = (double) intersectionArea / unionArea;
            
            log.debug("IoU 计算：框 1[{}, {}, {}, {}] 框 2[{}, {}, {}, {}] IoU={}", 
                left1, top1, width1, height1, left2, top2, width2, height2, iou);
            
            return iou;
            
        } catch (Exception e) {
            log.error("计算 IoU 失败", e);
            return 0.0;
        }
    }
}
