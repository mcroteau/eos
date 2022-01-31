package eros.startup;

import eros.A8i;
import eros.Eros;
import eros.jdbc.Mediator;
import eros.jdbc.Repo;
import eros.model.Element;
import eros.model.web.EndpointMappings;
import eros.processor.*;
import eros.util.Support;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static eros.A8i.DBMEDIATOR;
import static eros.A8i.command;

public class Initializer {

    public static class Builder {

        Eros.Cache cache;
        Repo repo;
        Support support;

        public Builder with(Eros.Cache cache, Repo repo){
            this.cache = cache;
            this.repo = repo;
            this.support = support;
            return this;
        }
        private void setAttributes(){
            Element element = new Element();
            element.setElement(cache);
            cache.getElementStorage().getElements().put(A8i.A8i, element);

            Element repoElement = new Element();
            repoElement.setElement(repo);
            cache.getElementStorage().getElements().put("repo", repoElement);

            if(cache.getResources() == null) cache.setResources(new ArrayList<>());
            if(cache.getPropertiesFiles() == null) cache.setPropertiesFiles(new ArrayList<>());
        }

        private void initDatabase() throws Exception{
            Mediator mediator = new Mediator(support, settings, cache);
            Element element = new Element();
            element.setElement(mediator);
            a8i.getElementStorage().getElements().put(DBMEDIATOR, element);
            mediator.createDb();
        }

        private void validateDatasource() throws Exception {
            Element element = a8i.getElementStorage().getElements().get(A8i.DATASOURCE);
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
            if(a8i.getEvents() != null) {
                Method setupComplete = a8i.getEvents().getClass().getDeclaredMethod("setupComplete", A8i.class);
                if(setupComplete != null) {
                    setupComplete.setAccessible(true);
                    setupComplete.invoke(a8i.getEvents(), a8i);
                }
            }
        }

        private void runElementsProcessor() throws Exception {
            ElementProcessor elementsProcessor = new ElementProcessor(a8i).run();
            a8i.setElementProcessor(elementsProcessor);
        }

        private void runConfigProcessor() throws Exception {
            if(a8i.getElementProcessor().getConfigs() != null &&
                    a8i.getElementProcessor().getConfigs().size() > 0){
                new ConfigurationProcessor(a8i).run();
            }
        }

        private void runAnnotationProcessor() throws Exception {
            new AnnotationProcessor(a8i).run();
        }

        private void runEndpointProcessor() throws Exception {
            EndpointProcessor endpointProcessor = new EndpointProcessor(a8i).run();
            EndpointMappings endpointMappings = endpointProcessor.getMappings();
            a8i.setEndpointMappings(endpointMappings);
        }

        private void runPropertiesProcessor() throws Exception {
            if(!a8i.getPropertiesFiles().isEmpty()) {
                new PropertiesProcessor(a8i).run();
            }
        }

        private void runInstanceProcessor() throws Exception {
            new InstanceProcessor(a8i).run();
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
            String name = a8i.getProjectName();
            command("[READY!] " + name +"! : o . o . o . o . o . o . o . o . o . o . o . o  ");
        }

        public Initializer build() throws Exception{
            setAttributes();
            runProcessors();
            setDbAttributes();
            sayReady();
            dispatchEvent();
            return new Initializer();
        }
    }

}
