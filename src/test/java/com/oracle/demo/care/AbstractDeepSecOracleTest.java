package com.oracle.demo.care;

import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;

abstract class AbstractDeepSecOracleTest {
    static final String SYS_PASSWORD = "Oracle_26ai_2026";
    static final String END_USER_PASSWORD = "Care_End_User_2026";
    static final String APP_PASSWORD = "Care_App_2026";
    static final String DEFAULT_IMAGE = "gvenzl/oracle-free:23.26.2-slim-faststart";

    static final GenericContainer<?> oracle = OracleDatabaseHolder.ORACLE;

    @BeforeAll
    static void setupCareSchema() throws Exception {
        try (Connection connection = sysConnection(); Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            cleanup(statement);
            runResourceScript(statement, "/db/deepsec.sql");
            connection.commit();
        }
    }

    static Connection sysConnection() throws SQLException {
        Properties props = new Properties();
        props.put("user", "sys");
        props.put("password", SYS_PASSWORD);
        props.put("internal_logon", "sysdba");
        return DriverManager.getConnection(jdbcUrl(), props);
    }

    static Connection endUserConnection(String user) throws SQLException {
        return DriverManager.getConnection(jdbcUrl(), user, END_USER_PASSWORD);
    }

    static Connection appConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl(), "care_app", APP_PASSWORD);
    }

    static String unsignedDatabaseAccessToken() {
        return "eyJhbGciOiJub25lIn0.eyJleHAiOjQxMDI0NDQ4MDB9.";
    }

    static String jdbcUrl() {
        return "jdbc:oracle:thin:@//" + oracle.getHost() + ":" + oracle.getMappedPort(1521) + "/FREEPDB1";
    }

    static void run(Statement statement, String sql) throws SQLException {
        statement.execute(sql);
    }

    static void runResourceScript(Statement statement, String resourcePath) throws Exception {
        try (var stream = AbstractDeepSecOracleTest.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalStateException("Missing SQL resource: " + resourcePath);
            }
            String script = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            for (String sql : script.split("(?m)^/\\s*$")) {
                String trimmed = sql.strip();
                if (!trimmed.isEmpty()) {
                    run(statement, trimmed);
                }
            }
        }
    }

    static void cleanup(Statement statement) {
        ignore(statement, "DROP DATA GRANT care_owner.policies_for_clinicians");
        ignore(statement, "DROP DATA GRANT care_owner.policies_for_coordinators");
        ignore(statement, "DROP DATA GRANT care_owner.drew_case_write");
        ignore(statement, "DROP DATA GRANT care_owner.cases_for_clinician_writes");
        ignore(statement, "DROP DATA GRANT care_owner.cases_for_clinician_reads");
        ignore(statement, "DROP DATA GRANT care_owner.cases_for_coordinators");
        ignore(statement, "DROP DATA ROLE clinician");
        ignore(statement, "DROP DATA ROLE care_coordinator");
        ignore(statement, "DROP ROLE care_clinician_db_role");
        ignore(statement, "DROP ROLE care_coordinator_db_role");
        ignore(statement, "DROP USER care_app CASCADE");
        ignore(statement, "DROP END USER drew");
        ignore(statement, "DROP END USER clara");
        ignore(statement, "DROP USER care_owner CASCADE");
    }

    static void ignore(Statement statement, String sql) {
        try {
            statement.execute(sql);
        } catch (SQLException ignored) {
            // Cleanup is best-effort so each test run starts from a known state.
        }
    }

    private static final class OracleDatabaseHolder {
        static final GenericContainer<?> ORACLE = new GenericContainer<>(
                DockerImageName.parse(System.getenv().getOrDefault("CARE_ORACLE_IMAGE", DEFAULT_IMAGE))
                        .asCompatibleSubstituteFor("gvenzl/oracle-free"))
                .withExposedPorts(1521)
                .withEnv("ORACLE_PASSWORD", SYS_PASSWORD)
                .withEnv("ORACLE_PWD", SYS_PASSWORD)
                .waitingFor(Wait.forLogMessage("(?s).*DATABASE IS READY TO USE!.*", 1)
                        .withStartupTimeout(Duration.ofMinutes(8)));

        static {
            ORACLE.start();
        }
    }
}
