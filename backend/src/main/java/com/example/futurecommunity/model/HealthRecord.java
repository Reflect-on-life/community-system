package com.example.futurecommunity.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

@TableName("health_record")
public class HealthRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    @TableField("doctor_id")
    private Long doctorId;
    @TableField("chronic_disease_type_id")
    private Long chronicDiseaseTypeId;
    @TableField("chronic_disease_other")
    private String chronicDiseaseOther;
    private Double height;
    private Double weight;
    private Double bmi;
    @TableField("blood_pressure")
    private String bloodPressure;
    @TableField("blood_sugar")
    private Double bloodSugar;
    @TableField("doctor_suggestion")
    private String doctorSuggestion;
    private String notes;
    @TableField("created_at")
    private Date createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Long getChronicDiseaseTypeId() {
        return chronicDiseaseTypeId;
    }

    public void setChronicDiseaseTypeId(Long chronicDiseaseTypeId) {
        this.chronicDiseaseTypeId = chronicDiseaseTypeId;
    }

    public String getChronicDiseaseOther() {
        return chronicDiseaseOther;
    }

    public void setChronicDiseaseOther(String chronicDiseaseOther) {
        this.chronicDiseaseOther = chronicDiseaseOther;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getBmi() {
        return bmi;
    }

    public void setBmi(Double bmi) {
        this.bmi = bmi;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public Double getBloodSugar() {
        return bloodSugar;
    }

    public void setBloodSugar(Double bloodSugar) {
        this.bloodSugar = bloodSugar;
    }

    public String getDoctorSuggestion() {
        return doctorSuggestion;
    }

    public void setDoctorSuggestion(String doctorSuggestion) {
        this.doctorSuggestion = doctorSuggestion;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
