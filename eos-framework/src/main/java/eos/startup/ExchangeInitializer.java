package eos.startup;

import eos.Eos;
import eos.util.Settings;
import eos.util.Support;
import eos.web.ExperienceProcessor;
import eos.web.Interceptor;
import eos.web.Fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

public class ExchangeInitializer {

    Eos.Cache cache;
    ExperienceProcessor experienceProcessor;
    Map<String, Fragment> pointcuts;
    Map<String, Interceptor> interceptors;

    public ExchangeInitializer(Map<String, Fragment> pointcuts,
                               Map<String, Interceptor> interceptors,
                               ExperienceProcessor experienceProcessor){
        this.experienceProcessor = experienceProcessor;
        this.pointcuts = pointcuts;
        this.interceptors = interceptors;
    }

    public void start() throws Exception {

        Support support = new Support();
        InputStream is = this.getClass().getResourceAsStream("/src/main/resources/eos.props");

        if(is == null) {
            try {
                String uri = support.getResourceUri() + File.separator + "eos.props";
                is = new FileInputStream(uri);
            } catch (FileNotFoundException fe) {}
        }

        if (is == null) {
            throw new Exception("eos.props not found in src/main/resources/");
        }

        Properties props = new Properties();
        props.load(is);

        Object env = props.get("eos.env");

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
            throw new Exception("You need to either set eos.env=basic for basic systems that do not need " +
                    "a database connection, or eos.env=create to create a db using src/main/resource/create-db.sql, " +
                    "or eos.env=create,drop to both create and drop a database.");

        Object resourcesProp = props.get("eos.assets");
        Object propertiesProp = props.get("eos.properties");

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
                    property = "eos.props";
                }
                propertiesFiles.add(property);
            }
        }


        Settings settings = new Settings();
        settings.setCreateDb(createDb);
        settings.setDropDb(dropDb);
        settings.setNoAction(noAction);
        settings.setResources(resources);
        settings.setPropertiesFiles(propertiesFiles);

        Eos.Repo repo = new Eos.Repo();
        this.cache = new Eos.Cache.Builder()
                    .withSettings(settings)
                    .withPointCuts(pointcuts)
                    .withInterceptors(interceptors)
                    .withUxProcessor(experienceProcessor)
                    .withRepo(repo)
                    .make();

        new ContainerStartup.Builder()
                .withRepo(repo)
                .withCache(cache)
                .withSettings(settings)
                .build();

    }

    public Eos.Cache getCache(){
        return this.cache;
    }

}
