package a8i.support;

import a8i.A8i;
import a8i.processor.UxProcessor;
import a8i.web.Interceptor;
import a8i.web.Pointcut;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

public class ExchangeStartup {

    UxProcessor uxProcessor;
    Map<String, Pointcut> pointcuts;
    Map<String, Interceptor> interceptors;

    A8i.Cache cache;

    public ExchangeStartup(Map<String, Pointcut> pointcuts,
                           Map<String, Interceptor> interceptors,
                           UxProcessor uxProcessor){
        this.uxProcessor = uxProcessor;
        this.pointcuts = pointcuts;
        this.interceptors = interceptors;
    }

    public void start() throws Exception {

        InputStream is = this.getClass().getResourceAsStream("/src/main/resources/a8i.properties");

        if(is == null) {
            try {
                String uri = A8i.Util.getResourceUri() + File.separator + "a8i.properties";
                is = new FileInputStream(uri);
            } catch (FileNotFoundException fe) {}
        }

        if (is == null) {
            throw new Exception("A8i : a8i.properties not found in src/main/resources/");
        }

        Properties props = new Properties();
        props.load(is);

        Object env = props.get("a8i.env");

        Boolean noAction = true;
        Boolean createDb = false;
        Boolean dropDb = false;
        if(env != null){
            String environment = env.toString().replaceAll("\\s+", "");
            List<String> properties = Arrays.asList(environment.split(","));
            for(String prop : properties){
                if(prop.equals("create")){
                    noAction = false;
                    createDb = true;
                }
                if(prop.equals("drop")){
                    noAction = false;
                    dropDb = true;
                }
                if (prop.equals("update") ||
                        prop.equals("plain") ||
                        prop.equals("basic") ||
                        prop.equals("stub") ||
                        prop.equals("")){
                    noAction = true;
                }
            }
        }

        if(noAction && (createDb || dropDb))
            throw new Exception("You need to either set a8i.env=basic for basic systems that do not need " +
                    "a database connection, or a8i.env=create to create a db using src/main/resource/create-db.sql, " +
                    "or a8i.env=create,drop to both create and drop a database.");

        Object resourcesProp = props.get("a8i.assets");
        Object propertiesProp = props.get("a8i.properties");

        List<String> resourcesPre = new ArrayList<>();
        if(resourcesProp != null){
            String resourceStr = resourcesProp.toString();
            if(!resourceStr.equals("")){
                resourcesPre = Arrays.asList(resourceStr.split(","));
            }
        }
        List<String> propertiesPre = new ArrayList<>();
        if(propertiesProp != null){
            String propString = propertiesProp.toString();
            if(!propString.equals("")){
                propertiesPre = Arrays.asList(propString.split(","));
            }
        }

        List<String> resources = new ArrayList<>();
        if(!resourcesPre.isEmpty()){
            for(String resource: resourcesPre){
                resource = resource.replaceAll("\\s+", "");
                resources.add(resource);
            }
        }

        List<String> propertiesFiles = new ArrayList<>();
        if(!propertiesPre.isEmpty()){
            for(String property : propertiesPre){
                property = property.replaceAll("\\s+","");
                if(property.equals("this")){
                    property = "a8i.properties";
                }
                propertiesFiles.add(property);
            }
        }



        A8i.Conditionals conditionals = new A8i.Conditionals(noAction, createDb, dropDb);

        this.cache = new A8i.Cache();
        cache.setConditionals(conditionals);
        cache.setUxProcessor(uxProcessor);
        cache.setPropertiesFiles(propertiesFiles);
        cache.setResources(resources);
        cache.setInterceptors(interceptors);
        cache.setPointcuts(pointcuts);
        cache.setup();

        A8i.Util util = new A8i.Util();
        Startup startup = new Startup(cache, util);
        startup.start();
    }

    public A8i.Cache getCache(){
        return this.cache;
    }
}
