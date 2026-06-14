package com.oracle.demo.care.patient;

public record PatientCase(
        long id,
        String patientName,
        String careTeam,
        String caseStatus,
        String assignedCoordinator,
        String carePlanSummary,
        String coordinatorNotes,
        String diagnosis,
        String sensitiveLabSummary,
        String clinicianNotes,
        String clinicianDecision
) {
}
