package foo;

import eos.annotate.Configuration;
import eos.annotate.Dependency;
import eos.annotate.Property;
import eos.data.BasicDataSource;

import javax.sql.DataSource;

@Configuration
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
        System.out.println("dependency");
        return new BasicDataSource.Builder()
                .driver(dbDriver)
                .url(dbUrl)
                .username(dbUser)
                .password(dbPass)
                .build();
    }

}
