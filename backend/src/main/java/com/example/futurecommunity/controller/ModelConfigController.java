package com.example.futurecommunity.controller;

import com.example.futurecommunity.config.ModelPreset;
import com.example.futurecommunity.config.ModelPresetConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 模型配置控制器
 * 供前端获取/切换 AI 模型预置方案
 */
@Slf4j
@RestController
@RequestMapping("/api/model-config")
public class ModelConfigController {

    @Autowired
    private ModelPresetConfig presetConfig;

    /**
     * 获取所有可用的模型预置方案
     */
    @GetMapping("/presets")
    public ResponseEntity<Map<String, Object>> getPresets() {
        List<Map<String, Object>> presets = new ArrayList<>();
        for (ModelPreset preset : ModelPreset.values()) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("id", preset.name());
            info.put("name", preset.getDisplayName());
            info.put("models", preset.getModels());
            info.put("description", preset.getDescription());
            presets.add(info);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("presets", presets);
        result.put("current", presetConfig.getCurrentPreset().name());
        return ResponseEntity.ok(result);
    }

    /**
     * 获取当前模型预置
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrent() {
        ModelPreset current = presetConfig.getCurrentPreset();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", current.name());
        result.put("name", current.getDisplayName());
        result.put("models", current.getModels());
        result.put("description", current.getDescription());
        return ResponseEntity.ok(result);
    }

    /**
     * 切换模型预置方案
     */
    @PostMapping("/switch")
    public ResponseEntity<Map<String, Object>> switchPreset(@RequestBody Map<String, String> request) {
        String presetName = request.get("preset");
        if (presetName == null || presetName.isEmpty()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("message", "缺少 preset 参数");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            ModelPreset preset = ModelPreset.valueOf(presetName.toUpperCase());
            presetConfig.setCurrentPreset(preset);`n            persistPreset(preset);
            log.info("模型预置已切换为: {} ({})", preset.getDisplayName(), preset.getModels());

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("current", preset.name());
            result.put("name", preset.getDisplayName());
            result.put("models", preset.getModels());
            result.put("message", "已切换为 " + preset.getDisplayName());
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("message", "无效的预置名称: " + presetName);
            return ResponseEntity.badRequest().body(error);
        }
    }

    private void persistPreset(ModelPreset preset) {
        try {
            java.nio.file.Path configFile = java.nio.file.Paths.get(
                System.getProperty("user.dir"), "config", "model-preset.json");
            java.nio.file.Files.createDirectories(configFile.getParent());
            String json = "{\"preset\":\"" + preset.name() + "\"}";
            java.nio.file.Files.writeString(configFile, json);
        } catch (Exception e) {
            log.warn("模型预设持久化失败: {}", e.getMessage());
        }
    }
}
