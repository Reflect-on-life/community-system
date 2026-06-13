package com.example.futurecommunity.service.impl;

import com.baidu.aip.imageclassify.AipImageClassify;
import com.example.futurecommunity.model.RiskEvent;
import com.example.futurecommunity.config.ModelPresetStrategy;
import com.example.futurecommunity.service.AIRecognitionService;
import com.example.futurecommunity.service.GLMService;
import com.example.futurecommunity.service.RiskEventAutoCreateService;
import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIRecognitionServiceImpl implements AIRecognitionService {
    
    @Autowired
    private AipImageClassify aipClient;
    
    @Autowired
    private GLMService glmService;
    
    @Autowired
    private RiskEventAutoCreateService riskEventAutoCreateService;

    @Autowired
    private ModelPresetStrategy modelStrategy;
    
    private static final String GLM_PROMPT =
        "璇蜂粩缁嗗垎鏋愯繖寮犵洃鎺у浘鐗囷紝鍙弿杩颁綘瀹為檯瑙傚療鍒扮殑鍐呭锛屼笉瑕佹弿杩颁笉瀛樺湪鐨勬儏鍐点€俓n\n" +
        "閲嶇偣鍏虫敞浠ヤ笅鏂归潰锛屽彧杈撳嚭浣犵‘瀹炵湅鍒扮殑锛歕n\n" +
        "1. 浜虹墿锛氭暟閲忋€佸勾榫勭壒寰併€佸叿浣撳姩浣滃拰濮挎€侊紙濡傦細绔欑珛銆佽璧般€佸潗鐫€銆佽汉鍦ㄥ湴涓娿€佹憯鍊掋€佸集鑵扮瓑锛塡n" +
        "2. 浜虹墿浜掑姩锛氬鏈夊浜猴紝鎻忚堪浠栦滑涔嬮棿鐨勫叿浣撹涓猴紙浜よ皥銆佸悎浣溿€佸啿绐併€佹帹鎼＄瓑锛塡n" +
        "3. 鐜鐘跺喌锛氬湴闈㈡儏鍐点€佺墿鍝佸爢鏀炬儏鍐点€佽鏂界姸鎬乗n" +
        "4. 鍗遍櫓杩硅薄锛氱伀鐒般€佹祿鐑熴€佺Н姘淬€佽鏂界牬鎹熺瓑\n\n" +
        "瑕佹眰锛歕n" +
        "- 鍙弿杩扮敾闈腑瀹為檯瀛樺湪鐨勪簨鐗╁拰琛屼负\n" +
        "- 涓嶈鐢ㄣ€屾病鏈夈€嶃€屼笉瀛樺湪銆嶃€屾湭鍙戠幇銆嶇瓑鍚﹀畾鍙ュ紡\n" +
        "- 濡傛灉鐢婚潰姝ｅ父锛岀畝鍗曟弿杩板満鏅嵆鍙痋n" +
        "- 缁欏嚭浣犵殑椋庨櫓鍒ゆ柇鍜屽缓璁?;
    
    @Override
    public Map<String, String> recognizeRisk(String imageUrl) {
        return recognizeRiskWithTitle(imageUrl, null);
    }
    
    @Override
    public Map<String, String> recognizeRiskWithTitle(String imageUrl, String riskTitle) {
        return recognizeRiskWithFullInfo(imageUrl, riskTitle);
    }
    
    @Override
    public Map<String, String> recognizeRiskWithFullInfo(String imageUrl, String riskTitle) {
        Map<String, String> result = new HashMap<>();
        result.put("riskType", "鏈煡椋庨櫓");
        result.put("riskLevel", "LOW");
        result.put("description", "绯荤粺鐩戞祴鍒版綔鍦ㄩ闄╀簨浠?);
        result.put("glmResult", "");
        result.put("baiduLabels", "");
        
        try {
            System.out.println("=== 寮€濮?AI 椋庨櫓璇嗗埆 ===");
            System.out.println("鍥剧墖 URL: " + imageUrl);
            System.out.println("椋庨櫓鏍囬锛? + (riskTitle != null ? riskTitle : "鏃?));
            
            String localFilePath = imageUrl;
            if (imageUrl.startsWith("/upload/")) {
                // 瑙ｆ瀽鐩稿璺緞锛屾敮鎸佷袱绉嶇洰褰曠粨鏋勶細
                // 1. /upload/monitor/2026-04-19/filename.jpg 鈫?upload/monitor/2026-04-19/filename.jpg
                // 2. /upload/images/filename.jpg 鈫?upload/images/filename.jpg
                String pathParts = imageUrl.substring("/upload/".length());
                localFilePath = "c:/Users/fengchunzhao/Desktop/future-community-system/backend/upload/" + pathParts;
                System.out.println("杞崲涓烘湰鍦版枃浠惰矾寰勶細" + localFilePath);
            }
            
            String glmResult = "";
            try {
                System.out.println("姝ｅ湪璋冪敤 VLM...");
                glmResult = modelStrategy.callVisionService(localFilePath, GLM_PROMPT);
                System.out.println("VLM 鍒嗘瀽缁撴灉锛? + glmResult);
                result.put("glmResult", glmResult);
            } catch (Exception e) {
                System.err.println("GLM 璋冪敤澶辫触锛? + e.getMessage());
                e.printStackTrace();
                glmResult = "";
                result.put("glmResult", "");
            }
            
            String baiduLabels = "";
            if (modelStrategy.useBaiduAI()) {
            try {
                System.out.println("姝ｅ湪璋冪敤鐧惧害 AI...");
                JSONObject response = aipClient.advancedGeneral(localFilePath, new HashMap<>());
                
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
                System.out.println("鐧惧害 AI 鏍囩锛? + baiduLabels);
                result.put("baiduLabels", baiduLabels.trim());
            } catch (Exception e) {
                System.err.println("鐧惧害 AI 璋冪敤澶辫触锛? + e.getMessage());
                e.printStackTrace();
                baiduLabels = "";
                result.put("baiduLabels", "");
            }
            
            System.out.println("=== 鍚堝苟淇℃伅杩涜椋庨櫓鍒ゆ柇 ===");
            Map<String, String> analysisResult = analyzeRiskType(glmResult, baiduLabels, riskTitle);
            
            result.put("riskType", analysisResult.get("riskType"));
            result.put("riskLevel", analysisResult.get("riskLevel"));
            result.put("description", analysisResult.get("description"));
            
            System.out.println("鏈€缁堣瘑鍒粨鏋滐細");
            System.out.println("  椋庨櫓绫诲瀷锛? + result.get("riskType"));
            System.out.println("  椋庨櫓绛夌骇锛? + result.get("riskLevel"));
            System.out.println("  鎻忚堪淇℃伅锛? + result.get("description"));
            
            String riskLevel = analysisResult.get("riskLevel");
            if ("HIGH".equals(riskLevel) || "MEDIUM".equals(riskLevel)) {
                System.out.println("妫€娴嬪埌涓珮椋庨櫓锛屽紑濮嬭嚜鍔ㄥ垱寤洪闄╀簨浠?..");
                
                RiskEvent riskEvent = riskEventAutoCreateService.autoCreateRiskEvent(
                    analysisResult.get("riskType"),
                    riskLevel,
                    "鐩戞帶鍖哄煙",
                    imageUrl,
                    analysisResult.get("description"),
                    "camera_default",
                    glmResult,
                    baiduLabels
                );
                
                if (riskEvent != null) {
                    System.out.println("椋庨櫓浜嬩欢鑷姩鍒涘缓鎴愬姛锛屼簨浠?ID: " + riskEvent.getId());
                    result.put("riskEventId", String.valueOf(riskEvent.getId()));
                } else {
                    System.out.println("椋庨櫓浜嬩欢鏈垱寤猴紙鍙兘 5 鍒嗛挓鍐呮湁閲嶅锛?);
                }
            }
            
            System.out.println("========================");
            
            return result;
        } catch (Exception e) {
            System.err.println("AI 椋庨櫓璇嗗埆澶辫触锛? + e.getMessage());
            e.printStackTrace();
            result.put("riskType", "鏈煡椋庨櫓");
            result.put("riskLevel", "LOW");
            result.put("description", "AI 璇嗗埆澶辫触锛? + e.getMessage());
            return result;
        }
    }
    
    @Override
    public Map<String, String> recognizeRiskWithCameraInfo(String imageUrl, String riskTitle, String cameraLocation, String cameraId) {
        Map<String, String> result = new HashMap<>();
        result.put("riskType", "鏈煡椋庨櫓");
        result.put("riskLevel", "LOW");
        result.put("description", "绯荤粺鐩戞祴鍒版綔鍦ㄩ闄╀簨浠?);
        result.put("glmResult", "");
        result.put("baiduLabels", "");
        
        try {
            System.out.println("=== 寮€濮?AI 椋庨櫓璇嗗埆锛堝甫鎽勫儚澶翠俊鎭級===");
            System.out.println("鍥剧墖 URL: " + imageUrl);
            System.out.println("椋庨櫓鏍囬锛? + (riskTitle != null ? riskTitle : "鏃?));
            System.out.println("鎽勫儚澶翠綅缃細" + cameraLocation);
            System.out.println("鎽勫儚澶碔D锛? + cameraId);
            
            String localFilePath = imageUrl;
            if (imageUrl.startsWith("/upload/")) {
                String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                localFilePath = "c:/Users/fengchunzhao/Desktop/future-community-system/backend/upload/images/" + filename;
                System.out.println("杞崲涓烘湰鍦版枃浠惰矾寰勶細" + localFilePath);
            }
            
            String glmResult = "";
            try {
                System.out.println("姝ｅ湪璋冪敤 VLM...");
                glmResult = modelStrategy.callVisionService(localFilePath, GLM_PROMPT);
                System.out.println("VLM 鍒嗘瀽缁撴灉锛? + glmResult);
                result.put("glmResult", glmResult);
            } catch (Exception e) {
                System.err.println("GLM 璋冪敤澶辫触锛? + e.getMessage());
                e.printStackTrace();
                glmResult = "";
                result.put("glmResult", "");
            }
            
            String baiduLabels = "";
            try {
                System.out.println("姝ｅ湪璋冪敤鐧惧害 AI...");
                JSONObject response = aipClient.advancedGeneral(localFilePath, new HashMap<>());
                
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
                System.out.println("鐧惧害 AI 鏍囩锛? + baiduLabels);
                result.put("baiduLabels", baiduLabels.trim());
            } catch (Exception e) {
                System.err.println("鐧惧害 AI 璋冪敤澶辫触锛? + e.getMessage());
                e.printStackTrace();
                baiduLabels = "";
                result.put("baiduLabels", "");
            }
            
            System.out.println("=== 鍚堝苟淇℃伅杩涜椋庨櫓鍒ゆ柇 ===");
            Map<String, String> analysisResult = analyzeRiskType(glmResult, baiduLabels, riskTitle);
            
            result.put("riskType", analysisResult.get("riskType"));
            result.put("riskLevel", analysisResult.get("riskLevel"));
            result.put("description", analysisResult.get("description"));
            
            System.out.println("鏈€缁堣瘑鍒粨鏋滐細");
            System.out.println("  椋庨櫓绫诲瀷锛? + result.get("riskType"));
            System.out.println("  椋庨櫓绛夌骇锛? + result.get("riskLevel"));
            System.out.println("  鎻忚堪淇℃伅锛? + result.get("description"));
            
            // 浣跨敤鐪熷疄鎽勫儚澶翠綅缃嚜鍔ㄥ垱寤洪闄╀簨浠?            String riskLevel = analysisResult.get("riskLevel");
            if ("HIGH".equals(riskLevel) || "MEDIUM".equals(riskLevel)) {
                System.out.println("妫€娴嬪埌涓珮椋庨櫓锛屽紑濮嬭嚜鍔ㄥ垱寤洪闄╀簨浠讹紙浣跨敤鐪熷疄鎽勫儚澶翠綅缃級...");
                
                RiskEvent riskEvent = riskEventAutoCreateService.autoCreateRiskEvent(
                    analysisResult.get("riskType"),
                    riskLevel,
                    cameraLocation,  // 鉁?浣跨敤鐪熷疄鎽勫儚澶翠綅缃?                    imageUrl,
                    analysisResult.get("description"),
                    cameraId,         // 鉁?浣跨敤鐪熷疄鎽勫儚澶碔D
                    glmResult,
                    baiduLabels
                );
                
                if (riskEvent != null) {
                    System.out.println("椋庨櫓浜嬩欢鑷姩鍒涘缓鎴愬姛锛屼簨浠?ID: " + riskEvent.getId());
                    result.put("riskEventId", String.valueOf(riskEvent.getId()));
                } else {
                    System.out.println("椋庨櫓浜嬩欢鏈垱寤猴紙鍙兘 5 鍒嗛挓鍐呮湁閲嶅锛?);
                }
            } else {
                System.out.println("浣庨闄╁満鏅紝涓嶈嚜鍔ㄥ垱寤洪闄╀簨浠?);
            }
            
            System.out.println("========================");
            
            return result;
            
        } catch (Exception e) {
            System.err.println("AI 璇嗗埆寮傚父锛? + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }
    
    private Map<String, String> analyzeRiskType(String glmResult, String baiduLabels, String userTitle) {
        System.out.println("========================================================");
        System.out.println("銆怗LM 浼樺厛璇嗗埆娴佺▼銆?);
        System.out.println("========================================================");
        
        if (glmResult != null && !glmResult.isEmpty()) {
            System.out.println("GLM 缁撴灉锛? + glmResult);
        }
        if (baiduLabels != null && !baiduLabels.isEmpty()) {
            System.out.println("鐧惧害鏍囩锛? + baiduLabels);
        }
        if (userTitle != null && !userTitle.isEmpty()) {
            System.out.println("鐢ㄦ埛鏍囬锛? + userTitle);
        }
        
        String text = (glmResult != null ? glmResult.toLowerCase() + " " : "") +
                      (userTitle != null ? userTitle.toLowerCase() + " " : "") +
                      (baiduLabels != null ? baiduLabels.toLowerCase() + " " : "");
        System.out.println("鍚堝苟鍒ゆ柇鏂囨湰锛? + text);
        
        // ================================================================
        // 绗竴灞傦細GLM 鐩存帴璇嗗埆锛堟渶楂樹紭鍏堢骇锛?        // 濡傛灉 GLM 鏄庣‘鎻愬埌浜嗘煇绉嶉闄╃被鍨嬶紝鐩存帴閲囩敤 GLM 鐨勫垽鏂?        // ================================================================
        if (glmResult != null && !glmResult.isEmpty()) {
            String glmRiskType = extractRiskTypeFromGLM(glmResult);
            if (glmRiskType != null) {
                System.out.println("\n鉁?GLM 鐩存帴璇嗗埆鍒伴闄╋細" + glmRiskType);
                System.out.println("========================================================");
                return getRiskResultByType(glmRiskType);
            }
            System.out.println("  鈫?GLM 鏈槑纭瘑鍒埌椋庨櫓绫诲瀷锛岃繘鍏ヨ緟鍔╁垽鏂?);
        }
        
        // ================================================================
        // 绗簩灞傦細鐧惧害 AI 鏍囩杈呭姪鍒ゆ柇
        // GLM 鏈槑纭瘑鍒椂锛岄€氳繃鐧惧害鏍囩杩涜鍒濇鍒ゆ柇
        // ================================================================
        String baiduRiskType = null;
        if (baiduLabels != null && !baiduLabels.isEmpty()) {
            baiduRiskType = extractRiskTypeFromBaidu(baiduLabels);
            if (baiduRiskType != null) {
                System.out.println("\n馃搶 鐧惧害 AI 鏍囩璇嗗埆鍒伴闄╋細" + baiduRiskType);
                System.out.println("  鈫?闇€瑕?GLM 浜屾纭...");
                
                // 绗笁灞傦細GLM 浜屾纭
                if (glmResult != null && !glmResult.isEmpty()) {
                    if (confirmRiskByGLM(glmResult, baiduRiskType)) {
                        System.out.println("鉁?GLM 浜屾纭閫氳繃锛岄闄╃被鍨嬶細" + baiduRiskType);
                        System.out.println("========================================================");
                        return getRiskResultByType(baiduRiskType);
                    } else {
                        System.out.println("鉂?GLM 浜屾纭涓嶉€氳繃锛屾帓闄ゆ椋庨櫓");
                    }
                } else {
                    // GLM 涓嶅彲鐢紝鐩存帴浣跨敤鐧惧害缁撴灉
                    System.out.println("鈿狅笍 GLM 涓嶅彲鐢紝鐩存帴浣跨敤鐧惧害璇嗗埆缁撴灉锛? + baiduRiskType);
                    System.out.println("========================================================");
                    return getRiskResultByType(baiduRiskType);
                }
            }
        }
        
        // ================================================================
        // 绗洓灞傦細涓よ€呴兘鏈瘑鍒埌椋庨櫓锛屽垽鏂负瀹夊叏鍦烘櫙
        // ================================================================
        System.out.println("\n鉁?GLM 鍜岀櫨搴?AI 鍧囨湭璇嗗埆鍒伴闄╋紝鍒ゅ畾涓哄畨鍏ㄥ満鏅?);
        System.out.println("========================================================");
        return buildRiskResult("姝ｅ父鍦烘櫙", "LOW", 
            "鐩戞帶鐢婚潰姝ｅ父锛屾湭妫€娴嬪埌鏄庢樉椋庨櫓銆?);
    }
    
    /**
     * 浠?GLM 缁撴灉涓洿鎺ユ彁鍙栭闄╃被鍨?     * GLM 鏄庣‘鎻愬埌鐨勯闄╃被鍨嬩紭鍏堢骇鏈€楂?     */
    private String extractRiskTypeFromGLM(String glmResult) {
        String text = glmResult.toLowerCase();
        
        // ================================================================
        // 鑰佷汉鎽斿€?- 鏈€楂樹紭鍏堢骇
        // 绛栫暐锛氭槑纭憯鍊掕瘝 OR (鑰佷汉鐗瑰緛 + 鍦伴潰/闈炲父瑙勫Э鍔?
        // ================================================================
        boolean hasExplicitFall = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "鎽斿€?, "璺屽€?, "鍊掑湴", "璺岃惤", "鎽旇筏",
            "fall down", "fallen", "collapsed", "slip and fall",
            "鑰佷汉鍊?, "鑰佷汉鎽?, "璺屽潗鍦ㄥ湴", "鎽斾簡", "鎽斿€掍簡"
        });
        
        boolean hasElderlyFeature = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "鑰佷汉", "鑰佸勾浜?, "鑰佸勾", "鑰佽€?, "鑰佸ザ濂?, "鑰佺埛鐖?,
            "骞磋繄", "骞撮暱", "鑰侀緞", "elderly", "senior", "aged"
        });
        
        boolean hasGroundPosture = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "韬哄湪鍦?, "鍧愬湪鍦?, "瓒村湪鍦?, "鍊掑湪鍦?, "璺湪鍦?,
            "韬哄湪鑽夊湴", "鍧愬湪鑽夊湴", "鍧愬湪鍦伴潰", "韬哄湪鍦颁笂", "鍧愬湪鍦颁笂",
            "鍊掑湪", "韬哄湪", "鍧愬湪鑽夊湴涓?, "韬哄湪鍦伴潰", "瓒村湪鍦颁笂"
        });
        
        if (hasExplicitFall || (hasElderlyFeature && hasGroundPosture)) {
            if (!containsKeywordsWithNegationCheck(glmResult, new String[]{"姝ｅ父鍦烘櫙", "娌℃湁椋庨櫓", "鏈彂鐜板紓甯?})) {
                return "鑰佷汉鎽斿€?;
            }
        }
        
        // ================================================================
        // 鍙戜紶鍗?- 蹇呴』鍦ㄧ伀鐏句箣鍓嶅垽鏂紝閬垮厤琚鍒?        // 浣跨敤瀹屾暣鐨?isLeafletDistribution 鏂规硶锛堜笁灞傛娴嬶級
        // ================================================================
        if (isLeafletDistribution(text, glmResult)) {
            return "鍙戜紶鍗?;
        }
        
        // ================================================================
        // 鐏伨 - 鍙娴嬫槑纭伀鐒扮浉鍏?        // 閲嶈锛氬鏋淕LM鍚屾椂鎻愬埌"鐕冪儳/鐫€鐏?鍜?鍨冨溇妗?锛岃繖鏄?鍨冨溇鐫€鐏?锛屼粛鏄伀鐏撅紒
        // 鍙湁褰撴病鏈夋槑纭伀鐏捐瘝銆佷粎鎻愬埌"鍨冨溇"鏃舵墠鎺掗櫎
        // ================================================================
        if (containsKeywordsWithNegationCheck(glmResult, new String[]{
                "鐏劙", "璧风伀", "鐫€鐏?, "娴撶儫", "鐕冪儳", "鐏娍", "澶х伀",
                "fire", "flame", "smoke", "burning"}) &&
            !hasFireInNegativeContext(glmResult)) {
            return "鐏伨";
        }
        
        // ================================================================
        // 鎵撴枟鍐茬獊 - 鎵╁睍鍏抽敭璇嶏紝瑕嗙洊鍔ㄤ綔鎻忚堪
        // GLM 鍙兘鐢?鍦ㄦ墦...浜?銆?鏆村姏浜嬩欢"銆?娈存墦"绛夋弿杩?        // ================================================================
        if (containsKeywordsWithNegationCheck(glmResult, new String[]{
                "鎵撴枟", "鍐茬獊", "鎵撴灦", "鑲綋鍐茬獊", "浜掓", "娈存墦", "鏂楁",
                "fighting", "conflict", "assault", "violence", "浜夋墽", "浜夋墽涓?}) ||
            (containsKeywordsWithNegationCheck(glmResult, new String[]{
                "鎶撲綇", "鎺?, "鍕?, "鎸夊€?, "鍘嬪埗", "鎺ㄦ悺", "鎷夋壇",
                "鎸変綇", "鑴栧瓙", "鍠夊挋", "鎺愪綇", "鍕掍綇", "鎷?, "鎵墦", "鎻?}) &&
             containsKeywordsWithNegationCheck(glmResult, new String[]{
                "鍦颁笂", "鍊掑湴", "鍊掍笅", "鎽斿€?, "鍦ㄥ湴", "鐥涜嫤"})) ||
            (containsKeywordsWithNegationCheck(glmResult, new String[]{
                "鍙嶆姉", "鑷崼", "鍙嶆姉", "鎸ｆ墡", "鎶垫姉", "鎸ｆ墡涓?}) &&
             containsKeywordsWithNegationCheck(glmResult, new String[]{
                "鎸変綇", "鎶撲綇", "鍘嬪埗", "鎺?, "鎷?, "鎵?, "鎵?})) ||
            containsKeywordsWithNegationCheck(glmResult, new String[]{
                "鍦ㄦ墦", "姝ｅ湪鎵?, "娈存墦", "鏆村姏浜嬩欢", "鏀诲嚮", "浼ゅ浠栦汉",
                "beating", "attacking", "violent incident"})) {
            return "鎵撴枟鍐茬獊";
        }
        
        // ================================================================
        // 浜哄憳鑱氶泦
        // ================================================================
        if (containsKeywords(glmResult.toLowerCase(), new String[]{
                "浜虹兢", "鑱氶泦", "澶ч噺浜哄憳", "浜哄憳瀵嗛泦", "浜哄憳鎷ユ尋",
                "crowd gathering", "鑱氶泦鍦ㄤ竴璧?, "鍥村湪涓€璧?})) {
            return "浜哄憳鑱氶泦";
        }
        
        // ================================================================
        // 鍨冨溇鍫嗙Н - 浣跨敤瀹屾暣鐨?isGarbageAccumulation 鏂规硶
        // 瑕嗙洊鍚勭琛ㄨ堪锛氬瀮鍦捐鍫嗙Н銆佸ぇ閲忓瀮鍦俱€佸簾寮冪墿鍫嗙Н绛?        // ================================================================
        if (isGarbageAccumulation(text, glmResult)) {
            return "鍨冨溇鍫嗙Н";
        }
        
        // ================================================================
        // 璺潰绉按锛堥渶瑕佸惁瀹氭劅鐭ワ紝閬垮厤"娌℃湁鐪嬪埌绉按"琚鍒わ級
        // ================================================================
        if (containsKeywordsWithNegationCheck(glmResult, new String[]{
                "璺潰绉按", "鍦伴潰绉按", "绉按涓ラ噸", "water accumulation", "puddle",
                "姘撮潰", "姘存醇"}) &&
            !isGarbageSceneByText(text) &&
            !hasKeywordInNegativeContext(glmResult, new String[]{"绉按", "water", "puddle"})) {
            return "璺潰绉按";
        }
        
        // ================================================================
        // 杞﹁締杩濆仠
        // ================================================================
        if (containsKeywords(glmResult.toLowerCase(), new String[]{
                "杞﹁締杩濆仠", "杩濊鍋滆溅", "涔卞仠杞?, "illegal parking", "杩濆仠",
                "杩濊鍋滄斁", "涔卞仠涔辨斁", "鍋滃湪涓嶈鍋?})) {
            return "杞﹁締杩濆仠";
        }
        
        // ================================================================
        // 瀹犵墿浼や汉
        // ================================================================
        if (containsKeywords(glmResult.toLowerCase(), new String[]{
                "瀹犵墿浼や汉", "鐙楀挰浜?, "鍔ㄧ墿鏀诲嚮", "pet attack", "dog attack", "bite",
                "鍜汉", "鍜激", "鏀诲嚮浜?})) {
            return "瀹犵墿浼や汉";
        }
        
        // ================================================================
        // 璁炬柦鎹熷潖
        // 閲嶈锛氬鏋滃悓鏃舵槸鍨冨溇鍦烘櫙锛堝瀮鍦炬《鐮存崯銆佸瀮鍦惧鑷寸幆澧冭剰涔憋級锛?        // 搴斾紭鍏堝垽涓?鍨冨溇鍫嗙Н"锛岃€岄潪"璁炬柦鎹熷潖"
        // ================================================================
        if (containsKeywords(glmResult.toLowerCase(), new String[]{
                "璁炬柦鎹熷潖", "璁惧鎹熷潖", "鍊掑", "facility damage",
                "鐮存崯涓ラ噸", "鏂"}) &&
            !isGarbageSceneByText(glmResult.toLowerCase()) &&
            !glmResult.toLowerCase().contains("鍨冨溇妗?) &&
            !glmResult.toLowerCase().contains("鍨冨溇")) {
            return "璁炬柦鎹熷潖";
        }
        
        // ================================================================
        // 鍙戜紶鍗?- 浣跨敤瀹屾暣鐨?isLeafletDistribution 鏂规硶
        // 涓夊眰妫€娴嬶細鐩存帴鍏抽敭璇?+ 琛屼负缁勫悎 + 鍦烘櫙缁勫悎
        // ================================================================
        if (isLeafletDistribution(text, glmResult)) {
            return "鍙戜紶鍗?;
        }
        
        return null;
    }
    
    /**
     * 浠庣櫨搴?AI 鏍囩涓彁鍙栭闄╃被鍨?     */
    private String extractRiskTypeFromBaidu(String baiduLabels) {
        String labels = baiduLabels.toLowerCase();
        
        // 鑰佷汉鎽斿€?        if (labels.contains("鑰佷汉") && (labels.contains("璺屽€?) || labels.contains("鎽斿€?))) {
            return "鑰佷汉鎽斿€?;
        }
        
        // 鐏伨
        if (labels.contains("鐏劙") || labels.contains("鐏伨") || labels.contains("娴撶儫")) {
            return "鐏伨";
        }
        
        // 浜哄憳鑱氶泦
        if (labels.contains("浜虹兢") || labels.contains("crowd")) {
            return "浜哄憳鑱氶泦";
        }
        
        // 鍨冨溇鍫嗙Н
        if (labels.contains("鍨冨溇") || labels.contains("鍨冨溇鍦?) || labels.contains("鍨冨溇灞?)) {
            return "鍨冨溇鍫嗙Н";
        }
        
        // 璺潰绉按
        if (labels.contains("绉按") || labels.contains("姘?) || labels.contains("puddle")) {
            return "璺潰绉按";
        }
        
        // 杞﹁締杩濆仠
        if (labels.contains("杩濆仠") || labels.contains("杩濊鍋滆溅")) {
            return "杞﹁締杩濆仠";
        }
        
        // 瀹犵墿浼や汉
        if (labels.contains("瀹犵墿") || labels.contains("dog") || labels.contains("animal")) {
            if (labels.contains("鍜?) || labels.contains("鏀诲嚮") || labels.contains("bite") || labels.contains("attack")) {
                return "瀹犵墿浼や汉";
            }
        }
        
        return null;
    }
    
    /**
     * GLM 浜屾纭锛氭牴鎹櫨搴﹁瘑鍒殑椋庨櫓绫诲瀷锛屾鏌?GLM 缁撴灉鏄惁鏀寔
     */
    private boolean confirmRiskByGLM(String glmResult, String baiduRiskType) {
        String text = glmResult.toLowerCase();
        
        switch (baiduRiskType) {
            case "鍨冨溇鍫嗙Н":
                return text.contains("鍨冨溇") || text.contains("搴熷純鐗?) || text.contains("鏉傜墿") ||
                       text.contains("garbage") || text.contains("trash") || text.contains("litter");
            case "璺潰绉按":
                return text.contains("绉按") || text.contains("姘?) || text.contains("婀挎粦") ||
                       text.contains("water") || text.contains("puddle");
            case "鐏伨":
                return (text.contains("鐏?) || text.contains("鐕冪儳") || text.contains("鐑?)) &&
                       !isGarbageSceneByText(text);
            case "鑰佷汉鎽斿€?:
                return text.contains("鑰佷汉") && (text.contains("鍊?) || text.contains("鎽?) || text.contains("韬?));
            case "杞﹁締杩濆仠":
                return text.contains("鍋滆溅") || text.contains("杞﹁締") || text.contains("car");
            case "浜哄憳鑱氶泦":
                return text.contains("浜虹兢") || text.contains("浜哄憳") || text.contains("crowd");
            default:
                return true;
        }
    }
    
    /**
     * 鏍规嵁椋庨櫓绫诲瀷杩斿洖瀵瑰簲鐨勭粨鏋?     */
    private Map<String, String> getRiskResultByType(String riskType) {
        switch (riskType) {
            case "鑰佷汉鎽斿€?:
                return buildRiskResult("鑰佷汉鎽斿€?, "HIGH",
                    "鐩戞帶妫€娴嬪埌鐤戜技鑰佷汉鎽斿€掞紝璇风珛鍗冲墠寰€鏌ョ湅骞舵彁渚涘府鍔╋紝蹇呰鏃惰仈绯诲尰鐤楁晳鎻淬€?);
            case "鐏伨":
                return buildRiskResult("鐏伨", "HIGH",
                    "鐩戞帶妫€娴嬪埌鐏伨杩硅薄锛岃绔嬪嵆鍚姩搴旀€ラ妗堬紝鐤忔暎浜哄憳骞惰仈绯绘秷闃查儴闂ㄣ€?);
            case "鎵撴枟鍐茬獊":
                return buildRiskResult("鎵撴枟鍐茬獊", "HIGH",
                    "鐩戞帶妫€娴嬪埌鑲綋鍐茬獊鎴栨墦鏂楄涓猴紝璇风珛鍗冲墠寰€鍒舵骞惰皟瑙ｏ紝蹇呰鏃惰仈绯诲叕瀹夐儴闂ㄣ€?);
            case "浜哄憳鑱氶泦":
                return buildRiskResult("浜哄憳鑱氶泦", "MEDIUM",
                    "鐩戞帶妫€娴嬪埌浜哄憳寮傚父鑱氶泦锛岃鍓嶅線鏌ョ湅鎯呭喌锛岀淮鎸佺З搴忋€?);
            case "璺潰绉按":
                return buildRiskResult("璺潰绉按", "MEDIUM",
                    "鐩戞帶妫€娴嬪埌璺潰绉按锛岃鍙婃椂鎺掓按骞惰缃绀烘爣蹇楋紝闃叉婊戝€掋€?);
            case "杞﹁締杩濆仠":
                return buildRiskResult("杞﹁締杩濆仠", "MEDIUM",
                    "鐩戞帶妫€娴嬪埌杞﹁締杩濊鍋滄斁锛岃鑱旂郴杞︿富绉昏溅锛屼繚鎸侀€氶亾鐣呴€氥€?);
            case "瀹犵墿浼や汉":
                return buildRiskResult("瀹犵墿浼や汉", "MEDIUM",
                    "鐩戞帶妫€娴嬪埌瀹犵墿鍙兘浼や汉锛岃绔嬪嵆鍓嶅線鏌ョ湅骞惰仈绯诲疇鐗╀富浜恒€?);
            case "鍨冨溇鍫嗙Н":
                return buildRiskResult("鍨冨溇鍫嗙Н", "LOW",
                    "鐩戞帶妫€娴嬪埌鍨冨溇鍫嗙Н锛岃瀹夋帓淇濇磥浜哄憳鍙婃椂娓呯悊銆?);
            case "鍙戜紶鍗?:
                return buildRiskResult("鍙戜紶鍗?, "MEDIUM",
                    "鐩戞帶妫€娴嬪埌鏈変汉鍦ㄥ彂鏀句紶鍗曟垨瀹ｄ紶璧勬枡锛岃纭鏄惁鍚堣銆?);
            case "璁炬柦鎹熷潖":
                return buildRiskResult("璁炬柦鎹熷潖", "MEDIUM",
                    "鐩戞帶妫€娴嬪埌璁炬柦鎹熷潖锛岃鍙婃椂缁翠慨锛岄槻姝㈤€犳垚鏇村ぇ鎹熷け銆?);
            default:
                return buildRiskResult("姝ｅ父鍦烘櫙", "LOW",
                    "鐩戞帶鐢婚潰姝ｅ父锛屾湭妫€娴嬪埌鏄庢樉椋庨櫓銆?);
        }
    }
    
    /**
     * 鍒ゆ柇鏄惁涓哄瀮鍦惧満鏅紙鐢ㄤ簬鎺掗櫎鐏伨绛夎鍒わ級
     */
    private boolean isGarbageSceneByText(String text) {
        return text.contains("鍨冨溇") || text.contains("搴熷純鐗?) || text.contains("鏉傜墿鍫?) ||
               text.contains("鍨冨溇妗?) || text.contains("鍨冨溇鍫?);
    }
    
    /**
     * 鍒ゆ柇鐏伨鍏抽敭璇嶆槸鍚﹀嚭鐜板湪鍚﹀畾璇涓?     * GLM 甯稿湪"娌℃湁鏄庢樉鐨勫畨鍏ㄩ殣鎮ｏ紝濡傜伀鐒般€佹祿鐑?.."涓垪鍑虹伀鐏捐瘝锛岃繖浜涘簲鎺掗櫎
     */
    private boolean hasFireInNegativeContext(String text) {
        // 鎸夊彞鍙枫€佹崲琛屽垎鍙?        String[] sentences = text.split("[銆傦紒\n]");
        String[] fireKeywords = {"鐏劙", "璧风伀", "鐫€鐏?, "娴撶儫", "鐕冪儳", "鐏娍", "澶х伀", "fire", "flame", "smoke", "burning"};
        String[] negationPrefixes = {"娌℃湁", "涓嶅瓨鍦?, "鏈彂鐜?, "娌℃湁鏄庢樉", "鏃犳槑鏄?, "娌℃湁鐪嬪埌", "鏈", "鏃?};
        
        for (String sentence : sentences) {
            // 妫€鏌ユ槸鍚﹀寘鍚伀鐏惧叧閿瘝
            boolean hasFire = false;
            for (String kw : fireKeywords) {
                if (sentence.toLowerCase().contains(kw.toLowerCase())) {
                    hasFire = true;
                    break;
                }
            }
            if (!hasFire) continue;
            
            // 妫€鏌ヨ鍙ユ槸鍚﹀湪鍚﹀畾璇涓?            boolean inNegativeContext = false;
            for (String neg : negationPrefixes) {
                int negIndex = sentence.indexOf(neg);
                int fireIndex = -1;
                for (String kw : fireKeywords) {
                    int idx = sentence.toLowerCase().indexOf(kw.toLowerCase());
                    if (idx >= 0 && (fireIndex < 0 || idx < fireIndex)) {
                        fireIndex = idx;
                    }
                }
                if (negIndex >= 0 && negIndex < fireIndex && fireIndex - negIndex < 30) {
                    inNegativeContext = true;
                    break;
                }
            }
            if (inNegativeContext) {
                System.out.println("  [鐏伨鎺掗櫎] 鍙ュ瓙鍖呭惈鍚﹀畾璇: " + sentence.trim().substring(0, Math.min(50, sentence.length())) + "...");
                return true;
            }
        }
        return false;
    }
    
    /**
     * 閫氱敤鏂规硶锛氬垽鏂寚瀹氬叧閿瘝鏄惁鍑虹幇鍦ㄥ惁瀹氳澧冧腑
     * 鐢ㄤ簬璺潰绉按绛夐渶瑕佹帓闄?娌℃湁鐪嬪埌...绉按"绛夊惁瀹氬彞寮忕殑鍦烘櫙
     */
    private boolean hasKeywordInNegativeContext(String text, String[] keywords) {
        String[] sentences = text.split("[銆傦紒\n]");
        String[] negationPrefixes = {"娌℃湁", "涓嶅瓨鍦?, "鏈彂鐜?, "娌℃湁鏄庢樉", "鏃犳槑鏄?, "娌℃湁鐪嬪埌", "鏈", "鏃?};
        
        for (String sentence : sentences) {
            boolean hasTarget = false;
            for (String kw : keywords) {
                if (sentence.toLowerCase().contains(kw.toLowerCase())) {
                    hasTarget = true;
                    break;
                }
            }
            if (!hasTarget) continue;
            
            for (String neg : negationPrefixes) {
                int negIndex = sentence.indexOf(neg);
                int targetIndex = -1;
                for (String kw : keywords) {
                    int idx = sentence.toLowerCase().indexOf(kw.toLowerCase());
                    if (idx >= 0 && (targetIndex < 0 || idx < targetIndex)) {
                        targetIndex = idx;
                    }
                }
                if (negIndex >= 0 && negIndex < targetIndex && targetIndex - negIndex < 30) {
                    System.out.println("  [鍏抽敭璇嶆帓闄 鍙ュ瓙鍖呭惈鍚﹀畾璇: " + sentence.trim().substring(0, Math.min(50, sentence.length())) + "...");
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isFallDown(String text, String glmResult) {
        System.out.println("\n銆愯€佷汉鎽斿€掑垽鏂€?);

        // =====================================================================
        // 淇璇存槑锛氬師鏉ヨ姹傚悓鏃舵弧瓒?hasLyingKeyword AND hasGLMConfirmation锛?        // 浣?鑰佷汉鍧愬湪鑽夊湴涓?杩欑鎻忚堪涓や釜鏉′欢閮戒笉瑙﹀彂锛屽鑷存紡鍒ゃ€?        //
        // 淇绛栫暐锛堜笁鏉￠€氶亾锛屼换涓€鍛戒腑鍗冲垽瀹氾級锛?        //   閫氶亾A锛氱洿鎺ユ湁"鎽斿€?璺屽€?鍊掑湴"绛夋槑纭瘝姹?        //   閫氶亾B锛氳€佸勾浜虹壒寰?+ 鍧?韬?瓒村湪鍦伴潰鐨勯潪甯歌濮垮娍
        //   閫氶亾C锛氬己鐑堢殑韬哄湴鍏抽敭璇嶏紙鏃犻渶鑰佷汉鐗瑰緛锛?        // =====================================================================

        // ---------- 閫氶亾 A锛氭槑纭憯鍊掕瘝姹?----------
        boolean hasExplicitFall = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "鎽斿€?, "璺屽€?, "鍊掑湴", "璺岃惤", "鎽旇筏",
            "鑰佷汉鍊?, "鑰佷汉鎽?, "璺屽潗鍦ㄥ湴",
            "fall down", "fallen", "collapsed", "slip and fall"
        });
        System.out.println("  閫氶亾A - 鏄庣‘鎽斿€掕瘝: " + hasExplicitFall);

        // ---------- 閫氶亾 B锛氳€佸勾浜虹壒寰?+ 鍦伴潰/闈炲父瑙勫Э鍔?----------
        // 瑕嗙洊"鑰佷汉鍧愬湪鑽夊湴涓?杩欑被 GLM 琛ㄨ堪
        boolean hasElderlyFeature = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "鑰佷汉", "鑰佸勾浜?, "鑰佸勾", "鑰佽€?, "鑰佸ザ濂?, "鑰佺埛鐖?,
            "骞磋繄", "骞撮暱", "鑰侀緞", "elderly", "senior", "aged"
        });
        boolean hasGroundPosture = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "韬哄湪鍦?, "鍧愬湪鍦?, "瓒村湪鍦?, "鍊掑湪鍦?, "璺湪鍦?,
            "鍧愬湪鑽夊湴", "鍧愬湪璺?, "韬哄湪鑽夊湴", "鍧愬湪鍦伴潰",
            "lying on", "sitting on the ground", "on the floor", "on the ground"
        });
        boolean channelB = hasElderlyFeature && hasGroundPosture;
        System.out.println("  閫氶亾B - 鑰佸勾鐗瑰緛=" + hasElderlyFeature + " + 鍦伴潰濮垮娍=" + hasGroundPosture + " 鈫?" + channelB);

        // ---------- 閫氶亾 C锛氬己鐑堢殑韬哄湴鍏抽敭璇嶏紙鏃犻渶鑰佷汉鐗瑰緛锛?---------
        boolean hasStrongLying = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "韬哄湪鍦颁笂", "韬哄湪鍦伴潰", "鍊掑湪鍦颁笂", "瓒村湪鍦颁笂",
            "浠板崸鍦ㄥ湴", "渚у崸鍦ㄥ湴", "淇崸鍦ㄥ湴", "韬哄€?, "鐦€?
        });
        System.out.println("  閫氶亾C - 寮鸿汉鍦拌瘝: " + hasStrongLying);

        // 鎺掗櫎鍏朵粬鏄庣‘椋庨櫓鍦烘櫙
        boolean isOtherRisk = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "鐏劙", "鐕冪儳", "鐫€鐏?, "鐏伨"
        });
        if (isOtherRisk) {
            System.out.println("  妫€娴嬪埌鍏朵粬鏄庣‘椋庨櫓(鐏伨)锛屾帓闄ゆ憯鍊掑垽瀹?);
        }

        boolean result = (hasExplicitFall || channelB || hasStrongLying) && !isOtherRisk;
        System.out.println("  鈫?鍒ゆ柇缁撴灉: " + (result ? "鉁?鏄€佷汉鎽斿€? : "鉂?涓嶆槸鑰佷汉鎽斿€?));

        return result;
    }
    
    private boolean isFire(String text, String glmResult) {
        System.out.println("\n銆愮伀鐏惧垽鏂€?);

        // 浣跨敤鍚﹀畾鎰熺煡锛岄伩鍏?娌℃湁鐏劙"琚鍒?        boolean hasFireKeyword = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "鐏劙", "娴撶儫", "鐕冪儳", "璧风伀", "鐫€鐏?, "澶х伀"
        });
        // 鐧惧害鏍囩涓嶄細鏈夊惁瀹氬彞锛屽彲鐢ㄧ畝鍗曞尮閰嶈ˉ鍏?        if (!hasFireKeyword) {
            hasFireKeyword = containsKeywords(text, new String[]{
                "flame", "burning", "thick smoke", "on fire"
            });
        }
        System.out.println("  鐏伨鍏抽敭璇?鍚﹀畾鎰熺煡): " + hasFireKeyword);

        boolean hasGLMConfirmation = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "鐏劙", "娴撶儫", "鐕冪儳", "璧风伀", "鐏伨", "鐫€鐏?
        });
        System.out.println("  GLM 纭(鍚﹀畾鎰熺煡): " + hasGLMConfirmation);
        if (hasGLMConfirmation && glmResult != null) {
            System.out.println("  GLM 鍏抽敭鍙? " + extractKeyPhrase(glmResult, new String[]{"鐏劙", "娴撶儫", "鐕冪儳"}));
        }

        boolean hasInterference = text.contains("鐑熷ご") ||
                                  text.contains("鍚哥儫") ||
                                  text.contains("鐑熻拏") ||
                                  text.contains("棣欑儫") ||
                                  text.contains("cigarette");
        System.out.println("  鏈夊共鎵拌瘝: " + hasInterference);
        if (hasInterference) {
            System.out.println("  骞叉壈璇? 鐑熷ご/鍚哥儫/鐑熻拏 鈫?鎺掗櫎鐏伨鍒ゆ柇");
        }

        // 濡傛灉 GLM 涓昏鎻忚堪鐨勬槸鍨冨溇鍦烘櫙锛屼笉搴斿垽涓虹伀鐏?        boolean isActuallyGarbage = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "鍨冨溇", "搴熷純鐗?, "鏉傜墿鍫?, "鍨冨溇妗?, "鍨冨溇鍫?, "鍨冨溇琚?
        }) && !containsKeywordsWithNegationCheck(glmResult, new String[]{
            "鍨冨溇鐕冪儳", "鍨冨溇鐫€鐏?, "鐒氱儳鍨冨溇"
        });
        if (isActuallyGarbage) {
            System.out.println("  鍦烘櫙瀹為檯涓哄瀮鍦惧爢绉紝鎺掗櫎鐏伨鍒ゅ畾");
        }

        boolean result = hasFireKeyword && hasGLMConfirmation && !hasInterference && !isActuallyGarbage;
        System.out.println("  鈫?鍒ゆ柇缁撴灉: " + (result ? "鉁?鏄伀鐏? : "鉂?涓嶆槸鐏伨"));

        return result;
    }
    
    private boolean isFighting(String text, String glmResult) {
        System.out.println("\n銆愭墦鏂楀啿绐佸垽鏂€?);

        // 浣跨敤鍚﹀畾鎰熺煡锛岄伩鍏?娌℃湁鑲綋鍐茬獊"琚鍒?        boolean hasFightingKeyword = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "鎵撴灦", "鎵撴枟", "鏂楁", "鑲綋鍐茬獊", "鎺ㄦ悺", "鎷夋壇", "浜掓", "娈存墦"
        });
        // 鐧惧害鏍囩琛ュ厖
        if (!hasFightingKeyword) {
            hasFightingKeyword = containsKeywords(text, new String[]{
                "fighting", "conflict", "physical fight", "brawl", "assault"
            });
        }
        System.out.println("  鍐茬獊鍏抽敭璇?鍚﹀畾鎰熺煡): " + hasFightingKeyword);

        boolean hasGLMConfirmation = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "鎵撴灦", "鎵撴枟", "鑲綋鍐茬獊", "鎺ㄦ悺", "浜掓", "鎷夋壇", "娈存墦"
        });
        if (!hasGLMConfirmation && glmResult != null) {
            hasGLMConfirmation = glmResult.contains("浜夊惖") && glmResult.contains("婵€鐑?);
        }
        System.out.println("  GLM 纭(鍚﹀畾鎰熺煡): " + hasGLMConfirmation);
        if (hasGLMConfirmation && glmResult != null) {
            System.out.println("  GLM 鍏抽敭鍙? " + extractKeyPhrase(glmResult, new String[]{"鎵撴灦", "鎵撴枟", "鎺ㄦ悺"}));
        }

        boolean hasInterference = text.contains("鎵撴壂") ||
                                  text.contains("鎵撶悊") ||
                                  text.contains("鎵撶悆") ||
                                  text.contains("鎵撶墝") ||
                                  text.contains("鎵撳お鏋?) ||
                                  text.contains("cleaning") ||
                                  text.contains("playing");
        System.out.println("  鏈夊共鎵拌瘝: " + hasInterference);
        if (hasInterference) {
            System.out.println("  骞叉壈璇? 鎵撴壂/鎵撶悊/鎵撶悆 鈫?鎺掗櫎鎵撴枟鍒ゆ柇");
        }

        // 濡傛灉 GLM 涓昏鎻忚堪鐨勬槸鎽斿€掑満鏅紙鍙湁涓€涓汉鍊掑湴锛夛紝涓嶅簲鍒や负鎵撴枟
        boolean isActuallyFall = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "鎽斿€?, "璺屽€?, "鍊掑湴", "韬哄湪鍦颁笂", "鑰佷汉鍊?
        });
        if (isActuallyFall) {
            System.out.println("  鍦烘櫙瀹為檯涓烘憯鍊掞紝鎺掗櫎鎵撴枟鍒ゅ畾");
        }

        boolean result = hasFightingKeyword && hasGLMConfirmation && !hasInterference && !isActuallyFall;
        System.out.println("  鈫?鍒ゆ柇缁撴灉: " + (result ? "鉁?鏄墦鏂楀啿绐? : "鉂?涓嶆槸鎵撴枟鍐茬獊"));

        return result;
    }
    
    private boolean isGarbageAccumulation(String text, String glmResult) {
        System.out.println("\n銆愬瀮鍦惧爢绉垽鏂€?);

        // GLM 鍚﹀畾鎰熺煡 + 鐧惧害鏍囩琛ュ厖
        boolean hasGarbageKeyword = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "鍨冨溇", "搴熷純鐗?, "鏉傜墿鍫嗙Н", "鍨冨溇妗舵孩鍑?, "搴熺墿", "鍨冨溇鍫?
        });
        if (!hasGarbageKeyword) {
            hasGarbageKeyword = containsKeywords(text, new String[]{
                "trash", "garbage", "waste", "litter"
            });
        }
        System.out.println("  鍨冨溇鍏抽敭璇?鍚﹀畾鎰熺煡): " + hasGarbageKeyword);

        // 鐢ㄥ惁瀹氭劅鐭ユ帓闄ょ伀鐏撅紝閬垮厤"娌℃湁鐏伨"琚鍒?        boolean isActuallyFire = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "鐏劙", "鐕冪儳", "鐏伨", "鐫€鐏?
        });
        System.out.println("  瀹為檯鏄伀鐏? " + isActuallyFire);

        boolean result = hasGarbageKeyword && !isActuallyFire;
        System.out.println("  鈫?鍒ゆ柇缁撴灉: " + (result ? "鉁?鏄瀮鍦惧爢绉? : "鉂?涓嶆槸鍨冨溇鍫嗙Н"));

        return result;
    }
    
    private boolean isCrowdGathering(String text, String glmResult) {
        System.out.println("\n銆愪汉鍛樿仛闆嗗垽鏂€?);
        
        boolean hasCrowdKeyword = containsKeywords(text, new String[]{
            "浜虹兢", "鑱氶泦", "鎷ユ尋", "浜哄憳瀵嗛泦", "浜哄", "浜哄憳鑱氶泦",
            "crowd", "gathering", "crowded", "many people"
        });
        System.out.println("  鑱氶泦鍏抽敭璇? " + hasCrowdKeyword);
        
        boolean hasGLMConfirmation = glmResult != null && (
            glmResult.contains("澶氫汉") ||
            glmResult.contains("浜虹兢") ||
            glmResult.contains("鑱氶泦") ||
            glmResult.contains("寰堝浜?) ||
            glmResult.matches(".*\\d+.*浜?*")
        );
        System.out.println("  GLM 纭: " + hasGLMConfirmation);
        
        boolean result = hasCrowdKeyword || hasGLMConfirmation;
        System.out.println("  鈫?鍒ゆ柇缁撴灉: " + (result ? "鉁?鏄汉鍛樿仛闆? : "鉂?涓嶆槸浜哄憳鑱氶泦"));
        
        return result;
    }
    
    private boolean isWaterAccumulation(String text, String glmResult) {
        System.out.println("\n銆愯矾闈㈢Н姘村垽鏂€?);
        
        boolean result = containsKeywords(text, new String[]{
            "绉按", "璺潰婀挎粦", "姘存笉", "璺潰鏈夋按", "鍦伴潰绉按",
            "water accumulation", "wet road", "puddle"
        });
        System.out.println("  鈫?鍒ゆ柇缁撴灉: " + (result ? "鉁?鏄矾闈㈢Н姘? : "鉂?涓嶆槸璺潰绉按"));
        
        return result;
    }
    
    private boolean isIllegalParking(String text, String glmResult) {
        System.out.println("\n銆愯溅杈嗚繚鍋滃垽鏂€?);
        
        boolean result = containsKeywords(text, new String[]{
            "杩濊鍋滆溅", "杞﹁締鍫靛", "鍗犵敤閫氶亾", "娑堥槻閫氶亾", "杩濆仠",
            "illegal parking", "blocking", "fire lane"
        });
        System.out.println("  鈫?鍒ゆ柇缁撴灉: " + (result ? "鉁?鏄溅杈嗚繚鍋? : "鉂?涓嶆槸杞﹁締杩濆仠"));
        
        return result;
    }
    
    private boolean isFacilityDamage(String text, String glmResult) {
        System.out.println("\n銆愯鏂芥崯鍧忓垽鏂€?);
        
        boolean result = containsKeywords(text, new String[]{
            "鐮存崯", "鎹熷潖", "鍊掑", "鐮磋", "鐮村潖", "鎹熸瘉",
            "damage", "broken", "collapsed", "destroyed"
        });
        System.out.println("  鈫?鍒ゆ柇缁撴灉: " + (result ? "鉁?鏄鏂芥崯鍧? : "鉂?涓嶆槸璁炬柦鎹熷潖"));
        
        return result;
    }
    
    private boolean isLeafletDistribution(String text, String glmResult) {
        System.out.println("\n銆愬彂浼犲崟鍒ゆ柇銆?);

        // =====================================================================
        // 绛栫暐锛欸LM 浼氱敤鍚勭涓嶅悓璇存硶鎻忚堪"鍙戜紶鍗?杩欎釜琛屼负锛屽繀椤诲叏閮ㄨ鐩栥€?        // 鏍规嵁瀹為檯鏃ュ織锛孏LM 璇寸殑鏄?閫掗€佷紶鍗曟垨瀹ｄ紶鍐?銆?鎵嬮噷鎷跨潃鍑犲紶绾?锛?        // 杩欎簺璇嶉兘涓嶅湪鍘熸潵鐨勫叧閿瘝鍒楄〃閲岋紝鎵€浠ュ叏閮ㄦ紡鍒ゃ€?        //
        // 鏂板浼樺寲锛?026-04-18锛夛細
        //   澧炲姞绗?5 灞傛娴?- 澶氫汉鍦烘櫙 + 绾稿紶绫荤墿鍝?= 鍙戜紶鍗?        //   瑙ｅ喅鍦烘櫙锛氫袱涓汉浜ゆ祦锛屽叾涓竴浜烘嬁鐫€绾?浼犲崟
        // =====================================================================

        // 1. 鐩存帴鍛戒腑鍏抽敭璇嶏紙鏈€鏄庣‘锛?        boolean hasDirectKeyword = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "鍙戜紶鍗?, "鍒嗗彂浼犲崟", "鏁ｅ彂浼犲崟", "鍙戝浼犲崟", "鏁ｅ彂骞垮憡",
            "閫掗€佷紶鍗?, "鍒嗗彂瀹ｄ紶", "瀹ｄ紶鍐?, "瀹ｄ紶鍗?, "浼犲崟",
            "leaflet", "distributing flyers", "handing out flyers", "pamphlet",
            "娲惧彂浼犲崟", "鍙戞斁浼犲崟", "鍙戞斁瀹ｄ紶", "娲惧彂瀹ｄ紶", "瀹ｄ紶"
        });
        System.out.println("  鐩存帴鍛戒腑鍏抽敭璇? " + hasDirectKeyword);

        // 2. 琛屼负鐗瑰緛缁勫悎璇嗗埆锛氥€岄€?閫?鍙?鍒嗗彂/娲惧彂銆?銆岀焊/鍐?鍗曘€?        //    GLM 瀹為檯璇存硶锛?閫掗€佷紶鍗曟垨瀹ｄ紶鍐?銆?鍚戝彟涓€涓汉閫掗€?
        boolean hasHandingAction = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "閫掗€?, "閫掔粰", "鍒嗗彂", "娲惧彂", "鏁ｅ彂", "鍙戞斁", "鍙戞斁",
            "handing", "distributing", "passing out", "giving out", "鍚?, "閫?
        });
        boolean hasPaperObject = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "浼犲崟", "瀹ｄ紶鍐?, "瀹ｄ紶鍗?, "瀹ｄ紶鏉愭枡", "骞垮憡鍗?, "灏忓唽瀛?,
            "绾稿紶", "绾?, "鍐?, "鍗曢〉", "flyer", "brochure", "paper", "pamphlet"
        });
        boolean hasBehaviorCombo = hasHandingAction && hasPaperObject;
        System.out.println("  琛屼负缁勫悎(閫掗€佸姩浣?" + hasHandingAction + " + 绾稿紶绫荤墿鍝?" + hasPaperObject + "): " + hasBehaviorCombo);

        // 3. 鍦烘櫙鐗瑰緛璇嗗埆锛氭墜鎸佺焊寮?+ 闈㈠悜浠栦汉鐨勪氦浜?        //    GLM 瀹為檯璇存硶锛?鎵嬮噷鎷跨潃鍑犲紶绾?+ 涓や汉浜ゆ祦鍦烘櫙
        boolean holdingPaper = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "鎵嬮噷鎷跨潃", "鎵嬫寔", "鎷跨潃绾?, "鎷跨潃鍑犲紶", "鎸佹湁绾稿紶", "鎷跨潃"
        });
        boolean facingOther = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "鍚戝彟涓€涓?, "閫掔粰鍙?, "闈㈠悜瀵规柟", "涓や汉", "浜ょ粰", "浜?, "鍒嗗彂"
        });
        boolean hasSceneCombo = holdingPaper && facingOther;
        System.out.println("  鍦烘櫙缁勫悎(鎵嬫寔绾?" + holdingPaper + " + 闈㈠悜浠栦汉=" + facingOther + "): " + hasSceneCombo);

        // 4. 瀹ｄ紶/娲诲姩鍦烘櫙璇嗗埆锛氬浼犳椿鍔?+ 浜虹墿浜掑姩
        boolean hasPromotion = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "瀹ｄ紶", "鎺ㄥ箍", "钀ラ攢", "娲诲姩", "淇冮攢", "娲惧彂"
        });
        boolean hasInteraction = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "浜ゆ祦", "浜掑姩", "浜ゆ崲", "閫掔粰", "缁?, "浜ゆ帴"
        });
        boolean hasPromoCombo = hasPromotion && hasInteraction;
        System.out.println("  瀹ｄ紶缁勫悎(瀹ｄ紶=" + hasPromotion + " + 浜掑姩=" + hasInteraction + "): " + hasPromoCombo);

        // 5. 銆愭柊澧炪€戝浜哄満鏅?+ 绾稿紶绫荤墿鍝?= 鍙戜紶鍗曪紙楂樺噯纭巼锛?        //    瑙ｅ喅鍦烘櫙锛氬浘鐗囦腑鏈?2 浜哄強浠ヤ笂锛屼笖鍏朵腑涓€浜烘嬁鐫€绾?浼犲崟/瀹ｄ紶鍐?        //    GLM 鍙兘鎻忚堪涓猴細"鍥句腑鏄剧ず浜嗕袱涓汉...鍏朵腑涓€涓汉鎷跨潃鍑犲紶绾?
        boolean hasMultiplePeople = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "涓や釜浜?, "涓や汉", "涓や釜", "澶氫釜浜?, "鍑犱釜浜?, "2浜?, "澶氫汉",
            "涓ゅ悕", "涓や綅", "涓変汉", "three people", "two people", "multiple people",
            "涓€涓?*浜?*鍙︿竴涓?, "涓€鍚?*鍙︿竴鍚?, "涓€浜?*鍙︿竴浜?, "鍏朵腑涓€浜?
        }) || (glmResult.contains("浜?) && (glmResult.contains("鍙︿竴涓?) || glmResult.contains("鍙︿竴")));
        
        boolean hasAnyPaperItem = containsKeywordsWithNegationCheck(glmResult, new String[]{
            "绾?, "绾稿紶", "浼犲崟", "瀹ｄ紶鍐?, "瀹ｄ紶鍗?, "骞垮憡鍗?, "灏忓唽瀛?,
            "鍗曢〉", "flyer", "brochure", "paper", "pamphlet", "鏂囦欢", "璧勬枡"
        });
        
        boolean hasPeopleAndPaperCombo = hasMultiplePeople && hasAnyPaperItem;
        System.out.println("  澶氫汉+绾稿紶缁勫悎(澶氫汉=" + hasMultiplePeople + " + 绾稿紶=" + hasAnyPaperItem + "): " + hasPeopleAndPaperCombo);
        if (hasMultiplePeople) {
            System.out.println("    鈫?妫€娴嬪埌澶氫汉鍦烘櫙");
        }
        if (hasAnyPaperItem) {
            System.out.println("    鈫?妫€娴嬪埌绾稿紶绫荤墿鍝?);
        }

        // 鏈€缁堬細浜斾釜鏉′欢浠讳竴婊¤冻鍗冲垽瀹氫负鍙戜紶鍗?        boolean result = hasDirectKeyword || hasBehaviorCombo || hasSceneCombo || hasPromoCombo || hasPeopleAndPaperCombo;
        System.out.println("  鈫?鍒ゆ柇缁撴灉: " + (result ? "鉁?鏄彂浼犲崟" : "鉂?涓嶆槸鍙戜紶鍗?));

        return result;
    }
    
    private boolean isPetAttack(String text, String glmResult) {
        System.out.println("\n銆愬疇鐗╀激浜哄垽鏂€?);
        
        boolean hasPetKeyword = containsKeywords(text, new String[]{
            "瀹犵墿", "鐙?, "鐚?, "鍔ㄧ墿", "pet", "dog", "animal"
        });
        System.out.println("  瀹犵墿鍏抽敭璇? " + hasPetKeyword);
        
        boolean hasAttackKeyword = containsKeywords(text, new String[]{
            "鍜?, "浼や汉", "鏀诲嚮", "杩介€?, "鎵?, "鍜激",
            "bite", "attack", "chasing", "aggressive"
        });
        System.out.println("  鏀诲嚮鍏抽敭璇? " + hasAttackKeyword);
        
        boolean result = hasPetKeyword && hasAttackKeyword;
        System.out.println("  鈫?鍒ゆ柇缁撴灉: " + (result ? "鉁?鏄疇鐗╀激浜? : "鉂?涓嶆槸瀹犵墿浼や汉"));
        
        return result;
    }
    
    private Map<String, String> buildRiskResult(String riskType, String riskLevel, String description) {
        Map<String, String> result = new HashMap<>();
        result.put("riskType", riskType);
        result.put("riskLevel", riskLevel);
        result.put("description", description);
        
        return result;
    }
    
    private boolean containsKeywords(String text, String[] keywords) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 鍚﹀畾鎰熺煡鐨勫叧閿瘝鍖归厤锛氬鏋滃叧閿瘝鍓嶉潰绱ц窡鍚﹀畾璇嶏紙娌℃湁銆佷笉鏄€佹湭銆佹棤銆佷笉瀛樺湪銆佸苟闈炪€佹病锛夛紝
     * 鍒欎笉绠楀尮閰嶃€傝В鍐?GLM 鍥炲"娌℃湁浜鸿汉鍦ㄥ湴涓?琚鍒や负鍖呭惈"韬哄湪鍦颁笂"鐨勯棶棰樸€?     */
    private boolean containsKeywordsWithNegationCheck(String text, String[] keywords) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        String[] negationPrefixes = {"娌℃湁", "涓嶆槸", "鏈彂鐜?, "鏈娴嬪埌", "鏈", "涓嶅瓨鍦?, "骞堕潪", "娌?, "鏃?, "鏈?};

        for (String keyword : keywords) {
            int index = 0;
            while ((index = text.indexOf(keyword, index)) >= 0) {
                boolean negated = false;
                for (String neg : negationPrefixes) {
                    int negStart = index - neg.length();
                    if (negStart >= 0 && text.substring(negStart, index).equals(neg)) {
                        negated = true;
                        System.out.println("    鍚﹀畾鎰熺煡: 銆? + neg + keyword + "銆嶁啋 蹇界暐");
                        break;
                    }
                }
                if (!negated) {
                    return true;
                }
                index += keyword.length();
            }
        }
        return false;
    }
    
    private String extractKeyPhrase(String text, String[] keywords) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        String[] sentences = text.split("[銆傦紒锛焅\n]");
        
        for (String sentence : sentences) {
            for (String keyword : keywords) {
                if (sentence.contains(keyword)) {
                    if (sentence.length() > 50) {
                        int index = sentence.indexOf(keyword);
                        int start = Math.max(0, index - 15);
                        int end = Math.min(sentence.length(), index + 35);
                        return "..." + sentence.substring(start, end) + "...";
                    }
                    return sentence.trim();
                }
            }
        }
        
        return "";
    }
    
    @Override
    public Map<String, String> recognizeMotion(List<String> imageUrls, String location) {
        Map<String, String> result = new HashMap<>();
        result.put("actionType", "姝ｅ父娲诲姩");
        result.put("riskLevel", "LOW");
        result.put("description", "鏈娴嬪埌寮傚父鍔ㄤ綔");
        result.put("frameAnalysis", "");
        
        try {
            System.out.println("=== 寮€濮嬭繛缁抚鍔ㄤ綔璇嗗埆 ===");
            System.out.println("鍥剧墖鏁伴噺锛? + imageUrls.size());
            System.out.println("浣嶇疆锛? + (location != null ? location : "鏈寚瀹?));
            
            if (imageUrls.size() < 3) {
                System.out.println("甯ф暟涓嶈冻锛屾棤娉曞垎鏋?);
                return result;
            }
            
            List<String> localFilePaths = new ArrayList<>();
            for (String imageUrl : imageUrls) {
                String localPath = imageUrl;
                if (imageUrl.startsWith("/upload/")) {
                    // 瑙ｆ瀽鐩稿璺緞锛屾敮鎸?monitor 鍜?images 鐩綍
                    String pathParts = imageUrl.substring("/upload/".length());
                    localPath = "c:/Users/fengchunzhao/Desktop/future-community-system/backend/upload/" + pathParts;
                }
                localFilePaths.add(localPath);
            }
            
            List<String> frameDescriptions = new ArrayList<>();
            StringBuilder combinedAnalysis = new StringBuilder();
            
            for (int i = 0; i < localFilePaths.size(); i++) {
                try {
                    System.out.println("鍒嗘瀽绗?" + (i + 1) + " 甯?..");
                    String frameDesc = modelStrategy.callVisionService(localFilePaths.get(i), getMotionPrompt(i));
                    frameDescriptions.add(frameDesc);
                    combinedAnalysis.append("绗?).append(i + 1).append("甯э紙").append(i).append("绉掞級锛?).append(frameDesc).append("\n");
                } catch (Exception e) {
                    System.err.println("绗?" + (i + 1) + " 甯?GLM 鍒嗘瀽澶辫触锛? + e.getMessage());
                    frameDescriptions.add("鍒嗘瀽澶辫触");
                }
            }
            
            String motionPrompt = buildMotionAnalysisPrompt(frameDescriptions);
            
            System.out.println("=== 鍔ㄤ綔搴忓垪鍒嗘瀽 ===");
            String motionAnalysis = "";
            try {
                motionAnalysis = modelStrategy.callVisionService(localFilePaths.get(0), motionPrompt);
                System.out.println("鍔ㄤ綔鍒嗘瀽缁撴灉锛? + motionAnalysis);
            } catch (Exception e) {
                System.err.println("鍔ㄤ綔鍒嗘瀽澶辫触锛? + e.getMessage());
                motionAnalysis = "鍔ㄤ綔鍒嗘瀽澶辫触";
            }
            
            Map<String, String> actionResult = parseMotionAnalysis(motionAnalysis, combinedAnalysis.toString());
            
            result.put("actionType", actionResult.get("actionType"));
            result.put("riskLevel", actionResult.get("riskLevel"));
            result.put("description", actionResult.get("description"));
            result.put("frameAnalysis", combinedAnalysis.toString());
            
            System.out.println("鏈€缁堝姩浣滆瘑鍒粨鏋滐細");
            System.out.println("  鍔ㄤ綔绫诲瀷锛? + result.get("actionType"));
            System.out.println("  椋庨櫓绛夌骇锛? + result.get("riskLevel"));
            
            String riskLevel = actionResult.get("riskLevel");
            if ("HIGH".equals(riskLevel) || "MEDIUM".equals(riskLevel)) {
                System.out.println("妫€娴嬪埌涓珮椋庨櫓锛屽紑濮嬭嚜鍔ㄥ垱寤洪闄╀簨浠?..");
                
                RiskEvent riskEvent = riskEventAutoCreateService.autoCreateRiskEvent(
                    actionResult.get("actionType"),
                    riskLevel,
                    location != null ? location : "鐩戞帶鍖哄煙",
                    localFilePaths.get(0),
                    actionResult.get("description"),
                    "camera_" + (location != null ? location : "unknown"),
                    motionAnalysis,
                    ""
                );
                
                if (riskEvent != null) {
                    System.out.println("椋庨櫓浜嬩欢鑷姩鍒涘缓鎴愬姛锛屼簨浠?ID: " + riskEvent.getId());
                    result.put("riskEventId", String.valueOf(riskEvent.getId()));
                } else {
                    System.out.println("椋庨櫓浜嬩欢鏈垱寤猴紙鍙兘 5 鍒嗛挓鍐呮湁閲嶅锛?);
                }
            }
            
            System.out.println("========================");
            
            return result;
            
        } catch (Exception e) {
            System.err.println("鍔ㄤ綔璇嗗埆寮傚父锛? + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }
    
    private String getMotionPrompt(int frameIndex) {
        return "璇峰垎鏋愯繖寮犵洃鎺у浘鐗囷紙杩炵画甯т腑鐨勭" + (frameIndex + 1) + "甯э級锛歕n" +
               "1. 浜虹墿鐨勬暟閲忋€佷綅缃拰鍒嗗竷\n" +
               "2. 浜虹墿鐨勫Э鎬佸拰鍔ㄤ綔锛堢珯绔嬨€佸集鑵般€佸€掑湴銆佸璺戠瓑锛塡n" +
               "3. 浜虹墿鐨勯潰閮ㄨ〃鎯呭拰鑲綋璇█\n" +
               "4. 鐜涓殑鐗╀綋鍜岃鏂絓n" +
               "5. 浠讳綍寮傚父鎯呭喌鎴栨綔鍦ㄩ闄‐n" +
               "\n" +
               "璇疯缁嗘弿杩板浘鐗囦腑鐨勫満鏅紝鐗瑰埆鍏虫敞浜虹墿鐨勫姩浣滃拰浣嶇疆銆?;
    }
    
    private String buildMotionAnalysisPrompt(List<String> frameDescriptions) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("鎴戞湁杩炵画 3 甯х洃鎺у浘鐗囷紙姣忕 1 甯э級锛歕n\n");
        
        for (int i = 0; i < frameDescriptions.size(); i++) {
            prompt.append("绗?).append(i + 1).append("甯э紙").append(i).append("绉掞級锛?).append(frameDescriptions.get(i)).append("\n\n");
        }
        
        prompt.append("璇峰垎鏋愯繖涓姩浣滃簭鍒楋細\n");
        prompt.append("1. 銆愰瑕佹鏌ャ€戞槸鍚﹀瓨鍦ㄧ伀鐏俱€佹槑鐏€佹祿鐑熴€佺噧鐑х瓑鍗遍櫓鍦烘櫙锛堜紭鍏堢骇鏈€楂橈級\n");
        prompt.append("2. 浜虹墿鐨勪綅缃拰濮挎€佸彉鍖栵紙浠庣 1 甯у埌绗?3 甯х殑杩囩▼锛塡n");
        prompt.append("3. 鏄惁瀛樺湪鎽斿€掑姩浣滐紙浠庣珯绔?鈫?寮叞 鈫?鍊掑湴鐨勮繃绋嬶級\n");
        prompt.append("4. 鏄惁鏈夊紓甯哥殑蹇€熺Щ鍔ㄦ垨濂旇窇锛堣繛缁綅绉伙級\n");
        prompt.append("5. 鏄惁鏈夎偄浣撳啿绐佹垨鎵撴枟杩硅薄锛堟帹鎼°€佹媺鎵€佹敾鍑诲姩浣滐級\n");
        prompt.append("6. 鏄惁鏈夊姩鐗╄拷閫愭垨瀹犵墿鏀诲嚮琛屼负\n");
        prompt.append("7. 鍏朵粬寮傚父鍔ㄤ綔鎴栧嵄闄╄涓篭n");
        prompt.append("\n");
        prompt.append("濡傛灉鍙戠幇鐏伨鎴栨祿鐑燂紝璇疯繑鍥烇細\n");
        prompt.append("- 鍔ㄤ綔绫诲瀷锛氱伀鐏綷n");
        prompt.append("- 椋庨櫓绛夌骇锛欻IGH\n");
        prompt.append("- 鎻忚堪锛氭娴嬪埌鐏伨鎴栨祿鐑燂紝瀛樺湪涓ラ噸瀹夊叏闅愭偅\n");
        prompt.append("\n");
        prompt.append("濡傛灉鍙戠幇鎽斿€掑姩浣滐紝璇疯繑鍥烇細\n");
        prompt.append("- 鍔ㄤ綔绫诲瀷锛氭憯鍊抃n");
        prompt.append("- 椋庨櫓绛夌骇锛欻IGH\n");
        prompt.append("- 鎻忚堪锛氭娴嬪埌浠庣珯绔嬪埌鍊掑湴鐨勫姩浣滃簭鍒梊n");
        prompt.append("\n");
        prompt.append("濡傛灉鍔ㄤ綔姝ｅ父锛岃杩斿洖锛歕n");
        prompt.append("- 鍔ㄤ綔绫诲瀷锛氭甯告椿鍔╘n");
        prompt.append("- 椋庨櫓绛夌骇锛歀OW\n");
        prompt.append("- 鎻忚堪锛氭湭妫€娴嬪埌寮傚父鍔ㄤ綔\n");
        
        return prompt.toString();
    }
    
    private Map<String, String> parseMotionAnalysis(String motionAnalysis, String frameAnalysis) {
        Map<String, String> result = new HashMap<>();
        String text = motionAnalysis.toLowerCase();
        
        System.out.println("=== 瑙ｆ瀽鍔ㄤ綔鍒嗘瀽缁撴灉 ===");
        System.out.println("鍒嗘瀽鏂囨湰锛? + text);
        
        // 浼樺厛妫€娴嬬伀鐏撅紙浼樺厛绾ф渶楂橈級
        if (containsKeywords(text, new String[]{
            "鐏伨", "鏄庣伀", "娴撶儫", "鐕冪儳", "鐏劙", "鐏厜", "fire", "smoke", "flame", "burning"
        })) {
            result.put("actionType", "鐏伨");
            result.put("riskLevel", "HIGH");
            result.put("description", generateMotionDescription("鐏伨", "HIGH", 
                "妫€娴嬪埌鐏伨鎴栨祿鐑燂紝瀛樺湪涓ラ噸瀹夊叏闅愭偅", 
                "绔嬪嵆鍚姩鐏伨搴旀€ラ妗堬紝鐤忔暎浜哄憳锛岃仈绯绘秷闃查儴闂?));
            return result;
        }
        
        if (containsKeywords(text, new String[]{
            "鎽斿€?, "鍊掑湴", "璺屽€?, "fall", "fallen", "浠庣珯绔嬪埌鍊掑湴", "浠庣洿绔嬪埌鍊掑湴"
        })) {
            result.put("actionType", "鎽斿€掑姩浣?);
            result.put("riskLevel", "HIGH");
            result.put("description", generateMotionDescription("鎽斿€掑姩浣?, "HIGH", 
                "妫€娴嬪埌浠庣珯绔嬪埌鍊掑湴鐨勫姩浣滃簭鍒楋紝鍙兘瀛樺湪浜哄憳鎽斿€?, 
                "绔嬪嵆鍓嶅線鏌ョ湅骞舵彁渚涘府鍔╋紝蹇呰鏃惰仈绯诲尰鐤楁晳鎻?));
            return result;
        }
        
        if (containsKeywords(text, new String[]{
            "濂旇窇", "蹇窇", "蹇€熺Щ鍔?, "running", "sprinting", "蹇€熶綅绉?
        })) {
            result.put("actionType", "濂旇窇鍔ㄤ綔");
            result.put("riskLevel", "MEDIUM");
            result.put("description", generateMotionDescription("濂旇窇鍔ㄤ綔", "MEDIUM", 
                "妫€娴嬪埌寮傚父鐨勫揩閫熺Щ鍔ㄦ垨濂旇窇琛屼负", 
                "鍓嶅線鏌ョ湅濂旇窇鍘熷洜锛岀淮鎸佺З搴?));
            return result;
        }
        
        if (containsKeywords(text, new String[]{
            "鎵撴枟", "鎵撴灦", "鑲綋鍐茬獊", "鎺ㄦ悺", "鎷夋壇", "fighting", "conflict", "鏀诲嚮"
        })) {
            result.put("actionType", "鎵撴枟鍔ㄤ綔");
            result.put("riskLevel", "HIGH");
            result.put("description", generateMotionDescription("鎵撴枟鍔ㄤ綔", "HIGH", 
                "妫€娴嬪埌鑲綋鍐茬獊鎴栨墦鏂楄涓?, 
                "绔嬪嵆鍓嶅線鍒舵骞惰皟瑙ｏ紝蹇呰鏃惰仈绯诲叕瀹夐儴闂?));
            return result;
        }
        
        if (containsKeywords(text, new String[]{
            "瀹犵墿", "鍔ㄧ墿", "鐙?, "杩介€?, "杩借刀", "pet", "dog", "animal", "chasing"
        })) {
            result.put("actionType", "瀹犵墿杩介€?);
            result.put("riskLevel", "LOW");
            result.put("description", generateMotionDescription("瀹犵墿杩介€?, "LOW", 
                "妫€娴嬪埌瀹犵墿鎴栧姩鐗╄拷閫愯涓?, 
                "鍏虫敞瀹犵墿鏄惁鐗电怀锛岄槻姝激浜轰簨浠?));
            return result;
        }
        
        result.put("actionType", "姝ｅ父娲诲姩");
        result.put("riskLevel", "LOW");
        result.put("description", "杩炵画甯у垎鏋愭樉绀烘甯告椿鍔紝鏈娴嬪埌寮傚父鍔ㄤ綔銆?);
        
        return result;
    }
    
    private String generateMotionDescription(String actionType, String riskLevel, String situation, String suggestion) {
        String levelText = "浣?;
        if ("MEDIUM".equals(riskLevel)) {
            levelText = "涓?;
        } else if ("HIGH".equals(riskLevel)) {
            levelText = "楂?;
        }
        
        StringBuilder description = new StringBuilder();
        description.append("鍔ㄤ綔璇嗗埆妫€娴嬪埌銆?).append(actionType).append("銆戯紝");
        description.append("椋庨櫓绛夌骇锛?).append(levelText).append("椋庨櫓锛?);
        description.append(situation).append("锛?);
        description.append("寤鸿鐗╀笟浜哄憳").append(suggestion).append("銆?);
        
        return description.toString();
    }
}


