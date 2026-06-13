package com.example.futurecommunity.model.dto;

import lombok.Data;

/**
 * 行为识别请求 DTO
 */
@Data
public class DetectRequest {

    /**
     * Base64 编码的图片
     */
    private String image;

    /**
     * 摄像头位置（可选）
     */
    private String location;
}
