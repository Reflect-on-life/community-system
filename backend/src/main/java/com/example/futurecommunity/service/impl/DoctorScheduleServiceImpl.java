package com.example.futurecommunity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.futurecommunity.mapper.DoctorScheduleMapper;
import com.example.futurecommunity.model.DoctorSchedule;
import com.example.futurecommunity.service.DoctorScheduleService;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class DoctorScheduleServiceImpl extends ServiceImpl<DoctorScheduleMapper, DoctorSchedule> implements DoctorScheduleService {

    @Override
    public List<DoctorSchedule> getScheduleByDoctorId(Long doctorId) {
        return lambdaQuery()
                .eq(DoctorSchedule::getDoctorId, doctorId)
                .orderByAsc(DoctorSchedule::getScheduleDate)
                .orderByAsc(DoctorSchedule::getStartTime)
                .list();
    }

    @Override
    public List<DoctorSchedule> getScheduleByDoctorIdAndDate(Long doctorId, Date scheduleDate) {
        return lambdaQuery()
                .eq(DoctorSchedule::getDoctorId, doctorId)
                .eq(DoctorSchedule::getScheduleDate, scheduleDate)
                .orderByAsc(DoctorSchedule::getStartTime)
                .list();
    }

    @Override
    public DoctorSchedule createSchedule(DoctorSchedule schedule) {
        save(schedule);
        return schedule;
    }

    @Override
    public DoctorSchedule updateSchedule(DoctorSchedule schedule) {
        updateById(schedule);
        return schedule;
    }

    @Override
    public boolean deleteSchedule(Long id) {
        return removeById(id);
    }
}
