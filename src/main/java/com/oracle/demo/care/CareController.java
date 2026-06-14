package com.oracle.demo.care;

import com.oracle.demo.care.patient.PatientCase;
import com.oracle.demo.care.policy.CarePolicyMatch;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CareController {
    private final CareService careService;
    private final CareAssistantService careAssistantService;

    public CareController(CareService careService, CareAssistantService careAssistantService) {
        this.careService = careService;
        this.careAssistantService = careAssistantService;
    }

    @GetMapping("/cases")
    List<PatientCase> visibleCases(Authentication authentication) {
        return careService.visibleCases(authentication.getName());
    }

    @GetMapping("/policies/search")
    List<CarePolicyMatch> policyMatches(
            Authentication authentication,
            @RequestParam(defaultValue = "coordinator") String vector) {
        return careService.policyMatches(authentication.getName(), vector);
    }

    @PostMapping("/assistant/chat")
    CareService.CareAssistantResponse assistantSummary(
            Authentication authentication,
            @RequestBody AssistantRequest request) {
        return careAssistantService.chat(authentication.getName(), request.message(), request.vector());
    }

    public record AssistantRequest(String message, String vector) {
    }
}
