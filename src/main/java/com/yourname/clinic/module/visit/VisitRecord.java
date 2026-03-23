package com.yourname.clinic.module.visit;

public class VisitRecord {
    private Long id;
    private String visitNo;
    private Long patientId;
    private String visitDate;
    private String complaint;
    private String diagnosis;
    private String treatmentPlan;
    private String advice;
    private String remark;
    private String createdAt;
    private String updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVisitNo() { return visitNo; }
    public void setVisitNo(String visitNo) { this.visitNo = visitNo; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getVisitDate() { return visitDate; }
    public void setVisitDate(String visitDate) { this.visitDate = visitDate; }

    public String getComplaint() { return complaint; }
    public void setComplaint(String complaint) { this.complaint = complaint; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getTreatmentPlan() { return treatmentPlan; }
    public void setTreatmentPlan(String treatmentPlan) { this.treatmentPlan = treatmentPlan; }

    public String getAdvice() { return advice; }
    public void setAdvice(String advice) { this.advice = advice; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}