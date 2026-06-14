package com.oracle.demo.care.patient;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "CARE_OWNER", name = "PATIENT_CASES")
public class PatientCaseEntity {
    @Id
    private Long id;

    @Column(name = "PATIENT_NAME")
    private String patientName;

    @Column(name = "CARE_TEAM")
    private String careTeam;

    @Column(name = "CASE_STATUS")
    private String caseStatus;

    @Column(name = "ASSIGNED_COORDINATOR")
    private String assignedCoordinator;

    @Column(name = "CARE_PLAN_SUMMARY")
    private String carePlanSummary;

    @Column(name = "COORDINATOR_NOTES")
    private String coordinatorNotes;

    private String diagnosis;

    @Column(name = "SENSITIVE_LAB_SUMMARY")
    private String sensitiveLabSummary;

    @Column(name = "CLINICIAN_NOTES")
    private String clinicianNotes;

    @Column(name = "CLINICIAN_DECISION")
    private String clinicianDecision;

    protected PatientCaseEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getCareTeam() {
        return careTeam;
    }

    public String getCaseStatus() {
        return caseStatus;
    }

    public String getAssignedCoordinator() {
        return assignedCoordinator;
    }

    public String getCarePlanSummary() {
        return carePlanSummary;
    }

    public String getCoordinatorNotes() {
        return coordinatorNotes;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public String getSensitiveLabSummary() {
        return sensitiveLabSummary;
    }

    public String getClinicianNotes() {
        return clinicianNotes;
    }

    public String getClinicianDecision() {
        return clinicianDecision;
    }
}
