package com.oracle.demo.care;

import com.oracle.demo.care.deepsec.DirectEndUserContext;
import com.oracle.demo.care.patient.PatientCaseRepository;
import com.oracle.demo.care.patient.PatientCase;
import com.oracle.demo.care.policy.CarePolicyMatch;
import com.oracle.demo.care.policy.CarePolicyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CareService {
    private final DirectEndUserContext endUserContext;
    private final PatientCaseRepository patientCaseRepository;
    private final CarePolicyRepository carePolicyRepository;

    public CareService(
            DirectEndUserContext endUserContext,
            PatientCaseRepository patientCaseRepository,
            CarePolicyRepository carePolicyRepository) {
        this.endUserContext = endUserContext;
        this.patientCaseRepository = patientCaseRepository;
        this.carePolicyRepository = carePolicyRepository;
    }

    public List<PatientCase> visibleCases(String subject) {
        return endUserContext.withEndUser(subject, patientCaseRepository::findVisibleCases);
    }

    public List<CarePolicyMatch> policyMatches(String subject, String vectorName) {
        return endUserContext.withEndUser(subject, () -> carePolicyRepository.searchByVector(vectorFor(vectorName)));
    }

    public CareAssistantResponse assistantSummary(String subject, String vectorName) {
        List<PatientCase> cases = visibleCases(subject);
        List<CarePolicyMatch> policies = policyMatches(subject, vectorName);
        return new CareAssistantResponse(
                "Oracle AI Database returned " + cases.size()
                        + " visible cases and " + policies.size()
                        + " policy matches for " + subject + ".",
                cases,
                policies);
    }

    private static String vectorFor(String vectorName) {
        return switch (vectorName == null ? "" : vectorName.toLowerCase()) {
            case "clinician" -> "[0,1,0]";
            case "shared" -> "[0,0,1]";
            default -> "[1,0,0]";
        };
    }

    public record CareAssistantResponse(
            String summary,
            List<PatientCase> visibleCases,
            List<CarePolicyMatch> policyMatches
    ) {
    }
}
