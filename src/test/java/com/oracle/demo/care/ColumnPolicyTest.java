package com.oracle.demo.care;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

class ColumnPolicyTest extends AbstractDeepSecOracleTest {
    @Test
    void coordinatorSensitiveClinicalFieldsAreMaskedAsNull() throws Exception {
        try (var connection = endUserConnection("clara"); Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery("""
                    select diagnosis, sensitive_lab_summary, clinician_notes
                    from care_owner.patient_cases
                    where id = 1
                    """)) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("diagnosis")).isNull();
                assertThat(rs.getString("sensitive_lab_summary")).isNull();
                assertThat(rs.getString("clinician_notes")).isNull();
            }
        }
    }

    @Test
    void clinicianCoordinatorNotesAreMaskedAsNull() throws Exception {
        try (var connection = endUserConnection("drew"); Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery("""
                    select coordinator_notes, diagnosis, sensitive_lab_summary
                    from care_owner.patient_cases
                    where id = 1
                    """)) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("coordinator_notes")).isNull();
                assertThat(rs.getString("diagnosis")).isEqualTo("Cardiac observation");
                assertThat(rs.getString("sensitive_lab_summary")).contains("Troponin");
            }
        }
    }
}
