package com.oracle.demo.care;

import com.oracle.demo.care.patient.PatientCase;
import com.oracle.demo.care.policy.CarePolicyMatch;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class CareAssistantTools {
    private final String subject;
    private final CareService careService;

    public CareAssistantTools(String subject, CareService careService) {
        this.subject = subject;
        this.careService = careService;
    }

    @Tool(
            name = "visibleCareCases",
            description = "Returns care cases visible to the authenticated user under Oracle Deep Data Security.")
    public List<PatientCase> visibleCareCases() {
        return careService.visibleCases(subject);
    }

    @Tool(
            name = "relevantCarePolicies",
            description = "Returns vector-search policy matches visible to the authenticated user under Oracle Deep Data Security.")
    public List<CarePolicyMatch> relevantCarePolicies(
            @ToolParam(description = "One of coordinator, clinician, or shared.") String vector) {
        return careService.policyMatches(subject, vector);
    }
}
