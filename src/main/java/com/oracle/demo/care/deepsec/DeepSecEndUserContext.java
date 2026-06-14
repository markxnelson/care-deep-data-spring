package com.oracle.demo.care.deepsec;

import oracle.jdbc.EndUserSecurityContext;
import oracle.jdbc.OracleConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

public final class DeepSecEndUserContext {
    private DeepSecEndUserContext() {
    }

    public static void set(Connection connection, CharSequence databaseAccessToken, String endUserName, Collection<String> dataRoles)
            throws SQLException {
        EndUserSecurityContext context = EndUserSecurityContext
                .createWithName(databaseAccessToken, endUserName)
                .withDataRoles(dataRoles);

        connection.unwrap(OracleConnection.class).setEndUserSecurityContext(context);
    }

    public static void clear(Connection connection) throws SQLException {
        connection.unwrap(OracleConnection.class).clearEndUserSecurityContext();
    }
}
