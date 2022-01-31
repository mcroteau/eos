package eros;

import eros.jdbc.Repo;
import eros.model.Element;
import eros.model.ObjectDetails;
import eros.model.web.EndpointMappings;
import eros.model.web.HttpRequest;
import eros.processor.ElementProcessor;
import eros.processor.EndpointProcessor;
import eros.processor.UxProcessor;
import eros.cargo.ElementStorage;
import eros.cargo.PropertyStorage;
import eros.startup.Initializer;
import eros.web.Interceptor;
import eros.web.Pointcut;
import com.sun.net.httpserver.Headers;

import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class A8i {

    public static final String A8i         = "eros";
    public static final String SECURITYTAG = "a8i.sessions";
    public static final String DBMEDIATOR  = "dbmediator";
    public static final String DATASOURCE  = "datasource";
    public static final String REDIRECT    = "[redirect]";
    public static final String RESOURCES   = "/src/main/resources/";

    Boolean fatJar;
    Object events;
    ElementStorage elementStorage;
    DataSource dataSource;

    public Boolean createDb;
    public Boolean dropDb;
    public Boolean noAction;
    public String contextPath;
    public String dbScript;

    Map<String, Pointcut> pointcuts;
    Map<String, Interceptor> interceptors;

    List<String> resources;
    List<String> propertiesFiles;
    PropertyStorage propertyStorage;
    EndpointProcessor endpointProcessor;
    ElementProcessor elementProcessor;
    Map<String, ObjectDetails> objects;
    EndpointMappings endpointMappings;

    UxProcessor uxProcessor;

    public A8i(ElementStorage elementStorage){
        this.elementStorage = elementStorage;
    }

    public A8i(Injector injector) throws Exception {
        this.dbScript        = "create-db.sql";
        this.noAction        = injector.noAction;
        this.createDb        = injector.createDb;
        this.dropDb          = injector.dropDb;
        this.contextPath     = injector.contextPath;
        this.resources       = injector.resources;
        this.propertiesFiles = injector.propertyFiles;
        this.uxProcessor = injector.uxProcessor;
        this.pointcuts       = injector.pointcuts;
        this.interceptors    = injector.interceptors;
        this.elementStorage  = new ElementStorage();
        this.propertyStorage = new PropertyStorage();
        this.objects         = new HashMap<>();
        this.fatJar          = getFatJar();
        new Initializer.Builder()
                .with(this, injector.repo)
                .build();
    }

    public Boolean isJar(){
        return this.fatJar;
    }



    public DataSource getDatasource(){
        return this.dataSource;
    }


    public static class Injector{

        Repo repo;
        Boolean noAction;
        Boolean createDb;
        Boolean dropDb;
        List<String> resources;
        List<String> propertyFiles;
        String contextPath;
        UxProcessor uxProcessor;

        Map<String, Pointcut> pointcuts;
        Map<String, Interceptor> interceptors;

        public Injector withRepo(Repo repo){
            this.repo = repo;
            return this;
        }
        public Injector setNoAction(boolean noAction){
            this.noAction = noAction;
            return this;
        }
        public Injector setCreateDb(boolean createDb){
            this.createDb = createDb;
            return this;
        }
        public Injector setDropDb(boolean dropDb){
            this.dropDb = dropDb;
            return this;
        }
        public Injector withPropertyFiles(List<String> propertyFiles){
            this.propertyFiles = propertyFiles;
            return this;
        }
        public Injector withContextPath(String contextPath){
            this.contextPath = contextPath;
            return this;
        }
        public Injector withWebResources(List<String> resources){
            this.resources = resources;
            return this;
        }
        public Injector withViewProcessor(UxProcessor uxProcessor) {
            this.uxProcessor = uxProcessor;
            return this;
        }
        public Injector withPointcuts(Map<String, Pointcut> pointcuts) {
            this.pointcuts = pointcuts;
            return this;
        }
        public Injector withInterceptors(Map<String, Interceptor> interceptors) {
            this.interceptors = interceptors;
            return this;
        }
        public A8i inject() throws Exception {
            return new A8i(this);
        }
    }




}
