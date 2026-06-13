package com.example.futurecommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.futurecommunity.model.ActivityCheckin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动签到 Mapper 接口
 */
@Mapper
public interface ActivityCheckinMapper extends BaseMapper<ActivityCheckin> {
}
