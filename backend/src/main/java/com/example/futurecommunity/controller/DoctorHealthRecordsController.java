package com.example.futurecommunity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.futurecommunity.dto.ResidentQueryRequest;
import com.example.futurecommunity.model.House;
import com.example.futurecommunity.model.ResidentHouse;
import com.example.futurecommunity.model.User;
import com.example.futurecommunity.service.HouseService;
import com.example.futurecommunity.service.ResidentHouseService;
import com.example.futurecommunity.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.futurecommunity.dto.HealthCheckRequest;
import com.example.futurecommunity.model.HealthRecord;
import com.example.futurecommunity.service.HealthRecordService;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctor-residents")
public class DoctorHealthRecordsController {
    private static final Logger logger = LoggerFactory.getLogger(DoctorHealthRecordsController.class);
    private static final long HEALTH_CACHE_TTL_MS = 180000L;
    private static final java.util.concurrent.atomic.AtomicLong healthCacheHits = new java.util.concurrent.atomic.AtomicLong(0);
    private static final java.util.concurrent.atomic.AtomicLong healthCacheMisses = new java.util.concurrent.atomic.AtomicLong(0);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ResidentHouseService residentHouseService;
    
    @Autowired
    private HouseService houseService;
    
    @Autowired
    private HealthRecordService healthRecordService;

    // Stats cache for health statistics
    private static final long HEALTH_STATS_CACHE_TTL_MS = 180000L;
    private static final java.util.concurrent.ConcurrentHashMap<Long, CachedHealthStats> healthStatsCache = new java.util.concurrent.ConcurrentHashMap<>();
    private static class CachedHealthStats {
        long timestamp;
        Map<String, Object> data;
        CachedHealthStats(Map<String, Object> data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * 分页查询已认证居民列表
     * 支持按姓名、手机号、楼栋房号搜索
     * 支持按重点人群标签筛选
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> getCertifiedResidents(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String building,
            @RequestParam(required = false) String room,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        // 查询已认证的居民
        List<ResidentHouse> residentHouses = residentHouseService.lambdaQuery()
                .eq(ResidentHouse::getCertified, 1)
                .list();
        
        // 获取已认证居民的ID列表
        Set<Long> certifiedUserIds = residentHouses.stream()
                .map(ResidentHouse::getUserId)
                .collect(Collectors.toSet());
        
        // 查询居民信息
        List<User> residents = userService.lambdaQuery()
                .eq(User::getType, "resident")
                .eq(User::getStatus, 1)
                .in(User::getId, certifiedUserIds)
                .list();
        
        // 搜索过滤
        if (name != null && !name.isEmpty()) {
            residents = residents.stream()
                    .filter(u -> u.getName() != null && u.getName().contains(name))
                    .collect(Collectors.toList());
        }
        if (phone != null && !phone.isEmpty()) {
            residents = residents.stream()
                    .filter(u -> u.getPhone() != null && u.getPhone().contains(phone))
                    .collect(Collectors.toList());
        }
        
        // 标签筛选
        if (tag != null && !tag.isEmpty()) {
            residents = residents.stream()
                    .filter(u -> u.getTags() != null && u.getTags().contains(tag))
                    .collect(Collectors.toList());
        }
        
        // 构建返回数据
        List<Map<String, Object>> result = new ArrayList<>();
        for (User resident : residents) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", resident.getId());
            map.put("name", resident.getName());
            map.put("phone", resident.getPhone());
            map.put("gender", resident.getGender());
            map.put("birthday", resident.getBirthday());
            map.put("tags", resident.getTags());
            
            // 计算年龄
            if (resident.getBirthday() != null) {
                LocalDate birthDate = resident.getBirthday().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                int age = Period.between(birthDate, LocalDate.now()).getYears();
                map.put("age", age);
            }
            
            // 获取最新健康记录的BMI
            List<HealthRecord> healthRecords = healthRecordService.lambdaQuery()
                    .eq(HealthRecord::getUserId, resident.getId())
                    .orderByDesc(HealthRecord::getCreatedAt)
                    .list();
            if (!healthRecords.isEmpty()) {
                HealthRecord latestRecord = healthRecords.get(0);
                map.put("bmi", latestRecord.getBmi());
                map.put("chronicDiseaseTypeId", latestRecord.getChronicDiseaseTypeId());
                map.put("chronicDiseaseOther", latestRecord.getChronicDiseaseOther());
                map.put("height", latestRecord.getHeight());
                map.put("weight", latestRecord.getWeight());
                map.put("bloodPressure", latestRecord.getBloodPressure());
                map.put("bloodSugar", latestRecord.getBloodSugar());
            }
            
            // 获取房屋信息
            List<ResidentHouse> userHouses = residentHouses.stream()
                    .filter(rh -> rh.getUserId().equals(resident.getId()))
                    .collect(Collectors.toList());
            
            if (!userHouses.isEmpty()) {
                List<Map<String, Object>> houseList = new ArrayList<>();
                for (ResidentHouse rh : userHouses) {
                    House house = houseService.getById(rh.getHouseId());
                    if (house != null) {
                        Map<String, Object> houseInfo = new HashMap<>();
                        houseInfo.put("houseId", house.getId());
                        houseInfo.put("buildingId", house.getBuildingId());
                        houseInfo.put("houseNo", house.getHouseNo());
                        houseList.add(houseInfo);
                    }
                }
                map.put("houses", houseList);
            }
            
            // 楼栋房号搜索
            boolean skip = false;
            if (building != null && !building.isEmpty()) {
                List<Map<String, Object>> houses = (List<Map<String, Object>>) map.get("houses");
                if (houses == null || houses.isEmpty()) {
                    skip = true;
                } else {
                    boolean matchBuilding = houses.stream()
                            .anyMatch(h -> h.get("buildingId") != null && 
                                    h.get("buildingId").toString().contains(building));
                    if (!matchBuilding) {
                        skip = true;
                    }
                }
            }
            if (!skip && room != null && !room.isEmpty()) {
                List<Map<String, Object>> houses = (List<Map<String, Object>>) map.get("houses");
                if (houses == null || houses.isEmpty()) {
                    skip = true;
                } else {
                    boolean matchRoom = houses.stream()
                            .anyMatch(h -> h.get("houseNo") != null && 
                                    h.get("houseNo").toString().contains(room));
                    if (!matchRoom) {
                        skip = true;
                    }
                }
            }
            
            if (!skip) {
                result.add(map);
            }
        }
        
        // 分页处理
        int total = result.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<Map<String, Object>> pageData = result.subList(fromIndex, toIndex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", pageData);
        response.put("total", total);
        response.put("page", page);
        response.put("pageSize", pageSize);
        response.put("totalPages", (int) Math.ceil((double) total / pageSize));
        
        logger.info("查询居民列表 - 页码: {}, 每页: {}, 总数: {}", page, pageSize, total);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health-stats")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> getHealthStats() {
        Long doctorId = null;
        try {
            String idStr = SecurityContextHolder.getContext().getAuthentication().getName();
            if (idStr != null) doctorId = Long.valueOf(idStr);
        } catch (Exception e) {
            doctorId = null;
        }
        Map<String, Object> response;
        long now = System.currentTimeMillis();
        CachedHealthStats cached = (doctorId != null) ? healthStatsCache.get(doctorId) : null;
        if (cached != null && doctorId != null && (now - cached.timestamp) <= HEALTH_STATS_CACHE_TTL_MS) {
            response = cached.data;
        } else {
            List<HealthRecord> records = healthRecordService.lambdaQuery()
                    .eq(HealthRecord::getDoctorId, doctorId)
                    .list();
            int under = 0, normal = 0, overweight = 0, obese = 0;
            for (HealthRecord r : records) {
                Double bmi = r.getBmi();
                if (bmi == null) continue;
                if (bmi < 18.5) under++;
                else if (bmi < 25) normal++;
                else if (bmi < 30) overweight++;
                else obese++;
            }
            Map<String, Integer> dist = new HashMap<>();
            dist.put("underweight", under);
            dist.put("normal", normal);
            dist.put("overweight", overweight);
            dist.put("obese", obese);
            Map<String, Object> data = new HashMap<>();
            data.put("bmiDistribution", dist);
            data.put("latestBMI", records.isEmpty() ? null : records.get(0).getBmi());
            data.put("count", records.size());
            response = new HashMap<>();
            response.put("success", true);
            response.put("data", data);
            healthStatsCache.put(doctorId, new CachedHealthStats(response, now));
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新居民重点人群标签
     */
    @PutMapping("/tags/{userId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> updateResidentTags(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> request) {
        
        User user = userService.getById(userId);
        if (user == null) {
            logger.warn("更新居民标签失败 - 用户不存在, ID: {}", userId);
            return ResponseEntity.notFound().build();
        }
        
        String tags = (String) request.get("tags");
        user.setTags(tags);
        userService.updateById(user);
        
        logger.info("更新居民标签成功 - 用户ID: {}, 标签: {}", userId, tags);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "标签更新成功");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 录入体检数据
     */
    @PostMapping("/health-check")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> createHealthCheck(
            @RequestBody HealthCheckRequest request) {
        
        try {
            // 获取当前医生ID
            String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
            Long doctorId = Long.parseLong(currentUserId);
            
            // 安全校验：当前医生只能为自己负责的居民录入体检数据
            if (request.getUserId() != null && !doctorId.equals(request.getUserId())) {
                logger.warn("医生 {} attempt to record health data for non-owned user {}", doctorId, request.getUserId());
                Map<String, Object> resp = new HashMap<>();
                resp.put("success", false);
                resp.put("message", "医生只能为自己负责的居民录入体检数据");
                return ResponseEntity.status(403).body(resp);
            }

            // 创建健康记录
            HealthRecord healthRecord = new HealthRecord();
            healthRecord.setUserId(request.getUserId());
            healthRecord.setDoctorId(doctorId);
            healthRecord.setHeight(request.getHeight());
            healthRecord.setWeight(request.getWeight());
            
            // 计算BMI
            if (request.getHeight() != null && request.getWeight() != null && request.getHeight() > 0) {
                double heightInMeters = request.getHeight() / 100;
                double bmi = request.getWeight() / (heightInMeters * heightInMeters);
                healthRecord.setBmi(Math.round(bmi * 100.0) / 100.0);
            }
            
            healthRecord.setBloodPressure(request.getBloodPressure());
            healthRecord.setBloodSugar(request.getBloodSugar());
            healthRecord.setChronicDiseaseTypeId(request.getChronicDiseaseTypeId());
            healthRecord.setChronicDiseaseOther(request.getChronicDiseaseOther());
            healthRecord.setDoctorSuggestion(request.getDoctorSuggestion());
            healthRecord.setNotes(request.getNotes());
            
            healthRecordService.save(healthRecord);
            // Invalidate cache for the user since a new health record has been added
            healthRecordsCache.remove(request.getUserId());
            // 可选：若缓存中存在该用户的历史记录，立刻将新记录推入缓存，方便后续查询
            CachedHealthRecords cachedForUser = healthRecordsCache.get(request.getUserId());
            if (cachedForUser != null) {
                List<HealthRecord> cachedList = new ArrayList<>(cachedForUser.data);
                cachedList.add(0, healthRecord);
                healthRecordsCache.put(request.getUserId(), new CachedHealthRecords(cachedList, System.currentTimeMillis()));
            }
            
            logger.info("录入体检数据成功 - 用户ID: {}, 记录ID: {}", request.getUserId(), healthRecord.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "体检数据录入成功");
            response.put("data", healthRecord);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("录入体检数据失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "体检数据录入失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取居民历史体检数据
     */
    @GetMapping("/health-records/{userId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> getHealthRecords(
            @PathVariable Long userId) {
        // 权限 guard：仅允许医生查看其负责居民的健康记录。若该居民无房产绑定，则视为无访问权限。
        List<ResidentHouse> residentHousesForUser = residentHouseService.lambdaQuery()
                .eq(ResidentHouse::getUserId, userId)
                .list();
        if (residentHousesForUser == null || residentHousesForUser.isEmpty()) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("message", "无访问权限或居民不存在");
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body(resp);
        }
        // 简单的内存缓存实现，1分钟过期，避免重复查询
        List<HealthRecord> records;
        long now = System.currentTimeMillis();
        CachedHealthRecords cached = healthRecordsCache.get(userId);
        boolean fromCache = false;
        if (cached != null && (now - cached.timestamp) <= HEALTH_CACHE_TTL_MS) {
            records = cached.data;
            fromCache = true;
        } else {
            records = healthRecordService.lambdaQuery()
                    .eq(HealthRecord::getUserId, userId)
                    .orderByDesc(HealthRecord::getCreatedAt)
                    .list();
            healthRecordsCache.put(userId, new CachedHealthRecords(records, now));
        }
        if (fromCache) {
            healthCacheHits.incrementAndGet();
            logger.info("Health records cache HIT for user {}", userId);
        } else {
            healthCacheMisses.incrementAndGet();
            logger.info("Health records cache MISS for user {}", userId);
        }
        
        logger.info("获取居民历史体检数据 - 用户ID: {}, 记录数: {}", userId, records.size());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", records);
        return ResponseEntity.ok(response);
    }
    
    // 简易缓存结构，防止重复查询同一居民最近1分钟的体检记录
    private static class CachedHealthRecords {
        List<HealthRecord> data;
        long timestamp;
        CachedHealthRecords(List<HealthRecord> data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
    }

    // 全局简单缓存容器（static，进程范围内）
    private static final ConcurrentHashMap<Long, CachedHealthRecords> healthRecordsCache = new ConcurrentHashMap<>();
}
