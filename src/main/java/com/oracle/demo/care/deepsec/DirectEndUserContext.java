package com.oracle.demo.care.deepsec;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.function.Supplier;

@Component
public class DirectEndUserContext {
    private final DataSource dataSource;

    public DirectEndUserContext(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T withEndUser(String endUser, Supplier<T> operation) {
        if (dataSource instanceof DirectEndUserDataSource directEndUserDataSource) {
            return directEndUserDataSource.withEndUser(endUser, operation);
        }
        return operation.get();
    }
}
