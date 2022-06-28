package eos.startup;

import eos.Eos;
import eos.data.Mediator;
import eos.model.Element;
import eos.model.web.EndpointMappings;
import eos.processor.*;
import eos.util.Settings;
import eos.util.Support;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;


public class ContainerStartup {

    public static class Builder {

        Eos.Cache cache;
        Eos.Repo repo;
        Support support;
        Settings settings;

        public Builder(){
            this.support = new Support();
        }
        public Builder withRepo(Eos.Repo repo){
            this.repo = repo;
            return this;
        }
        public Builder withCache(Eos.Cache cache){
            this.cache = cache;
            return this;
        }
        public Builder withSettings(Settings settings){
            this.settings = settings;
            return this;
        }
        private void setAttributes(){
            Element element = new Element();
            element.setElement(cache);
            cache.getElementStorage().getElements().put("cache", element);

            Element repoElement = new Element();
            repoElement.setElement(repo);
            cache.getElementStorage().getElements().put("repo", repoElement);

            Element supportElement = new Element();
            supportElement.setElement(new Eos.Util());
            cache.getElementStorage().getElements().put("util", supportElement);

            if(cache.getResources() == null) cache.setResources(new ArrayList<>());
            if(cache.getPropertiesFiles() == null) cache.setPropertiesFiles(new ArrayList<>());
        }

        private void initDatabase() throws Exception{
            Mediator mediator = new Mediator(settings, support, cache);
            Element element = new Element();
            element.setElement(mediator);
            cache.getElementStorage().getElements().put("dbmediator", element);
            mediator.createDb();
        }

        private void validateDatasource() throws Exception {
            Element element = cache.getElementStorage().getElements().get("datasource");
            if(element != null){
                DataSource dataSource = (DataSource) element.getElement();
                repo.setDataSource(dataSource);
            }
        }

        private void setDbAttributes() throws Exception {
            validateDatasource();
            initDatabase();
        }

        private void dispatchEvent() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
            if(cache.getEvents() != null) {
                Method setupComplete = cache.getEvents().getClass().getDeclaredMethod("setupComplete", Eos.Cache.class);
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
                new ConfigurationProcessor(cache).run();
            }
        }

        private void runAnnotationProcessor() throws Exception {
            new AnnotationProcessor(cache).run();
        }

        private void runEndpointProcessor() throws Exception {
            EndpointProcessor endpointProcessor = new EndpointProcessor(cache).run();
            EndpointMappings endpointMappings = endpointProcessor.getMappings();
            cache.setEndpointMappings(endpointMappings);
        }

        private void runPropertiesProcessor() throws Exception {
            if(!cache.getPropertiesFiles().isEmpty()) {
                new PropertiesProcessor(cache).run();
            }
        }

        private void runInstanceProcessor() throws Exception {
            new InstanceProcessor(cache).run();
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
            String name = support.getProject();
            System.out.println("[READY!] " + name +"! : o . o . o . o . o . o . o . o . o . o . o . o  ");
        }

        public ContainerStartup build() throws Exception{
            setAttributes();
            runProcessors();
            setDbAttributes();
            dispatchEvent();
            return new ContainerStartup();
        }
    }

}
