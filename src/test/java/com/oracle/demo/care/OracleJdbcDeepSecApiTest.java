package com.oracle.demo.care;

import oracle.jdbc.EndUserSecurityContext;
import oracle.jdbc.OracleConnection;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OracleJdbcDeepSecApiTest {
    @Test
    void oracleJdbcDriverExposesDeepSecEndUserContextApi() throws Exception {
        assertThat(EndUserSecurityContext.class.getMethod("createWithName", CharSequence.class, String.class))
                .isNotNull();
        assertThat(EndUserSecurityContext.class.getMethod("withDataRoles", java.util.Collection.class))
                .isNotNull();
        assertThat(OracleConnection.class.getMethod("setEndUserSecurityContext", EndUserSecurityContext.class))
                .isNotNull();
        assertThat(OracleConnection.class.getMethod("clearEndUserSecurityContext"))
                .isNotNull();
    }

    @Test
    void endUserContextFactoryBuildsNameAndDataRolePayloadWithJwtShapedToken() {
        EndUserSecurityContext context = EndUserSecurityContext
                .createWithName(unsignedJwtWithFutureExpiration(), "CLARA")
                .withDataRoles(List.of("CARE_COORDINATOR"));

        assertThat(context.endUserName()).contains("CLARA");
        assertThat(context.dataRoles()).containsExactly("CARE_COORDINATOR");
        assertThat(context.databaseAccessToken()).isNotEmpty();
    }

    private static String unsignedJwtWithFutureExpiration() {
        return base64Url("{\"alg\":\"none\"}") + "." + base64Url("{\"exp\":4102444800}") + ".";
    }

    private static String base64Url(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
