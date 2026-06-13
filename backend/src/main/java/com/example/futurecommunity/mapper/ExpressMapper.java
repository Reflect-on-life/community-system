package com.example.futurecommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.futurecommunity.model.Express;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ExpressMapper extends BaseMapper<Express> {
    // 获取指定时间范围内的每日入库数量
    List<Map<String, Object>> getDailyInboundCount(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // 获取指定时间范围内的每日出库数量
    List<Map<String, Object>> getDailyOutboundCount(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
