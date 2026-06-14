package com.oracle.demo.care;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.crypto.spec.SecretKeySpec;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "care.deepsec.local-direct-logon=true",
        "care.deepsec.default-end-user=clara",
        "care.deepsec.end-user-password=" + AbstractDeepSecOracleTest.END_USER_PASSWORD,
        "care.security.jwt.local-hmac-enabled=true",
        "care.security.jwt.hmac-secret=0123456789abcdef0123456789abcdef",
        "spring.ai.openai.api-key=test",
        "spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false"
})
@AutoConfigureMockMvc
class CareApiSpringIntegrationTest extends AbstractDeepSecOracleTest {
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
    private MockMvc mockMvc;

    @Test
    void apiRejectsRequestsWithoutBearerToken() throws Exception {
        mockMvc.perform(get("/api/cases"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwtAuthenticatedEndpointReturnsCoordinatorCasesThroughJpaRepository() throws Exception {
        mockMvc.perform(get("/api/cases").header("Authorization", bearerToken("clara")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(content().string(not(containsString("Cardiac observation"))))
                .andExpect(content().string(not(containsString("Troponin"))));
    }

    @Test
    void jwtAuthenticatedEndpointReturnsClinicianCasesThroughSameJpaRepository() throws Exception {
        mockMvc.perform(get("/api/cases").header("Authorization", bearerToken("drew")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(3))
                .andExpect(content().string(containsString("Cardiac observation")))
                .andExpect(content().string(not(containsString("Needs transport assistance"))));
    }

    @Test
    void jwtAuthenticatedEndpointReturnsDeepSecFilteredVectorPolicies() throws Exception {
        mockMvc.perform(get("/api/policies/search?vector=clinician").header("Authorization", bearerToken("drew")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Clinician review protocol")))
                .andExpect(content().string(containsString("Shared escalation policy")))
                .andExpect(content().string(not(containsString("Coordinator discharge checklist"))));
    }

    @Test
    void jwtAuthenticatedAssistantEndpointSummarizesOnlySecuredServiceResults() throws Exception {
        mockMvc.perform(post("/api/assistant/chat")
                        .contentType("application/json")
                        .content("{\"message\":\"summarize care context\",\"vector\":\"coordinator\"}")
                        .header("Authorization", bearerToken("clara")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Oracle AI Database returned 2 visible cases and 2 policy matches for clara."))
                .andExpect(content().string(containsString("Coordinator discharge checklist")))
                .andExpect(content().string(not(containsString("Clinician review protocol"))));
    }

    private static String bearerToken(String subject) throws Exception {
        var claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer("care-local-test")
                .issueTime(java.util.Date.from(Instant.now()))
                .expirationTime(java.util.Date.from(Instant.now().plusSeconds(300)))
                .build();
        var jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(),
                claims);
        var key = new SecretKeySpec(
                "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");
        jwt.sign(new MACSigner(key));
        return "Bearer " + jwt.serialize();
    }
}
