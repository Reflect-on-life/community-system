package com.example.futurecommunity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.futurecommunity.model.DoctorSchedule;
import java.util.Date;
import java.util.List;

public interface DoctorScheduleService extends IService<DoctorSchedule> {
    /**
     * 获取医生的排班列表
     * @param doctorId 医生ID
     * @return 排班列表
     */
    List<DoctorSchedule> getScheduleByDoctorId(Long doctorId);

    /**
     * 获取医生在指定日期的排班
     * @param doctorId 医生ID
     * @param scheduleDate 排班日期
     * @return 排班列表
     */
    List<DoctorSchedule> getScheduleByDoctorIdAndDate(Long doctorId, Date scheduleDate);

    /**
     * 创建医生排班
     * @param schedule 排班信息
     * @return 创建后的排班
     */
    DoctorSchedule createSchedule(DoctorSchedule schedule);

    /**
     * 更新医生排班
     * @param schedule 排班信息
     * @return 更新后的排班
     */
    DoctorSchedule updateSchedule(DoctorSchedule schedule);

    /**
     * 删除医生排班
     * @param id 排班ID
     * @return 是否删除成功
     */
    boolean deleteSchedule(Long id);
}
