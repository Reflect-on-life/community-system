package com.example.futurecommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.futurecommunity.model.AlarmRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 行为告警记录 Mapper 接口
 */
@Mapper
public interface AlarmRecordMapper extends BaseMapper<AlarmRecord> {
}
