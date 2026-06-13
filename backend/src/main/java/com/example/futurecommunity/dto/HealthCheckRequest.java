package com.example.futurecommunity.dto;

public class HealthCheckRequest {
    private Long userId;
    private Double height;
    private Double weight;
    private String bloodPressure;
    private Double bloodSugar;
    private Long chronicDiseaseTypeId;
    private String chronicDiseaseOther;
    private String doctorSuggestion;
    private String notes;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
}
