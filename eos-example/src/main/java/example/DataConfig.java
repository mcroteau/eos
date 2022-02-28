package example;

import eos.annotate.Config;
import eos.annotate.Dependency;
import eos.annotate.Property;
import eos.datasource.Basic;

import javax.sql.DataSource;

@Config
public class DataConfig {

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
