package com.oracle.demo.care;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RelationalPolicyTest extends AbstractDeepSecOracleTest {
    @Test
    void claraSeesOnlyHerAssignedCases() throws Exception {
        try (var connection = endUserConnection("clara"); Statement statement = connection.createStatement()) {
            List<Long> ids = ids(statement);
            assertThat(ids).containsExactly(1L, 2L);
        }
    }

    @Test
    void drewSeesOnlyCasesThatNeedClinicianReview() throws Exception {
        try (var connection = endUserConnection("drew"); Statement statement = connection.createStatement()) {
            List<Long> ids = ids(statement);
            assertThat(ids).containsExactly(1L, 3L);
        }
    }

    private static List<Long> ids(Statement statement) throws Exception {
        List<Long> ids = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery("select id from care_owner.patient_cases order by id")) {
            while (rs.next()) {
                ids.add(rs.getLong(1));
            }
        }
        return ids;
    }
}
