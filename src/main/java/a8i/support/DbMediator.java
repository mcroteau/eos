package a8i.support;

import a8i.A8i;
import org.h2.tools.RunScript;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static a8i.A8i.DATASOURCE;

public class DbMediator {

    A8i a8i;

    final String CREATEDB_URI = "src/main/resources/create-db.sql";

    public DbMediator(A8i a8i){
        this.a8i = a8i;
    }

    public Boolean createDb() throws Exception {

        String artifactPath = A8i.getResourceUri(a8i.getContextPath());

        if(!a8i.noAction &&
                a8i.createDb) {

            StringBuilder createSql;
            if (a8i.isJar()) {
                JarFile jarFile = a8i.getJarFile();
                JarEntry jarEntry = jarFile.getJarEntry(CREATEDB_URI);
                InputStream in = jarFile.getInputStream(jarEntry);
                createSql = a8i.convert(in);
            } else {
                File createFile = new File(artifactPath + File.separator + a8i.getDbScript());
                InputStream in = new FileInputStream(createFile);
                createSql = a8i.convert(in);
            }

            DataSource datasource = (DataSource) a8i.getElement(DATASOURCE);

            if (datasource == null) {
                throw new Exception("\n\n           " +
                        "You have a8i.env set to create or create,drop in a8i.props.\n           " +
                        "In addition you need to configure a datasource. \n           " +
                        "Feel free to use a8i.jdbc.BasicDataSource to " +
                        "get started.\n" +
                        "           " +
                        "You can also checkout HikariCP, it is great!" +
                        "\n\n" +
                        "           https://github.com/brettwooldridge/HikariCP\n\n\n");
            }
            Connection conn = datasource.getConnection();

            if(a8i.dropDb) {
                RunScript.execute(conn, new StringReader("drop all objects;"));
            }

            RunScript.execute(conn, new StringReader(createSql.toString()));
            conn.commit();
            conn.close();
        }

        return true;
    }

    public Boolean dropDb() {

        if(!a8i.noAction &&
                a8i.dropDb) {

            try {

                DataSource datasource = (DataSource) a8i.getElement(DATASOURCE);
                Connection conn = datasource.getConnection();

                RunScript.execute(conn, new StringReader("drop all objects;"));
                conn.commit();
                conn.close();

            } catch (Exception e) {
            }

        }

        return true;
    }


}

