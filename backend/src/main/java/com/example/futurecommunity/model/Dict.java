package com.example.futurecommunity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dict")
public class Dict {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String type;
    private String code;
    private String value;
    private String description;
}
