package com.oracle.demo.care;

import org.junit.jupiter.api.Test;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "care.deepsec.local-direct-logon=true",
        "care.deepsec.default-end-user=clara",
        "care.deepsec.end-user-password=" + AbstractDeepSecOracleTest.END_USER_PASSWORD,
        "care.security.jwt.local-hmac-enabled=true",
        "care.security.jwt.hmac-secret=0123456789abcdef0123456789abcdef",
        "spring.ai.openai.api-key=test",
        "spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false"
})
class SpringAiToolIntegrationTest extends AbstractDeepSecOracleTest {
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
    private CareService careService;

    @Test
    void springAiVisibleCasesToolReturnsOnlyDatabaseAuthorizedRows() {
        ToolCallback callback = toolCallback("visibleCareCases", "clara");

        String result = callback.call("{}");

        assertThat(result)
                .contains("Avery Patel")
                .contains("Morgan Lee")
                .doesNotContain("Cardiac observation")
                .doesNotContain("Troponin");
    }

    @Test
    void springAiPolicyToolReturnsOnlyDatabaseAuthorizedVectorMatches() {
        ToolCallback callback = toolCallback("relevantCarePolicies", "clara");

        String result = callback.call("{\"vector\":\"clinician\"}");

        assertThat(result)
                .contains("Shared escalation policy")
                .doesNotContain("Clinician review protocol");
    }

    private ToolCallback toolCallback(String name, String subject) {
        return Arrays.stream(ToolCallbacks.from(new CareAssistantTools(subject, careService)))
                .filter(callback -> callback.getToolDefinition().name().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
