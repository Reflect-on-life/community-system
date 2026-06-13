package com.example.futurecommunity.controller;

import com.example.futurecommunity.model.Appointment;
import com.example.futurecommunity.model.DoctorSchedule;
import com.example.futurecommunity.model.HealthRecord;
import com.example.futurecommunity.model.User;
import com.example.futurecommunity.service.AppointmentService;
import com.example.futurecommunity.service.DoctorScheduleService;
import com.example.futurecommunity.service.HealthRecordService;
import com.example.futurecommunity.service.UserService;
import com.example.futurecommunity.dto.CompleteAppointmentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.Date;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {
    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private DoctorScheduleService doctorScheduleService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private HealthRecordService healthRecordService;
    
    // 获取预约列表（带权限控制和分页）
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('RESIDENT', 'DOCTOR', 'ADMIN', 'PROPERTY', 'TECHNICIAN', 'PROPERTY_TECHNICIAN')")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        // 获取当前登录用户的信息
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        String userRole = SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority();
        
        // 构建查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Appointment> pagination = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Appointment> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        
        // 根据用户角色构建查询条件
        Long currentUserIdLong = Long.parseLong(currentUserId);
        if ("ROLE_RESIDENT".equals(userRole) || "ROLE_PROPERTY_TECHNICIAN".equals(userRole)) {
            // 居民和维修师傅只能查看自己的预约记录
            queryWrapper.eq(Appointment::getUserId, currentUserIdLong);
        } else if ("ROLE_DOCTOR".equals(userRole)) {
            // 医生查看其相关的预约记录
            queryWrapper.eq(Appointment::getDoctorId, currentUserIdLong);
        } else if ("ROLE_PROPERTY".equals(userRole)) {
            // 物业用户：如果是维修师傅（用户 ID 在 17-21 之间），只能查看自己的预约记录
            // 否则可以查看所有预约记录
            if (currentUserIdLong >= 17 && currentUserIdLong <= 21) {
                queryWrapper.eq(Appointment::getUserId, currentUserIdLong);
            }
        }
        // 管理员用户可以查看所有预约记录
        
        // 执行分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Appointment> resultPage = appointmentService.page(pagination, queryWrapper);
        List<Appointment> appointments = resultPage.getRecords();
        
        // 转换为包含userName的Map列表
        List<Map<String, Object>> records = new ArrayList<>();
        for (Appointment appointment : appointments) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", appointment.getId());
            map.put("userId", appointment.getUserId());
            map.put("doctorId", appointment.getDoctorId());
            map.put("type", appointment.getType());
            map.put("appointmentType", appointment.getAppointmentType());
            map.put("date", appointment.getDate());
            map.put("status", appointment.getStatus());
            map.put("createdAt", appointment.getCreatedAt());
            
            // 添加居民姓名
            User user = userService.getById(appointment.getUserId());
            if (user != null) {
                map.put("userName", user.getName());
            }
            
            records.add(map);
        }
        
        // 构建分页响应
        Map<String, Object> response = new HashMap<>();
        response.put("records", records);
        response.put("total", resultPage.getTotal());
        response.put("current", resultPage.getCurrent());
        response.put("size", resultPage.getSize());
        response.put("pages", resultPage.getPages());
        
        return ResponseEntity.ok(response);
    }
    
    // 查询疫苗预约记录
    @GetMapping("/vaccine/list")
    @PreAuthorize("hasAnyRole('RESIDENT', 'DOCTOR', 'ADMIN', 'PROPERTY', 'TECHNICIAN', 'PROPERTY_TECHNICIAN')")
    public ResponseEntity<Map<String, Object>> getVaccineAppointments(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        // 获取当前登录用户的信息
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        String userRole = SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority();
        
        // 构建查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Appointment> pagination = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Appointment> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        
        // 只查询疫苗预约
        queryWrapper.eq(Appointment::getAppointmentType, "VACCINE");
        
        // 根据用户角色构建查询条件
        Long currentUserIdLong = Long.parseLong(currentUserId);
        if ("ROLE_RESIDENT".equals(userRole) || "ROLE_PROPERTY_TECHNICIAN".equals(userRole)) {
            // 居民和维修师傅只能查看自己的预约记录
            queryWrapper.eq(Appointment::getUserId, currentUserIdLong);
        } else if ("ROLE_DOCTOR".equals(userRole)) {
            // 医生查看其相关的预约记录
            queryWrapper.eq(Appointment::getDoctorId, currentUserIdLong);
        } else if ("ROLE_PROPERTY".equals(userRole)) {
            // 物业用户：如果是维修师傅（用户 ID 在 17-21 之间），只能查看自己的预约记录
            // 否则可以查看所有预约记录
            if (currentUserIdLong >= 17 && currentUserIdLong <= 21) {
                queryWrapper.eq(Appointment::getUserId, currentUserIdLong);
            }
        }
        // 管理员用户可以查看所有预约记录
        
        // 执行分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Appointment> resultPage = appointmentService.page(pagination, queryWrapper);
        List<Appointment> appointments = resultPage.getRecords();
        
        // 转换为包含userName的Map列表
        List<Map<String, Object>> records = new ArrayList<>();
        for (Appointment appointment : appointments) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", appointment.getId());
            map.put("userId", appointment.getUserId());
            map.put("doctorId", appointment.getDoctorId());
            map.put("type", appointment.getType());
            map.put("appointmentType", appointment.getAppointmentType());
            map.put("date", appointment.getDate());
            map.put("status", appointment.getStatus());
            map.put("createdAt", appointment.getCreatedAt());
            
            // 添加居民姓名
            User user = userService.getById(appointment.getUserId());
            if (user != null) {
                map.put("userName", user.getName());
            }
            
            records.add(map);
        }
        
        // 构建分页响应
        Map<String, Object> response = new HashMap<>();
        response.put("records", records);
        response.put("total", resultPage.getTotal());
        response.put("current", resultPage.getCurrent());
        response.put("size", resultPage.getSize());
        response.put("pages", resultPage.getPages());
        
        return ResponseEntity.ok(response);
    }
    
    // 查询指定医生的可用日程安排（支持分页）
    @GetMapping("/schedule/{doctorId}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailableSchedule(
            @PathVariable Long doctorId,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        // 获取当前日期，设置为当天的0点0分0秒
        Date today = new Date();
        today.setHours(0);
        today.setMinutes(0);
        today.setSeconds(0);
        
        // 构建分页对象
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<DoctorSchedule> pagination = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        
        // 构建查询条件
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DoctorSchedule> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(DoctorSchedule::getDoctorId, doctorId)
                .eq(DoctorSchedule::getStatus, "available")
                .ge(DoctorSchedule::getScheduleDate, today) // 只返回今天及以后的日程
                .orderByAsc(DoctorSchedule::getScheduleDate)
                .orderByAsc(DoctorSchedule::getStartTime);
        
        // 如果指定了日期，添加日期筛选条件
        if (date != null && !date.isEmpty()) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                Date selectedDate = sdf.parse(date);
                Date nextDay = new Date(selectedDate.getTime() + 24 * 60 * 60 * 1000);
                queryWrapper.ge(DoctorSchedule::getScheduleDate, selectedDate)
                        .lt(DoctorSchedule::getScheduleDate, nextDay);
            } catch (Exception e) {
                // 日期格式错误，忽略日期筛选
            }
        }
        
        // 执行分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<DoctorSchedule> resultPage = doctorScheduleService.page(pagination, queryWrapper);
        List<DoctorSchedule> schedules = resultPage.getRecords();
        
        // 构建分页响应
        Map<String, Object> response = new HashMap<>();
        response.put("records", schedules);
        response.put("total", resultPage.getTotal());
        response.put("current", resultPage.getCurrent());
        response.put("size", resultPage.getSize());
        response.put("pages", resultPage.getPages());
        
        return ResponseEntity.ok(response);
    }
    
    // 根据医生ID获取预约列表
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByDoctorId(@PathVariable Long doctorId) {
        List<Appointment> appointments = appointmentService.lambdaQuery()
                .eq(Appointment::getDoctorId, doctorId)
                .list();
        return ResponseEntity.ok(appointments);
    }
    
    // 根据用户ID获取预约列表
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByUserId(@PathVariable Long userId) {
        List<Appointment> appointments = appointmentService.lambdaQuery()
                .eq(Appointment::getUserId, userId)
                .list();
        return ResponseEntity.ok(appointments);
    }
    
    // 创建预约
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('RESIDENT', 'ADMIN', 'PROPERTY')")
    public ResponseEntity<Appointment> createAp(@RequestBody Appointment appointment) {
        // 获取当前登录用户的信息
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        appointment.setUserId(Long.parseLong(currentUserId));
        appointment.setStatus("pending");
        
        // 如果没有指定预约类型，默认为健康检查
        if (appointment.getAppointmentType() == null || appointment.getAppointmentType().isEmpty()) {
            appointment.setAppointmentType("HEALTH_CHECK");
        }
        
        // 保存预约
        appointmentService.save(appointment);
        
        // 后续操作：更新对应医生日程安排的状态为"不可用"
        // 这里简化处理，实际应用中可能需要更复杂的逻辑来匹配具体的时间段
        List<DoctorSchedule> schedules = doctorScheduleService.lambdaQuery()
                .eq(DoctorSchedule::getDoctorId, appointment.getDoctorId())
                .eq(DoctorSchedule::getStatus, "available")
                .list();
        
        // 更新找到的第一个可用日程为不可用
        if (!schedules.isEmpty()) {
            DoctorSchedule schedule = schedules.get(0);
            schedule.setStatus("unavailable");
            doctorScheduleService.updateById(schedule);
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
    }
    
    // 医生接单（更新预约状态为 confirmed）
    @PutMapping("/confirm/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Appointment> confirmAppointment(@PathVariable Long id) {
        Appointment appointment = appointmentService.getById(id);
        if (appointment != null) {
            appointment.setStatus("confirmed");
            appointmentService.updateById(appointment);
            logger.info("医生接单成功 - 预约ID: {}, 医生ID: {}, 居民ID: {}, 预约时间: {}", 
                id, appointment.getDoctorId(), appointment.getUserId(), appointment.getDate());
            return ResponseEntity.ok(appointment);
        }
        logger.warn("接单失败 - 预约不存在, ID: {}", id);
        return ResponseEntity.notFound().build();
    }
    
    // 取消预约（医生或居民可以取消自己的预约）
    @PutMapping("/cancel/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RESIDENT')")
    public ResponseEntity<Appointment> cancelAppointment(@PathVariable Long id) {
        Appointment appointment = appointmentService.getById(id);
        if (appointment != null) {
            appointment.setStatus("cancelled");
            appointmentService.updateById(appointment);
            return ResponseEntity.ok(appointment);
        }
        return ResponseEntity.notFound().build();
    }
    
    // 完成预约
    @PutMapping("/complete/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Appointment> completeAppointment(
            @PathVariable Long id,
            @RequestBody CompleteAppointmentRequest request) {
        Appointment appointment = appointmentService.getById(id);
        if (appointment != null) {
            appointment.setStatus("completed");
            appointmentService.updateById(appointment);
            
            // 自动创建健康记录
            HealthRecord healthRecord = new HealthRecord();
            healthRecord.setUserId(appointment.getUserId());
            healthRecord.setDoctorId(appointment.getDoctorId());
            healthRecord.setChronicDiseaseTypeId(request.getChronicDiseaseTypeId());
            healthRecord.setChronicDiseaseOther(request.getChronicDiseaseOther());
            healthRecord.setNotes(request.getNotes() != null ? request.getNotes() : request.getDiagnosisResult());
            healthRecord.setCreatedAt(new Date());
            healthRecordService.save(healthRecord);
            
            logger.info("诊疗完成 - 预约ID: {}, 医生ID: {}, 居民ID: {}, 诊疗结果: {}, 健康记录ID: {}", 
                id, appointment.getDoctorId(), appointment.getUserId(), request.getDiagnosisResult(), healthRecord.getId());
            
            return ResponseEntity.ok(appointment);
        }
        logger.warn("诊疗完成失败 - 预约不存在, ID: {}", id);
        return ResponseEntity.notFound().build();
    }
    
    // 获取医生列表
    @GetMapping("/doctors")
    public ResponseEntity<List<Map<String, Object>>> getDoctors() {
        List<User> doctors = userService.lambdaQuery()
                .eq(User::getType, "doctor")
                .eq(User::getStatus, 1)
                .list();
        
        // 转换为包含department字段的Map列表
        List<Map<String, Object>> doctorList = doctors.stream().map(doctor -> {
            Map<String, Object> doctorMap = new HashMap<>();
            doctorMap.put("id", doctor.getId());
            doctorMap.put("name", doctor.getName());
            // 为医生添加默认科室，实际应用中可能需要从其他表获取
            doctorMap.put("department", "内科"); // 暂时默认内科，实际应根据医生信息设置
            return doctorMap;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(doctorList);
    }
}
