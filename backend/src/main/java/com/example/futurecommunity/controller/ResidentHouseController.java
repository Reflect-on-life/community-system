package com.example.futurecommunity.controller;

import com.example.futurecommunity.model.ResidentHouse;
import com.example.futurecommunity.model.Building;
import com.example.futurecommunity.model.House;
import com.example.futurecommunity.model.User;
import com.example.futurecommunity.model.CertificationApplicationDTO;
import com.example.futurecommunity.service.ResidentHouseService;
import com.example.futurecommunity.service.BuildingService;
import com.example.futurecommunity.service.HouseService;
import com.example.futurecommunity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/resident-house")
public class ResidentHouseController {

    @Autowired
    private ResidentHouseService residentHouseService;

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private HouseService houseService;

    @Autowired
    private UserService userService;

    /**
     * 提交房产认证申请
     * 居民用户可以选择楼栋和房号，提交房产认证申请
     */
    @PreAuthorize("hasRole('RESIDENT')")
    @PostMapping("/apply")
    public ResidentHouse applyCertification(@RequestBody ResidentHouse residentHouse) {
        // 从 SecurityContextHolder 中获取当前登录用户的 userId
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录用户ID: " + currentUserId);
        
        // 设置 userId 为当前登录用户的 ID，防止用户伪造其他用户的 ID
        residentHouse.setUserId(Long.parseLong(currentUserId));
        
        // 设置认证状态为未认证
        residentHouse.setCertified(0);
        
        // 保存认证申请
        residentHouseService.save(residentHouse);
        System.out.println("保存认证申请成功，申请信息: " + residentHouse);
        return residentHouse;
    }

    /**
     * 物业审核认证申请
     * 物业用户可以审核居民的房产认证申请
     */
    @PreAuthorize("hasAnyRole('PROPERTY', 'ADMIN')")
    @PutMapping("/approve")
    public ResidentHouse approveCertification(@RequestParam Long userId, @RequestParam Long houseId) {
        System.out.println("审核通过请求，userId: " + userId + "，houseId: " + houseId);
        // 使用条件更新认证状态
        boolean updateResult = residentHouseService.lambdaUpdate()
                .eq(ResidentHouse::getUserId, userId)
                .eq(ResidentHouse::getHouseId, houseId)
                .set(ResidentHouse::getCertified, 1)
                .update();
        System.out.println("更新结果：" + updateResult);
        if (!updateResult) {
            System.out.println("认证申请不存在或更新失败，userId: " + userId + "，houseId: " + houseId);
            throw new RuntimeException("认证申请不存在或更新失败");
        }
        
        // ✅ 同步更新 house 表的 owner_id 字段
        House house = houseService.getById(houseId);
        if (house != null) {
            house.setOwnerId(userId);
            houseService.updateById(house);
            System.out.println("已同步更新 house 表 owner_id，houseId: " + houseId + ", new ownerId: " + userId);
        }
        
        // 查询更新后的认证申请
        ResidentHouse residentHouse = residentHouseService.lambdaQuery()
                .eq(ResidentHouse::getUserId, userId)
                .eq(ResidentHouse::getHouseId, houseId)
                .one();
        System.out.println("更新后的认证申请：" + residentHouse);
        return residentHouse;
    }

    /**
     * 物业拒绝认证申请
     * 物业用户可以拒绝居民的房产认证申请
     */
    @PreAuthorize("hasAnyRole('PROPERTY', 'ADMIN')")
    @DeleteMapping("/reject")
    public void rejectCertification(@RequestParam Long userId, @RequestParam Long houseId) {
        // 查询认证申请
        ResidentHouse residentHouse = residentHouseService.lambdaQuery()
                .eq(ResidentHouse::getUserId, userId)
                .eq(ResidentHouse::getHouseId, houseId)
                .one();
        if (residentHouse == null) {
            throw new RuntimeException("认证申请不存在");
        }
        // 删除认证申请
        residentHouseService.lambdaUpdate()
                .eq(ResidentHouse::getUserId, userId)
                .eq(ResidentHouse::getHouseId, houseId)
                .remove();
    }

    /**
     * 获取已审核的认证记录
     * 物业用户可以查看所有已审核的认证记录
     */
    @PreAuthorize("hasAnyRole('PROPERTY', 'ADMIN')")
    @GetMapping("/processed")
    public List<CertificationApplicationDTO> getProcessedApplications() {
        System.out.println("获取已审核的认证记录");
        // 查询所有已认证的记录
        List<ResidentHouse> residentHouses = residentHouseService.lambdaQuery()
                .eq(ResidentHouse::getCertified, 1)
                .list();

        // 构建DTO列表
        List<CertificationApplicationDTO> applications = new ArrayList<>();
        for (ResidentHouse residentHouse : residentHouses) {
            CertificationApplicationDTO dto = new CertificationApplicationDTO();
            // 使用复合主键：userId 和 houseId
            dto.setUserId(residentHouse.getUserId());
            dto.setHouseId(residentHouse.getHouseId());
            dto.setRelation(residentHouse.getRelation());
            dto.setCertified(residentHouse.getCertified());
            dto.setContractImageUrl(residentHouse.getContractImageUrl());

            // 查询用户信息
            User user = userService.getById(residentHouse.getUserId());
            if (user != null) {
                dto.setUserName(user.getName());
                dto.setPhone(user.getPhone());
            }

            // 查询房屋信息
            House house = houseService.getById(residentHouse.getHouseId());
            if (house != null) {
                dto.setHouseNo(house.getHouseNo());
                // 查询楼栋信息
                if (house.getBuildingId() != null) {
                    Building building = buildingService.getById(house.getBuildingId());
                    if (building != null) {
                        dto.setBuildingName(building.getName());
                    }
                }
            }

            applications.add(dto);
        }

        System.out.println("已审核认证记录数量: " + applications.size());
        return applications;
    }

    /**
     * 撤回认证
     * 物业用户可以撤回已通过的认证
     */
    @PreAuthorize("hasAnyRole('PROPERTY', 'ADMIN')")
    @PutMapping("/revoke")
    public ResidentHouse revokeCertification(@RequestParam Long userId, @RequestParam Long houseId) {
        System.out.println("撤回认证请求，userId: " + userId + "，houseId: " + houseId);
        // 使用条件更新认证状态
        boolean updateResult = residentHouseService.lambdaUpdate()
                .eq(ResidentHouse::getUserId, userId)
                .eq(ResidentHouse::getHouseId, houseId)
                .set(ResidentHouse::getCertified, 0)
                .update();
        System.out.println("更新结果: " + updateResult);
        if (!updateResult) {
            System.out.println("认证记录不存在或更新失败，userId: " + userId + "，houseId: " + houseId);
            throw new RuntimeException("认证记录不存在或更新失败");
        }
        // 查询更新后的认证记录
        ResidentHouse residentHouse = residentHouseService.lambdaQuery()
                .eq(ResidentHouse::getUserId, userId)
                .eq(ResidentHouse::getHouseId, houseId)
                .one();
        System.out.println("更新后的认证记录: " + residentHouse);
        return residentHouse;
    }

    /**
     * 获取所有待审核的认证申请
     * 物业用户使用
     */
    @PreAuthorize("hasAnyRole('PROPERTY', 'ADMIN')")
    @GetMapping("/pending")
    public List<CertificationApplicationDTO> getPendingApplications() {
        // 查询所有待审核的认证申请
        List<ResidentHouse> residentHouses = residentHouseService.lambdaQuery()
                .eq(ResidentHouse::getCertified, 0)
                .list();

        // 构建DTO列表
        List<CertificationApplicationDTO> applications = new ArrayList<>();
        for (ResidentHouse residentHouse : residentHouses) {
            CertificationApplicationDTO dto = new CertificationApplicationDTO();
            // 使用复合主键：userId 和 houseId
            dto.setUserId(residentHouse.getUserId());
            dto.setHouseId(residentHouse.getHouseId());
            dto.setRelation(residentHouse.getRelation());
            dto.setCertified(residentHouse.getCertified());
            dto.setContractImageUrl(residentHouse.getContractImageUrl());

            // 查询用户信息
            User user = userService.getById(residentHouse.getUserId());
            if (user != null) {
                dto.setUserName(user.getName());
                dto.setPhone(user.getPhone());
            }

            // 查询房屋信息
            House house = houseService.getById(residentHouse.getHouseId());
            if (house != null) {
                dto.setHouseNo(house.getHouseNo());
                dto.setBuildingId(house.getBuildingId());

                // 查询楼栋信息
                if (house.getBuildingId() != null) {
                    Building building = buildingService.getById(house.getBuildingId());
                    if (building != null) {
                        dto.setBuildingName(building.getName());
                    }
                }
            }

            applications.add(dto);
        }

        return applications;
    }

    /**
     * 获取所有楼栋
     * 用于前端选择楼栋
     */
    @PreAuthorize("hasAnyRole('RESIDENT', 'PROPERTY', 'ADMIN')")
    @GetMapping("/buildings")
    public List<Building> getBuildings() {
        System.out.println("获取所有楼栋");
        List<Building> buildings = buildingService.list();
        System.out.println("楼栋列表: " + buildings);
        return buildings;
    }

    /**
     * 根据楼栋 ID 获取房屋列表
     * 用于前端选择房号
     */
    @PreAuthorize("hasAnyRole('RESIDENT', 'PROPERTY', 'ADMIN')")
    @GetMapping("/houses/{buildingId}")
    public List<House> getHousesByBuildingId(@PathVariable Long buildingId) {
        System.out.println("根据楼栋 ID 获取房屋列表，buildingId: " + buildingId);
        List<House> houses = houseService.lambdaQuery()
                .eq(House::getBuildingId, buildingId)
                .list();
        System.out.println("房屋列表: " + houses);
        return houses;
    }

    /**
     * 获取所有房屋列表
     * 用于前端初始化时加载所有房屋数据
     */
    @PreAuthorize("hasAnyRole('RESIDENT', 'PROPERTY', 'ADMIN')")
    @GetMapping("/houses/all")
    public List<House> getAllHouses() {
        System.out.println("获取所有房屋列表");
        List<House> houses = houseService.list();
        System.out.println("房屋列表: " + houses);
        return houses;
    }

    /**
     * 获取用户的房产列表
     * 用于前端展示用户的房产
     */
    @PreAuthorize("#userId.toString() == authentication.principal.username or hasAnyRole('PROPERTY', 'ADMIN')")
    @GetMapping("/user/{userId}")
    public List<ResidentHouse> getUserHouses(@PathVariable Long userId) {
        System.out.println("获取用户的房产列表，userId: " + userId);
        List<ResidentHouse> residentHouses = residentHouseService.lambdaQuery()
                .eq(ResidentHouse::getUserId, userId)
                .list();
        System.out.println("用户的房产列表: " + residentHouses);
        return residentHouses;
    }

    /**
     * 检查用户是否已认证房产
     * 用于前端判断用户是否可以报名活动
     */
    @PreAuthorize("hasAnyRole('RESIDENT', 'PROPERTY', 'ADMIN')")
    @GetMapping("/certified")
    public boolean checkUserCertified(@RequestParam Long userId) {
        System.out.println("检查用户是否已认证房产，userId: " + userId);
        List<ResidentHouse> residentHouses = residentHouseService.lambdaQuery()
                .eq(ResidentHouse::getUserId, userId)
                .eq(ResidentHouse::getCertified, 1)
                .list();
        boolean isCertified = !residentHouses.isEmpty();
        System.out.println("用户认证状态: " + isCertified);
        return isCertified;
    }

    /**
     * 获取全社区的房产列表
     * 物业用户使用，展示所有房产信息
     */
    @PreAuthorize("hasAnyRole('PROPERTY', 'ADMIN')")
    @GetMapping("/all")
    public List<Object> getAllCommunityHouses() {
        System.out.println("获取全社区的房产列表");
        
        List<Object> result = new ArrayList<>();
        
        // 获取所有房屋
        List<House> houses = houseService.list();
        
        for (House house : houses) {
            // 构建房产信息对象
            java.util.Map<String, Object> houseInfo = new java.util.HashMap<>();
            houseInfo.put("id", house.getId());
            houseInfo.put("buildingId", house.getBuildingId());
            houseInfo.put("houseNo", house.getHouseNo());
            houseInfo.put("status", house.getStatus());
            
            // 只有房屋状态为可用（已入住）时才显示业主信息
            if (house.getStatus() != null && house.getStatus() == 1) {
                // 查询业主信息
                if (house.getOwnerId() != null) {
                    User owner = userService.getById(house.getOwnerId());
                    if (owner != null) {
                        houseInfo.put("ownerName", owner.getName());
                        houseInfo.put("ownerPhone", owner.getPhone());
                    }
                } else {
                    // 如果house表中没有owner_id，从resident_house表中查询已认证的居民
                    List<ResidentHouse> residentHouses = residentHouseService.lambdaQuery()
                            .eq(ResidentHouse::getHouseId, house.getId())
                            .eq(ResidentHouse::getCertified, 1)
                            .list();
                    if (!residentHouses.isEmpty()) {
                        // 取第一个已认证的居民作为业主信息
                        ResidentHouse residentHouse = residentHouses.get(0);
                        if (residentHouse.getUserId() != null) {
                            User resident = userService.getById(residentHouse.getUserId());
                            if (resident != null) {
                                houseInfo.put("ownerName", resident.getName());
                                houseInfo.put("ownerPhone", resident.getPhone());
                            }
                        }
                    }
                }
            }
            
            // 查询认证状态 - 只有房屋状态为可用（已入住）时才查询认证状态
            if (house.getStatus() != null && house.getStatus() == 1) {
                List<ResidentHouse> residentHouses = residentHouseService.lambdaQuery()
                        .eq(ResidentHouse::getHouseId, house.getId())
                        .eq(ResidentHouse::getCertified, 1)
                        .list();
                houseInfo.put("certified", residentHouses.isEmpty() ? 0 : 1);
            } else {
                // 空置房屋显示为未认证
                houseInfo.put("certified", 0);
            }
            
            result.add(houseInfo);
        }
        
        System.out.println("全社区房产列表数量: " + result.size());
        return result;
    }

    /**
     * 新增房产
     * 物业用户使用，手动录入新房产
     */
    @PreAuthorize("hasAnyRole('PROPERTY', 'ADMIN')")
    @PostMapping
    public House addHouse(@RequestBody House house) {
        System.out.println("新增房产，请求数据: " + house);
        houseService.save(house);
        System.out.println("新增房产成功，房产ID: " + house.getId());
        return house;
    }

    /**
     * 更新房产信息
     * 物业用户使用，编辑房产信息
     */
    @PreAuthorize("hasAnyRole('PROPERTY', 'ADMIN')")
    @PutMapping("/{id}")
    public House updateHouse(@PathVariable Long id, @RequestBody House house) {
        System.out.println("更新房产信息，房产ID: " + id + "，请求数据: " + house);
        house.setId(id);
        houseService.updateById(house);
        System.out.println("更新房产信息成功");
        return house;
    }

    /**
     * 删除房产
     * 物业用户使用，删除房产
     */
    @PreAuthorize("hasAnyRole('PROPERTY', 'ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteHouse(@PathVariable Long id) {
        System.out.println("删除房产，房产ID: " + id);
        houseService.removeById(id);
        System.out.println("删除房产成功");
    }

}
