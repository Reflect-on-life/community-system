package com.example.futurecommunity.controller;

import com.example.futurecommunity.model.DoctorSchedule;
import com.example.futurecommunity.service.DoctorScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
public class DoctorScheduleController {

    @Autowired
    private DoctorScheduleService doctorScheduleService;

    /**
     * 获取医生的排班列表
     * @param doctorId 医生ID
     * @return 排班列表
     */
    @GetMapping("/list")
    public List<DoctorSchedule> getScheduleList(@RequestParam Long doctorId) {
        return doctorScheduleService.getScheduleByDoctorId(doctorId);
    }

    /**
     * 获取医生在指定日期的排班
     * @param doctorId 医生ID
     * @param scheduleDate 排班日期
     * @return 排班列表
     */
    @GetMapping("/list-by-date")
    public List<DoctorSchedule> getScheduleListByDate(@RequestParam Long doctorId, @RequestParam Date scheduleDate) {
        return doctorScheduleService.getScheduleByDoctorIdAndDate(doctorId, scheduleDate);
    }

    /**
     * 创建医生排班
     * @param schedule 排班信息
     * @return 创建后的排班
     */
    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/create")
    public DoctorSchedule createSchedule(@RequestBody DoctorSchedule schedule) {
        return doctorScheduleService.createSchedule(schedule);
    }

    /**
     * 更新医生排班
     * @param schedule 排班信息
     * @return 更新后的排班
     */
    @PreAuthorize("hasRole('DOCTOR')")
    @PutMapping("/update")
    public DoctorSchedule updateSchedule(@RequestBody DoctorSchedule schedule) {
        return doctorScheduleService.updateSchedule(schedule);
    }

    /**
     * 删除医生排班
     * @param id 排班ID
     * @return 是否删除成功
     */
    @PreAuthorize("hasRole('DOCTOR')")
    @DeleteMapping("/delete/{id}")
    public boolean deleteSchedule(@PathVariable Long id) {
        return doctorScheduleService.deleteSchedule(id);
    }
}
