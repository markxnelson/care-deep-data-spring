package com.oracle.demo.care;

import oracle.jdbc.EndUserSecurityContext;
import oracle.jdbc.OracleConnection;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

class DeepSecAvailabilityTest extends AbstractDeepSecOracleTest {
    @Test
    void oracleFreeImageExposesDeepSecVectorAndJdbcContextApis() throws Exception {
        assertThat(EndUserSecurityContext.class.getMethod("createWithName", CharSequence.class, String.class)).isNotNull();
        assertThat(OracleConnection.class.getMethod("setEndUserSecurityContext", EndUserSecurityContext.class)).isNotNull();
        assertThat(OracleConnection.class.getMethod("clearEndUserSecurityContext")).isNotNull();

        try (var connection = sysConnection(); Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery("select banner_full from v$version")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString(1)).contains("Oracle AI Database 26ai Free").contains("23.26.2");
            }
            try (ResultSet rs = statement.executeQuery("""
                    select count(*)
                    from all_objects
                    where object_name in ('DBA_DATA_GRANTS', 'DBA_DATA_ROLES', 'END_USER_CONTEXT')
                    """)) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt(1)).isGreaterThanOrEqualTo(3);
            }
            try (ResultSet rs = statement.executeQuery("""
                    select vector_distance(to_vector('[1,0,0]'), to_vector('[0,1,0]'), cosine)
                    from dual
                    """)) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getDouble(1)).isGreaterThan(0.0);
            }
        }
    }
}
