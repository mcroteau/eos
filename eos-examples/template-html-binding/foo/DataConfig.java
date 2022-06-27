package example.foo;

import eos.annotate.Config;
import eos.annotate.Dependency;
import eos.annotate.Property;
import eos.data.BasicDataSource;

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
        return new BasicDataSource.Builder()
                .driver(dbDriver)
                .url(dbUrl)
                .username(dbUser)
                .password(dbPass)
                .build();
    }

}
