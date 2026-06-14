package com.oracle.demo.care;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Test
    void liveOpenAiAssistantKeepsDatabaseReturnedContextAsAuthority() {
        CareService.CareAssistantResponse response = assistantService.chat(
                "clara",
                "Summarize the care cases and policy documents I am allowed to use.",
                "clinician");

        assertThat(response.summary())
                .isNotBlank()
                .doesNotContain("Troponin")
                .doesNotContain("Cardiac observation")
                .doesNotContain("Clinician review protocol");
        assertThat(response.visibleCases())
                .extracting("patientName")
                .contains("Avery Patel", "Morgan Lee")
                .doesNotContain("Jordan Smith");
        assertThat(response.policyMatches())
                .extracting("title")
                .contains("Shared escalation policy")
                .doesNotContain("Clinician review protocol");
    }
}
