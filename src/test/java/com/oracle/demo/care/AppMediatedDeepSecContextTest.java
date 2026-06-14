package com.oracle.demo.care;

import com.oracle.demo.care.deepsec.DeepSecEndUserContext;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppMediatedDeepSecContextTest extends AbstractDeepSecOracleTest {
    @Test
    void pooledApplicationUserContextRequiresTcpsForTokenBasedAuthentication() throws Exception {
        try (var connection = appConnection(); Statement statement = connection.createStatement()) {
            DeepSecEndUserContext.set(
                    connection,
                    unsignedDatabaseAccessToken(),
                    "clara",
                    List.of("care_coordinator"));

            assertThatThrownBy(() -> patientCaseIds(statement))
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("ORA-18718")
                    .hasMessageContaining("TLS")
                    .hasMessageContaining("token-based authentication");

            DeepSecEndUserContext.clear(connection);
        }
    }

    @Test
    void appUserWithoutEndUserContextCannotBypassDeepSecGrants() throws Exception {
        try (var connection = appConnection(); Statement statement = connection.createStatement()) {
            assertThatThrownBy(() -> patientCaseIds(statement))
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("ORA-00942");
        }
    }

    private static List<Long> patientCaseIds(Statement statement) throws Exception {
        List<Long> ids = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery("select id from care_owner.patient_cases order by id")) {
            while (rs.next()) {
                ids.add(rs.getLong(1));
            }
        }
        return ids;
    }
}
