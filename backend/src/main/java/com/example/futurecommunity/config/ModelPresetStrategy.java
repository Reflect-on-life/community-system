package com.example.futurecommunity.config;

import com.baidu.aip.imageclassify.AipImageClassify;
import com.example.futurecommunity.service.GLMService;
import com.example.futurecommunity.service.VisionService;
import com.example.futurecommunity.service.YoloDetectorService;
import com.example.futurecommunity.service.impl.GeminiVLServiceImpl;
import com.example.futurecommunity.service.impl.QwenVLServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 模型预设策略路由器
 * 根据当前选中的预设方案，返回合适的 VLM 和检测服务
 */
@Slf4j
@Component
public class ModelPresetStrategy {

    @Autowired
    private ModelPresetConfig config;

    @Autowired
    private GLMService glmService;

    @Autowired
    private QwenVLServiceImpl qwenVLService;

    @Autowired
    private GeminiVLServiceImpl geminiVLService;

    @Autowired
    private YoloDetectorService yoloDetectorService;

    @Autowired
    private AipImageClassify aipClient;

    /**
     * 根据当前预设获取 VLM 服务
     */
    public VisionService getVisionService() {
        ModelPreset preset = config.getCurrentPreset();
        return switch (preset) {
            case ECONOMY -> qwenVLService;
            case PREMIUM -> geminiVLService;
            default -> null; // ORIGINAL: 使用 GLMService（独立接口）
        };
    }

    /**
     * 获取 GLM 服务（原版专用）
     */
    public GLMService getGlmService() {
        return glmService;
    }

    /**
     * 获取百度 AI 客户端（原版专用）
     */
    public AipImageClassify getBaiduClient() {
        return aipClient;
    }

    /**
     * 获取 YOLO 检测服务（省钱版和最强版使用）
     */
    public YoloDetectorService getYoloService() {
        return yoloDetectorService;
    }

    /**
     * 当前是否使用百度 AI（仅原版）
     */
    public boolean useBaiduAI() {
        return config.getCurrentPreset() == ModelPreset.ORIGINAL;
    }

    /**
     * 当前是否使用 YOLO
     */
    public boolean useYolo() {
        ModelPreset preset = config.getCurrentPreset();
        return preset == ModelPreset.ECONOMY || preset == ModelPreset.PREMIUM;
    }

    /**
     * 当前预设名称
     */
    public ModelPreset getCurrentPreset() {
        return config.getCurrentPreset();
    }

    /**
     * 获取检测层描述（用于日志）
     */
    public String getDetectionLayerName() {
        return useYolo() ? "YOLOv11" : "百度AI";
    }

    /**
     * 获取 VLM 层名称（用于日志）
     */
    public String getVisionLayerName() {
        return switch (config.getCurrentPreset()) {
            case ORIGINAL -> "GLM-4V";
            case ECONOMY -> "Qwen-VL";
            case PREMIUM -> "Gemini 2.5 Flash";
        };
    }

    /**
     * 调用 VLM 分析图片（根据预设自动选择后端）
     */
    public String callVisionService(String imagePath, String prompt) {
        ModelPreset preset = config.getCurrentPreset();
        log.info("使用预设 [{}] 调用 VLM: {}", preset.getDisplayName(), getVisionLayerName());

        if (preset == ModelPreset.ORIGINAL) {
            return glmService.analyzeImageWithPrompt(imagePath, prompt);
        } else {
            VisionService vs = getVisionService();
            return vs.analyzeImageWithPrompt(imagePath, prompt);
        }
    }
}
