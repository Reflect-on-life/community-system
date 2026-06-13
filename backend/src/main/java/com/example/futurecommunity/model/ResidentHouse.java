package com.example.futurecommunity.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("resident_house")
public class ResidentHouse {
    @TableField("user_id")
    private Long userId;
    @TableField("house_id")
    private Long houseId;
    private String relation;
    private Integer certified;
    
    /**
     * 房产证号，用于房产认证（VARCHAR(100)）
     */
    @TableField("cert_no")
    private String certNo;
    
    /**
     * 房产详细地址，用于房产认证（VARCHAR(200)）
     */
    @TableField("address")
    private String address;
    
    /**
     * 合同图片路径（VARCHAR(500)）
     */
    @TableField("contract_image_url")
    private String contractImageUrl;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getHouseId() {
        return houseId;
    }

    public void setHouseId(Long houseId) {
        this.houseId = houseId;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public Integer getCertified() {
        return certified;
    }

    public void setCertified(Integer certified) {
        this.certified = certified;
    }

    public String getCertNo() {
        return certNo;
    }

    public void setCertNo(String certNo) {
        this.certNo = certNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getContractImageUrl() {
        return contractImageUrl;
    }
    
    public void setContractImageUrl(String contractImageUrl) {
        this.contractImageUrl = contractImageUrl;
    }
}
