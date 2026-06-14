package com.oracle.demo.care;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WritePolicyTest extends AbstractDeepSecOracleTest {
    @Test
    void clinicianUpdateGrantIsRecognizedButDirectLogonDmlFiltersClosed() throws Exception {
        try (var connection = endUserConnection("drew"); Statement statement = connection.createStatement()) {
            List<String> updatePrivileges = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery("""
                    select id,
                           case
                               when ORA_CHECK_DATA_PRIVILEGE(pc, 'UPDATE', clinician_decision)
                               then 'Y'
                               else 'N'
                           end as can_update_decision
                    from care_owner.patient_cases pc
                    order by id
                    """)) {
                while (rs.next()) {
                    updatePrivileges.add(rs.getLong(1) + ":" + rs.getString(2));
                }
            }
            assertThat(updatePrivileges).containsExactly("1:Y", "3:Y");

            int updated = statement.executeUpdate("""
                    update care_owner.patient_cases
                    set clinician_decision = 'follow-up required'
                    where id = 1
                    """);
            assertThat(updated).isZero();

            List<String> decisions = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery("""
                    select clinician_decision
                    from care_owner.patient_cases
                    where needs_clinician_review = 'Y'
                    order by id
                    """)) {
                while (rs.next()) {
                    decisions.add(rs.getString(1));
                }
            }
            assertThat(decisions).containsExactly((String) null, (String) null);
        }
    }

    @Test
    void coordinatorCannotUpdateClinicianDecision() throws Exception {
        try (var connection = endUserConnection("clara"); Statement statement = connection.createStatement()) {
            assertThatThrownBy(() -> statement.executeUpdate("""
                            update care_owner.patient_cases
                            set clinician_decision = 'not allowed'
                            where id = 1
                            """))
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("ORA-41900");
        }
    }
}
