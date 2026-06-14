package com.oracle.demo.care.policy;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CarePolicyRepository {
    private final JdbcClient jdbcClient;

    public CarePolicyRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<CarePolicyMatch> searchByVector(String queryVector) {
        String sql = """
                select id, title, audience,
                       vector_distance(embedding, to_vector(?), cosine) as distance
                from care_owner.care_policies
                order by distance
                fetch first 10 rows only
                """;

        return jdbcClient.sql(sql)
                .param(queryVector)
                .query((rs, rowNum) -> new CarePolicyMatch(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("audience"),
                        rs.getDouble("distance")))
                .list();
    }
}
