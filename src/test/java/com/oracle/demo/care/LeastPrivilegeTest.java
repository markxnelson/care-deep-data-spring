package com.oracle.demo.care;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LeastPrivilegeTest extends AbstractDeepSecOracleTest {
    @Test
    void endUsersDoNotReceiveBroadDatabasePrivileges() throws Exception {
        try (var connection = sysConnection(); Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery("""
                    select count(*)
                    from dba_sys_privs
                    where grantee in ('CLARA', 'DREW')
                      and privilege not in ('CREATE SESSION')
                    """)) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt(1)).isZero();
            }
        }
    }

    @Test
    void mandatoryDataGrantsPreventConventionalSelectBypass() throws Exception {
        try (var connection = sysConnection(); Statement statement = connection.createStatement()) {
            statement.execute("GRANT SELECT ON care_owner.patient_cases TO care_clinician_db_role");
        }

        try (var connection = endUserConnection("drew"); Statement statement = connection.createStatement()) {
            List<Long> ids = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery("select id from care_owner.patient_cases order by id")) {
                while (rs.next()) {
                    ids.add(rs.getLong(1));
                }
            }
            assertThat(ids).containsExactly(1L, 3L);
        }
    }
}
