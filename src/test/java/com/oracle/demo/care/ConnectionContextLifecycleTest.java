package com.oracle.demo.care;

import com.oracle.demo.care.deepsec.DeepSecEndUserContext;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

class ConnectionContextLifecycleTest extends AbstractDeepSecOracleTest {
    @Test
    void jdbcContextAdapterCompilesAgainstTheSelectedDriverApi() throws Exception {
        try (Connection connection = sysConnection()) {
            assertThat(connection.isWrapperFor(oracle.jdbc.OracleConnection.class)).isTrue();
            DeepSecEndUserContext.clear(connection);
        }
    }
}
