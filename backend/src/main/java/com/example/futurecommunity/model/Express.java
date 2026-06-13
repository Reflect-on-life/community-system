package com.example.futurecommunity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

@TableName("express")
public class Express {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("tracking_no")
    private String trackingNo;
    @TableField("user_id")
    private Long userId;
    @TableField("pickup_code")
    private String pickupCode;
    private String status;
    @TableField("received_at")
    private Date receivedAt;
    @TableField("picked_at")
    private Date pickedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPickupCode() {
        return pickupCode;
    }

    public void setPickupCode(String pickupCode) {
        this.pickupCode = pickupCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Date receivedAt) {
        this.receivedAt = receivedAt;
    }

    public Date getPickedAt() {
        return pickedAt;
    }

    public void setPickedAt(Date pickedAt) {
        this.pickedAt = pickedAt;
    }
}
