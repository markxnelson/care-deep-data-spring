package com.oracle.demo.care.patient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PatientCaseRepository extends JpaRepository<PatientCaseEntity, Long> {
    @Query("""
            select new com.oracle.demo.care.patient.PatientCase(
                p.id,
                p.patientName,
                p.careTeam,
                p.caseStatus,
                p.assignedCoordinator,
                p.carePlanSummary,
                p.coordinatorNotes,
                p.diagnosis,
                p.sensitiveLabSummary,
                p.clinicianNotes,
                p.clinicianDecision
            )
            from PatientCaseEntity p
            order by p.id
            """)
    List<PatientCase> findVisibleCases();
}
