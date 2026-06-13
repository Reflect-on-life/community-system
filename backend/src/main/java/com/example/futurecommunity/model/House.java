package com.example.futurecommunity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("house")
public class House {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("building_id")
    private Long buildingId;
    @TableField("house_no")
    private String houseNo;
    @TableField("owner_id")
    private Long ownerId;
    private Integer status;
}
