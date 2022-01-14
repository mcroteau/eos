package a8i.support;

import a8i.A8i;
import a8i.model.Element;
import a8i.model.web.EndpointMappings;
import a8i.processor.*;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

import static a8i.A8i.DBMEDIATOR;
import static a8i.A8i.command;

public class Initializer {

    public Initializer(){}

    public static class Builder {

        A8i a8i;

        public Builder with(A8i a8i){
            this.a8i = a8i;
            return this;
        }
        private void setAttributes(){
            A8i a8icopy = createMethodOnlyVersion(a8i);
            Element element = new Element();
            element.setElement(a8icopy);
            a8i.getElementStorage().getElements().put(A8i.A8i, element);
            if(a8i.getResources() == null) a8i.setResources(new ArrayList<>());
            if(a8i.getPropertiesFiles() == null) a8i.setPropertiesFiles(new ArrayList<>());
        }

        private A8i createMethodOnlyVersion(A8i a8i) {
            A8i a8icopy = a8i;
            for(Map.Entry<String, Element> entry: a8i.getElements().entrySet()){
                a8icopy.getElements().remove(entry.getKey());
            }
            return a8icopy;
        }

        private void initDatabase() throws Exception{
            if (a8i.createDb){
                DbMediator mediator = new DbMediator(a8i);
                Element element = new Element();
                element.setElement(mediator);
                a8i.getElementStorage().getElements().put(DBMEDIATOR, element);
                mediator.createDb();
            }
        }

        private void validateDatasource() throws Exception {
            Element element = a8i.getElementStorage().getElements().get(A8i.DATASOURCE);
            if(element != null){
                DataSource dataSource = (DataSource) element.getElement();
                a8i.setDataSource(dataSource);
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
