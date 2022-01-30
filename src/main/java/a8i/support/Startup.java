package a8i.support;

import a8i.A8i;
import a8i.model.Element;
import a8i.model.web.EndpointMappings;
import a8i.processor.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class Startup {

    final String PROJECT_NAME = A8i.Util.getProject();

    A8i.Cache cache;
    A8i.Util util;

    public Startup(A8i.Cache cache, A8i.Util util){
        this.cache = cache;
        this.util = util;
    }

    private void setAttributes(){
        Element element = new Element();
        element.setElement(cache);
        cache.getElementStorage().getElements().put("cache", element);

        Element repoElement = new Element();
        A8i.Repo repo = new A8i.Repo(cache.getDataSource());
        repoElement.setElement(repo);
        cache.getElementStorage().getElements().put(A8i.REPO, repoElement);

        if(cache.getResources() == null) cache.setResources(new ArrayList<>());
        if(cache.getPropertiesFiles() == null) cache.setPropertiesFiles(new ArrayList<>());
    }

    private void initDatabase() throws Exception{
        DbMediator mediator = new DbMediator(cache);
        Element element = new Element();
        element.setElement(mediator);
        cache.getElementStorage().getElements().put("dbmediator", element);
        mediator.createDb();
    }

    private void dispatchEvent() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if(cache.getEvents() != null) {
            Method setupComplete = cache.getEvents().getClass().getDeclaredMethod("setupComplete", A8i.class);
            if(setupComplete != null) {
                setupComplete.setAccessible(true);
                setupComplete.invoke(cache.getEvents(), cache);
            }
        }
    }

    private void runElementsProcessor() throws Exception {
        ElementProcessor elementsProcessor = new ElementProcessor(cache).run();
        cache.setElementProcessor(elementsProcessor);
    }

    private void runConfigProcessor() throws Exception {
        if(cache.getElementProcessor().getConfigs() != null &&
                cache.getElementProcessor().getConfigs().size() > 0){
            new ConfigurationProcessor(cache, util).run();
        }
    }

    private void runAnnotationProcessor() throws Exception {
        new AnnotationProcessor(cache, util).run();
    }

    private void runEndpointProcessor() throws Exception {
        EndpointProcessor endpointProcessor = new EndpointProcessor(cache).run();
        EndpointMappings endpointMappings = endpointProcessor.getMappings();
        cache.setEndpointMappings(endpointMappings);
    }

    private void runPropertiesProcessor() throws Exception {
        if(!cache.getPropertiesFiles().isEmpty()) {
            new PropertiesProcessor(cache, util).run();
        }
    }

    private void runInstanceProcessor() throws Exception {
        new InstanceProcessor(cache, util).run();
    }

    private void runProcessors() throws Exception {
        runPropertiesProcessor();
        runInstanceProcessor();
        runElementsProcessor();
        runConfigProcessor();
        runAnnotationProcessor();
        runEndpointProcessor();
    }

    private void sayReady(){
        System.out.println("[READY!] "+this.PROJECT_NAME+"! : o . o . o . o . o . o . o . o . o . o . o . o  ");
    }

    public void start() throws Exception {
        setAttributes();
        runProcessors();
        initDatabase();
        dispatchEvent();
        sayReady();
    }
}
