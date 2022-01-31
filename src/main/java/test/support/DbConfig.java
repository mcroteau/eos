package test.support;

import eros.annotate.Config;
import eros.annotate.Dependency;
import eros.annotate.Property;
import eros.jdbc.datasource.Basic;

import javax.sql.DataSource;

@Config
public class DbConfig {

    @Property("db.url")
    String dbUrl;

    @Property("db.user")
    String dbUser;

    @Property("db.pass")
    String dbPass;

    @Property("db.driver")
    String dbDriver;

    @Dependency
    public DataSource dataSource(){
        return new Basic.Builder()
                .driver(dbDriver)
                .url(dbUrl)
                .username(dbUser)
                .password(dbPass)
                .build();
    }

}
