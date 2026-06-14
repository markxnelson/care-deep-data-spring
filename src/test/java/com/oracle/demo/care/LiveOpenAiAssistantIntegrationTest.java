package com.oracle.demo.care;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "CARE_LIVE_OPENAI_TEST", matches = "true")
@SpringBootTest(properties = {
        "care.assistant.spring-ai-enabled=true",
        "care.deepsec.local-direct-logon=true",
        "care.deepsec.default-end-user=clara",
        "care.deepsec.end-user-password=" + AbstractDeepSecOracleTest.END_USER_PASSWORD,
        "care.security.jwt.local-hmac-enabled=true",
        "care.security.jwt.hmac-secret=0123456789abcdef0123456789abcdef",
        "spring.ai.openai.api-key=${OPENAI_API_KEY}",
        "spring.ai.openai.chat.options.model=${CARE_OPENAI_MODEL:gpt-4.1-mini}",
        "spring.ai.openai.chat.options.temperature=0",
        "spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false"
})
class LiveOpenAiAssistantIntegrationTest extends AbstractDeepSecOracleTest {
    private static final Logger log = LoggerFactory.getLogger(LiveOpenAiAssistantIntegrationTest.class);
    private static final String VECTOR_NAME = "clinician";
    private static final String PROMPT = "Summarize the care cases and policy documents I am allowed to use.";

    static {
        try {
            setupCareSchema();
        } catch (Exception error) {
            throw new ExceptionInInitializerError(error);
        }
    }

    @DynamicPropertySource
    static void deepSecProperties(DynamicPropertyRegistry registry) {
        registry.add("care.deepsec.jdbc-url", AbstractDeepSecOracleTest::jdbcUrl);
    }

    @Autowired
    private CareAssistantService assistantService;

    @Value("${spring.ai.openai.chat.options.model}")
    private String modelName;

    @Test
    void liveOpenAiAssistantShowsDifferentDatabaseReturnedContextForClaraAndDrew() {
        log.info("Running live OpenAI assistant smoke test with model '{}'.", modelName);

        CareService.CareAssistantResponse claraResponse = runAssistantAs("clara");
        CareService.CareAssistantResponse drewResponse = runAssistantAs("drew");

        assertThat(claraResponse.summary())
                .isNotBlank()
                .doesNotContain("Troponin")
                .doesNotContain("Cardiac observation")
                .doesNotContain("Clinician review protocol");
        assertThat(claraResponse.visibleCases())
                .extracting("patientName")
                .contains("Avery Patel", "Morgan Lee")
                .doesNotContain("Jordan Kim");
        assertThat(claraResponse.policyMatches())
                .extracting("title")
                .contains("Shared escalation policy")
                .doesNotContain("Clinician review protocol");

        assertThat(drewResponse.summary())
                .isNotBlank();
        assertThat(drewResponse.visibleCases())
                .extracting("patientName")
                .contains("Avery Patel", "Jordan Kim")
                .doesNotContain("Morgan Lee");
        assertThat(drewResponse.policyMatches())
                .extracting("title")
                .contains("Clinician review protocol", "Shared escalation policy")
                .doesNotContain("Coordinator discharge checklist");
    }

    private CareService.CareAssistantResponse runAssistantAs(String subject) {
        log.info("Running '{}' as {}.", PROMPT, subject);

        CareService.CareAssistantResponse response = assistantService.chat(subject, PROMPT, VECTOR_NAME);

        log.info("""
                Running "{}" as {}, output is:
                Assistant summary:
                {}
                Database-authorized visible cases:
                {}
                Database-authorized policy matches:
                {}
                """,
                PROMPT,
                subject,
                response.summary(),
                response.visibleCases()
                        .stream()
                        .map(patientCase -> "%s [%s]".formatted(patientCase.patientName(), patientCase.caseStatus()))
                        .toList(),
                response.policyMatches()
                        .stream()
                        .map(policy -> "%s (distance=%.4f)".formatted(policy.title(), policy.distance()))
                        .toList());

        return response;
    }
}
