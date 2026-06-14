package com.oracle.demo.care;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CareAssistantService {
    private final ChatClient.Builder chatClientBuilder;
    private final CareService careService;
    private final boolean springAiEnabled;

    public CareAssistantService(
            ChatClient.Builder chatClientBuilder,
            CareService careService,
            @Value("${care.assistant.spring-ai-enabled:false}") boolean springAiEnabled) {
        this.chatClientBuilder = chatClientBuilder;
        this.careService = careService;
        this.springAiEnabled = springAiEnabled;
    }

    public CareService.CareAssistantResponse chat(String subject, String message, String vectorName) {
        if (!springAiEnabled) {
            return careService.assistantSummary(subject, vectorName);
        }

        String answer = chatClientBuilder.build()
                .prompt()
                .system("""
                        You are a care-coordination assistant.
                        Use the provided tools for case and policy context.
                        Do not infer hidden diagnoses, lab summaries, notes, decisions, or policies.
                        If a tool does not return a value, treat it as unavailable to the current user.
                        """)
                .user(message == null || message.isBlank()
                        ? "Summarize the care context available to me."
                        : message)
                .tools(new CareAssistantTools(subject, careService))
                .call()
                .content();

        CareService.CareAssistantResponse securedContext = careService.assistantSummary(subject, vectorName);
        return new CareService.CareAssistantResponse(
                answer,
                securedContext.visibleCases(),
                securedContext.policyMatches());
    }
}
