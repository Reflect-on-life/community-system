package com.example.futurecommunity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.futurecommunity.model.Express;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ExpressService extends IService<Express> {
    Express createExpressWithPickupCode(Express express);
    Express pickupExpress(String pickupCode);
    List<Express> listByUserId(Long userId);
    
    // 获取指定时间范围内的每日入库数量
    List<Map<String, Object>> getDailyInboundCount(LocalDateTime start, LocalDateTime end);
    
    // 获取指定时间范围内的每日出库数量
    List<Map<String, Object>> getDailyOutboundCount(LocalDateTime start, LocalDateTime end);
}
