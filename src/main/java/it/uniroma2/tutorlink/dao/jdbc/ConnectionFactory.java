package it.uniroma2.tutorlink.dao.jdbc;

import it.uniroma2.tutorlink.config.AppConfig;
import it.uniroma2.tutorlink.exception.PersistenceException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
    private final String url;
    private final String user;
    private final String password;

    public ConnectionFactory() {
        AppConfig config = AppConfig.getInstance();
        this.url = config.getJdbcUrl();
        this.user = config.getJdbcUser();
        this.password = config.getJdbcPassword();
    }

    public ConnectionFactory(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection open() throws PersistenceException {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new PersistenceException("the database is not reachable: " + url, e);
        }
    }
}
