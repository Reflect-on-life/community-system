package com.example.futurecommunity.dto;

public class CompleteAppointmentRequest {
    private String diagnosisResult;
    private String notes;
    private Long chronicDiseaseTypeId;
    private String chronicDiseaseOther;

    public String getDiagnosisResult() {
        return diagnosisResult;
    }

    public void setDiagnosisResult(String diagnosisResult) {
        this.diagnosisResult = diagnosisResult;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
}
