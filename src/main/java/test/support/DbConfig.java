package test.support;

import a8i.annotate.Config;
import a8i.annotate.Dependency;
import a8i.annotate.Property;
import a8i.jdbc.BasicDataSource;

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
        return new BasicDataSource.Builder()
                .driver(dbDriver)
                .url(dbUrl)
                .username(dbUser)
                .password(dbPass)
                .build();
    }

}
