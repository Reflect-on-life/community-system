package com.example.futurecommunity.enums;

/**
 * 维修工单状态枚举
 */
public enum RepairStatus {
    /**
     * 待派单 - 居民提交报修申请后初始状态
     */
    PENDING("待派单"),
    
    /**
     * 已派单/待接单 - 物业已分配维修师傅，等待维修师傅接单
     */
    ASSIGNED("已派单"),
    
    /**
     * 处理中 - 维修师傅已接单并开始处理
     */
    PROCESSING("处理中"),
    
    /**
     * 已完成 - 维修师傅完成维修
     */
    COMPLETED("已完成"),
    
    /**
     * 已取消 - 工单被取消
     */
    CANCELLED("已取消");
    
    private final String description;
    
    RepairStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据字符串获取枚举
     */
    public static RepairStatus fromString(String status) {
        if (status == null) {
            return null;
        }
        try {
            return valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
