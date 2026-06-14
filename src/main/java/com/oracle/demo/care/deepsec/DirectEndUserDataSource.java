package com.oracle.demo.care.deepsec;

import org.springframework.jdbc.datasource.AbstractDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Supplier;

public class DirectEndUserDataSource extends AbstractDataSource {
    private final String jdbcUrl;
    private final String defaultEndUser;
    private final String endUserPassword;
    private final ThreadLocal<String> currentEndUser = new ThreadLocal<>();

    public DirectEndUserDataSource(String jdbcUrl, String defaultEndUser, String endUserPassword) {
        this.jdbcUrl = Objects.requireNonNull(jdbcUrl, "jdbcUrl");
        this.defaultEndUser = Objects.requireNonNull(defaultEndUser, "defaultEndUser");
        this.endUserPassword = Objects.requireNonNull(endUserPassword, "endUserPassword");
    }

    public <T> T withEndUser(String endUser, Supplier<T> operation) {
        String previous = currentEndUser.get();
        currentEndUser.set(Objects.requireNonNull(endUser, "endUser"));
        try {
            return operation.get();
        } finally {
            if (previous == null) {
                currentEndUser.remove();
            } else {
                currentEndUser.set(previous);
            }
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getConnection(currentEndUser.get() == null ? defaultEndUser : currentEndUser.get(), endUserPassword);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }
}
