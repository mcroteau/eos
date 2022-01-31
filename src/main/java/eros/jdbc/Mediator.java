package eros.jdbc;

import eros.A8i;
import eros.Eros;
import eros.util.Settings;
import eros.util.Support;
import org.h2.tools.RunScript;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static eros.A8i.DATASOURCE;

public class Mediator {

    Support support;
    Settings settings;
    Eros.Cache cache;

    final String CREATEDB_URI = "src/main/resources/create-db.sql";

    public Mediator(Settings settings, Support support, Eros.Cache cache){
        this.support = support;
        this.settings = settings;
        this.cache = cache;
    }

    public void createDb() throws Exception {

        String artifactPath = support.getResourceUri();

        if(!settings.isNoAction() &&
                settings.isCreateDb()) {

            StringBuilder createSql;
            if (support.isJar()) {
                JarFile jarFile = support.getJarFile();
                JarEntry jarEntry = jarFile.getJarEntry(CREATEDB_URI);
                InputStream in = jarFile.getInputStream(jarEntry);
                createSql = support.convert(in);
            } else {
                File createFile = new File(artifactPath + File.separator + "create-db.sql");
                InputStream in = new FileInputStream(createFile);
                createSql = support.convert(in);
            }

            DataSource datasource = (DataSource) cache.getElement("datasource");

            if (datasource == null) {
                throw new Exception("\n\n           " +
                        "You have a8i.env set to create or create,drop in a8i.props.\n           " +
                        "In addition you need to configure a datasource. \n           " +
                        "Feel free to use a8i.jdbc.datasource.Basic to " +
                        "get started.\n" +
                        "           " +
                        "You can also checkout HikariCP, it is great!" +
                        "\n\n" +
                        "           https://github.com/brettwooldridge/HikariCP\n\n\n");
            }
            Connection conn = datasource.getConnection();

            if(settings.isDropDb()) {
                RunScript.execute(conn, new StringReader("drop all objects;"));
            }

            RunScript.execute(conn, new StringReader(createSql.toString()));
            conn.commit();
            conn.close();
        }

    }

    public void dropDb() {

        if(!settings.isNoAction() &&
                settings.isCreateDb()) {

            try {

                DataSource datasource = (DataSource) cache.getElement("datasource");
                Connection conn = datasource.getConnection();

                RunScript.execute(conn, new StringReader("drop all objects;"));
                conn.commit();
                conn.close();

            } catch (Exception e) {
            }
        }
    }

}

