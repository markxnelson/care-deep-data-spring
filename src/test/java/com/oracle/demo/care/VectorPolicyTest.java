package com.oracle.demo.care;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VectorPolicyTest extends AbstractDeepSecOracleTest {
    @Test
    void coordinatorVectorSearchReturnsOnlyCoordinatorAndSharedPolicies() throws Exception {
        try (var connection = endUserConnection("clara"); Statement statement = connection.createStatement()) {
            assertThat(policyTitles(statement, "[1,0,0]"))
                    .containsExactly("Coordinator discharge checklist", "Shared escalation policy");
        }
    }

    @Test
    void clinicianVectorSearchReturnsOnlyClinicianAndSharedPolicies() throws Exception {
        try (var connection = endUserConnection("drew"); Statement statement = connection.createStatement()) {
            assertThat(policyTitles(statement, "[0,1,0]"))
                    .containsExactly("Clinician review protocol", "Shared escalation policy");
        }
    }

    private static List<String> policyTitles(Statement statement, String queryVector) throws Exception {
        List<String> titles = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery("""
                select title
                from care_owner.care_policies
                order by vector_distance(embedding, to_vector('%s'), cosine)
                fetch first 10 rows only
                """.formatted(queryVector))) {
            while (rs.next()) {
                titles.add(rs.getString(1));
            }
        }
        return titles;
    }
}
