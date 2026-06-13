package com.example.futurecommunity.model.dto;

import lombok.Data;

/**
 * 告警列表查询请求 DTO
 */
@Data
public class AlarmListRequest {

    /**
     * 当前页码（从 1 开始）
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

    /**
     * 告警类型筛选（可选）
     */
    private String alarmType;

    /**
     * 开始时间（可选，格式：yyyy-MM-dd HH:mm:ss）
     */
    private String startTime;

    /**
     * 结束时间（可选，格式：yyyy-MM-dd HH:mm:ss）
     */
    private String endTime;
}
