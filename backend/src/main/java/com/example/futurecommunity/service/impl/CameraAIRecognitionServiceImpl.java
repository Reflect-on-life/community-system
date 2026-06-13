package com.example.futurecommunity.service.impl;

import com.baidu.aip.imageclassify.AipImageClassify;
import com.example.futurecommunity.config.ModelPresetStrategy;
import com.example.futurecommunity.service.CameraAIRecognitionService;
import com.example.futurecommunity.service.GLMService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 鎽勫儚澶?AI 璇嗗埆鏈嶅姟瀹炵幇绫? * 涓撻棬鐢ㄤ簬鎽勫儚澶寸洃鎺ч〉闈㈢殑 AI 椋庨櫓璇嗗埆
 * 澶嶇敤 GLMService 鍜岀櫨搴?AI 鐨勬牳蹇冭兘鍔涳紝涓嶄緷璧栭闄╀簨浠惰嚜鍔ㄥ垱寤烘湇鍔? * 
 * 閫傜敤鍦烘櫙锛? * 1. 鎽勫儚澶村疄鏃剁洃鎺э紝姣忕鍒嗘瀽涓€娆? * 2. 鎵嬪姩涓婁紶鎴浘杩涜椋庨櫓璇嗗埆
 * 3. 鎵归噺鍒嗘瀽鍘嗗彶鎴浘
 */
@Slf4j
@Service
public class CameraAIRecognitionServiceImpl implements CameraAIRecognitionService {
    
    @Autowired
    private AipImageClassify aipClient;
    
    @Autowired
    private GLMService glmService;

    @Autowired
    private ModelPresetStrategy modelStrategy;
    
    // GLM 鎻愮ず璇嶏細涓撴敞绀惧尯椋庨櫓鍦烘櫙璇嗗埆
    private static final String GLM_PROMPT = 
        "璇蜂粩缁嗗垎鏋愯繖寮犵ぞ鍖虹洃鎺у浘鐗囷紝鍒ゆ柇鏄惁瀛樺湪浠ヤ笅瀹夊叏椋庨櫓锛歕n\n" +
        "1. 鑰佷汉鎽斿€掞紙鏈変汉韬哄湪鍦颁笂銆佹憯鍊掋€佸€掑湴涓嶈捣锛岀壒鍒槸鑰佷汉锛塡n" +
        "2. 鐏伨鐑熼浘锛堟湁鏄庣伀銆佹祿鐑熴€佺噧鐑э級\n" +
        "3. 浜哄憳鑱氶泦鏂楁锛堝浜鸿仛闆嗐€佹墦鏋躲€佹帹鎼★級\n" +
        "4. 璁炬柦鎹熷潖锛堝叕鍏辫鏂姐€佸櫒鏉愭崯鍧忥級\n" +
        "5. 鍨冨溇鍫嗙Н锛堝瀮鍦炬《澶栨湁澶ч噺鍨冨溇銆佹潅鐗╁爢绉級\n" +
        "6. 鐢靛姩杞﹁繚瑙勫厖鐢碉紙鐢靛姩杞﹀湪妤奸亾銆佸鍐呭厖鐢碉級\n" +
        "7. 璺潰绉按锛堣矾闈㈡湁澶ч噺绉按锛塡n\n" +
        "瑕佹眰锛歕n" +
        "1. 閲嶇偣妫€鏌ユ槸鍚︽湁浜猴紙灏ゅ叾鏄€佷汉锛夎汉鍦ㄥ湴涓婃垨鎽斿€掞紝杩欐槸鏈€閲嶈鐨刓n" +
        "2. 鍙洖绛斿瓨鍦ㄧ殑椋庨櫓绫诲瀷锛屼笉瑕佸垪涓炬湭鍑虹幇鐨勯闄‐n" +
        "3. 濡傛灉瀛樺湪椋庨櫓锛岀洿鎺ュ洖绛旓細'瀛樺湪銆愰闄╃被鍨嬨€?鍏蜂綋鎯呭喌鏄?..'\n" +
        "4. 濡傛灉纭疄娌℃湁浠讳綍椋庨櫓锛屽啀鍥炵瓟'鏃犳槑鏄惧畨鍏ㄩ闄?\n\n" +
        "鍥剧墖鍐呭鍒嗘瀽锛?;
    
    @Override
    public Map<String, String> analyzeCameraImage(String base64Image, String cameraLocation) {
        Map<String, String> result = new HashMap<>();
        result.put("riskType", "鏃犻闄?);
        result.put("riskLevel", "LOW");
        result.put("description", "鍦烘櫙姝ｅ父锛屾棤鏄庢樉瀹夊叏椋庨櫓");
        result.put("isRisk", "false");
        result.put("glmResult", "");
        result.put("baiduLabels", "");
        
        try {
            log.info("=== 寮€濮嬫憚鍍忓ご AI 璇嗗埆锛圔ase64锛?==");
            
            // 1. 灏?Base64 鍥剧墖淇濆瓨鍒颁复鏃舵枃浠?            String tempFilePath = "c:/Users/fengchunzhao/Desktop/future-community-system/backend/upload/temp_" + System.currentTimeMillis() + ".jpg";
            saveBase64Image(base64Image, tempFilePath);
            
            // 2. 璋冪敤鏂囦欢鍒嗘瀽鏂规硶
            result = analyzeCameraImageFromFile(tempFilePath, cameraLocation);
            
            // 3. 鍒犻櫎涓存椂鏂囦欢
            new File(tempFilePath).delete();
            
            return result;
            
        } catch (Exception e) {
            log.error("鎽勫儚澶?AI 璇嗗埆寮傚父锛圔ase64锛夛細{}", e.getMessage(), e);
        }
        
        return result;
    }
    
    /**
     * 灏?Base64 鍥剧墖淇濆瓨鍒版枃浠?     */
    private void saveBase64Image(String base64Image, String filePath) throws Exception {
        // 绉婚櫎 Base64 鍓嶇紑锛堝鏋滄湁锛?        String base64Data = base64Image;
        if (base64Image.contains(",")) {
            base64Data = base64Image.split(",")[1];
        }
        
        // 瑙ｇ爜骞朵繚瀛?        byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(imageBytes);
        }
    }
    
    @Override
    public Map<String, String> analyzeCameraImageFromFile(String localFilePath, String cameraLocation) {
        Map<String, String> result = new HashMap<>();
        result.put("riskType", "鏃犻闄?);
        result.put("riskLevel", "LOW");
        result.put("description", "鍦烘櫙姝ｅ父锛屾棤鏄庢樉瀹夊叏椋庨櫓");
        result.put("isRisk", "false");
        result.put("glmResult", "");
        result.put("baiduLabels", "");
        
        try {
            log.info("=== 寮€濮嬫憚鍍忓ご AI 璇嗗埆锛堜粠鏂囦欢锛?==");
            log.info("鏂囦欢璺緞锛歿}", localFilePath);
            log.info("鎽勫儚澶翠綅缃細{}", cameraLocation);
            
            // 1. 璋冪敤 GLM-4V 鍒嗘瀽
            String glmResult = "";
            try {
                log.info("姝ｅ湪璋冪敤 GLM-4V 鍒嗘瀽...");
                glmResult = modelStrategy.callVisionService(localFilePath, GLM_PROMPT);
                log.info("GLM-4V 鍒嗘瀽缁撴灉锛歿}", glmResult);
                result.put("glmResult", glmResult);
            } catch (Exception e) {
                log.error("GLM-4V 璋冪敤澶辫触锛歿}", e.getMessage(), e);
            }
            
            // 2. 璋冪敤鐧惧害 AI 鏍囩璇嗗埆
            String baiduLabels = "";
            try {
                log.info("姝ｅ湪璋冪敤鐧惧害 AI 鏍囩璇嗗埆...");
                JSONObject response = aipClient.advancedGeneral(localFilePath, new java.util.HashMap<>());
                
                if (response != null && response.has("result")) {
                    JSONArray results = response.getJSONArray("result");
                    if (results != null && results.length() > 0) {
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject item = results.getJSONObject(i);
                            String keyword = item.getString("keyword");
                            float confidence = (float) item.optDouble("probability", 0.0);
                            baiduLabels += keyword + "(" + String.format("%.2f", confidence) + ") ";
                        }
                    }
                }
                log.info("鐧惧害 AI 鏍囩锛歿}", baiduLabels);
                result.put("baiduLabels", baiduLabels.trim());
            } catch (Exception e) {
                log.error("鐧惧害 AI 璋冪敤澶辫触锛歿}", e.getMessage(), e);
            }
            
            // 3. 鍚堝苟 AI 缁撴灉杩涜椋庨櫓鍒ゆ柇
            analyzeAndJudgeRisk(glmResult, baiduLabels, result);
            
            // 4. 娣诲姞鎽勫儚澶翠綅缃俊鎭?            if (cameraLocation != null && !cameraLocation.isEmpty()) {
                result.put("cameraLocation", cameraLocation);
            }
            
            log.info("鎽勫儚澶?AI 璇嗗埆瀹屾垚 - 椋庨櫓绫诲瀷锛歿}, 椋庨櫓绛夌骇锛歿}, 鏄惁椋庨櫓锛歿}", 
                result.get("riskType"), result.get("riskLevel"), result.get("isRisk"));
            
        } catch (Exception e) {
            log.error("鎽勫儚澶?AI 璇嗗埆寮傚父锛歿}", e.getMessage(), e);
        }
        
        return result;
    }
    
    /**
     * 鍚堝苟 GLM 鍜岀櫨搴?AI 缁撴灉锛岃繘琛岄闄╁垽鏂?     * 澶嶇敤 AIRecognitionServiceImpl 涓殑鍒ゆ柇閫昏緫
     */
    private void analyzeAndJudgeRisk(String glmResult, String baiduLabels, Map<String, String> result) {
        String riskType = "鏃犻闄?;
        String riskLevel = "LOW";
        String description = "鍦烘櫙姝ｅ父锛屾棤鏄庢樉瀹夊叏椋庨櫓";
        boolean isRisk = false;
        
        // GLM 缁撴灉鍒嗘瀽锛堝鐢?AIRecognitionServiceImpl 鐨勫垽鏂€昏緫锛?        if (glmResult != null && !glmResult.isEmpty()) {
            // 鍒ゆ柇鏄惁瀛樺湪椋庨櫓
            if (glmResult.contains("鑰佷汉鎽斿€?) || glmResult.contains("鎽斿€?) || glmResult.contains("韬哄湪鍦颁笂")) {
                riskType = "鑰佷汉鎽斿€?;
                riskLevel = "HIGH";
                description = glmResult;
                isRisk = true;
            } else if (glmResult.contains("鐏伨") || glmResult.contains("鏄庣伀") || glmResult.contains("娴撶儫") || glmResult.contains("鐕冪儳")) {
                riskType = "鐏伨";
                riskLevel = "HIGH";
                description = glmResult;
                isRisk = true;
            } else if (glmResult.contains("鎵撴灦") || glmResult.contains("鏂楁") || glmResult.contains("鎺ㄦ悺") || glmResult.contains("鍐茬獊")) {
                riskType = "鎵撴灦鏂楁";
                riskLevel = "HIGH";
                description = glmResult;
                isRisk = true;
            } else if (glmResult.contains("鍨冨溇鍫嗙Н") || glmResult.contains("鏉傜墿鍫嗙Н")) {
                riskType = "鍨冨溇鍫嗙Н";
                riskLevel = "MEDIUM";
                description = glmResult;
                isRisk = true;
            } else if (glmResult.contains("璁炬柦鎹熷潖") || glmResult.contains("鍣ㄦ潗鎹熷潖")) {
                riskType = "璁炬柦鎹熷潖";
                riskLevel = "MEDIUM";
                description = glmResult;
                isRisk = true;
            } else if (glmResult.contains("鐢靛姩杞?) && (glmResult.contains("鍏呯數") || glmResult.contains("妤奸亾"))) {
                riskType = "鐢靛姩杞﹁繚瑙勫厖鐢?;
                riskLevel = "HIGH";
                description = glmResult;
                isRisk = true;
            } else if (glmResult.contains("绉按")) {
                riskType = "璺潰绉按";
                riskLevel = "LOW";
                description = glmResult;
                isRisk = true;
            }
        }
        
        // 鐧惧害 AI 鏍囩杈呭姪鍒ゆ柇
        if (baiduLabels != null && !baiduLabels.isEmpty()) {
            if (baiduLabels.contains("鐏伨") || baiduLabels.contains("鐏劙") || baiduLabels.contains("鐑熼浘")) {
                if (!isRisk || riskLevel.equals("LOW")) {
                    riskType = "鐏伨";
                    riskLevel = "HIGH";
                    isRisk = true;
                }
            } else if (baiduLabels.contains("鎽斿€?) || baiduLabels.contains("浜?) && baiduLabels.contains("鍦伴潰")) {
                if (!isRisk) {
                    riskType = "鐤戜技鑰佷汉鎽斿€?;
                    riskLevel = "MEDIUM";
                    isRisk = true;
                }
            }
        }
        
        result.put("riskType", riskType);
        result.put("riskLevel", riskLevel);
        result.put("description", description);
        result.put("isRisk", String.valueOf(isRisk));
    }
}

