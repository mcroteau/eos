package eros.jdbc.datasource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class Basic implements DataSource {

    public Basic(Builder builder){
        this.init = true;
        this.dbDriver = builder.dbDriver;
        this.dbUrl = builder.dbUrl;
        this.dbName = builder.dbName;
        this.dbUsername = builder.dbUsername;
        this.dbPassword = builder.dbPassword;
        Basic.DB = this.dbName;
    }

    Boolean init;
    String dbDriver;
    String dbUrl;
    String dbName;
    String dbUsername;
    String dbPassword;
    Integer loginTimeout;
    Connection connection;

    private static String DB;

    public String getDbUrl() {
        return dbUrl;
    }

    public static class Builder{
        String dbUrl;
        String dbName;
        String dbUsername;
        String dbPassword;
        String dbDriver;

        public Builder url(String dbUrl){
            this.dbUrl = dbUrl;
            return this;
        }
        public Builder dbName(String dbName){
            this.dbName = dbName;
            return this;
        }
        public Builder username(String dbUsername){
            this.dbUsername = dbUsername;
            return this;
        }
        public Builder password(String dbPassword){
            this.dbPassword = dbPassword;
            return this;
        }
        public Builder driver(String dbDriver){
            this.dbDriver = dbDriver;
            return this;
        }

        public Basic build(){
            return new Basic(this);
        }

    }


    @Override
    public Connection getConnection() throws SQLException {
        try {

            Class.forName(dbDriver);

            Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            connection.setAutoCommit(false);

            this.connection = connection;

            return connection;
        } catch (SQLException | ClassNotFoundException ex) {
            throw new RuntimeException("Problem connecting to the database", ex);
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        try {
            Class.forName(dbDriver);
            return DriverManager.getConnection(dbUrl, username, password);
        } catch (SQLException | ClassNotFoundException ex) {
            throw new RuntimeException("Problem connecting to the database", ex);
        }
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        this.loginTimeout = seconds;
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return this.loginTimeout;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
