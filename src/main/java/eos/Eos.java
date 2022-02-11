package eos;

import com.sun.net.httpserver.HttpServer;
import eos.cargo.ElementStorage;
import eos.cargo.ObjectStorage;
import eos.cargo.PropertyStorage;
import eos.jdbc.Repo;
import eos.model.Element;
import eos.model.ObjectDetails;
import eos.model.web.EndpointMappings;
import eos.processor.ElementProcessor;
import eos.processor.EndpointProcessor;
import eos.processor.UxProcessor;
import eos.startup.ExchangeStartup;
import eos.util.Settings;
import eos.util.Support;
import eos.web.HttpTransmission;
import eos.web.Interceptor;
import eos.web.Pointcut;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Eos {

    public static final String SECURITYTAG = "eos.sessions";
    public static final String RESOURCES   = "/src/main/resources/";

    Support support;
    HttpServer httpServer;
    Map<String, Pointcut> pointcuts;
    Map<String, Interceptor> interceptors;

    public Eos(Builder builder){
        this.support = builder.support;
        this.httpServer = builder.httpServer;
        this.pointcuts = new HashMap<>();
        this.interceptors = new HashMap<>();
    }

    public Eos run() throws Exception {
        UxProcessor uxProcessor = new UxProcessor();
        ExchangeStartup exchangeStartup = new ExchangeStartup(pointcuts, interceptors, uxProcessor);
        exchangeStartup.start();
        Cache cache = exchangeStartup.getCache();
        HttpTransmission modulator = new HttpTransmission(cache);
        httpServer.createContext("/", modulator);
        httpServer.start();
        return this;
    }

    public boolean registerPointcut(Pointcut pointcut){
        String key = support.getName(pointcut.getClass().getName());
        this.pointcuts.put(key, pointcut);
        return true;
    }

    public boolean registerInterceptor(Interceptor interceptor){
        String key = support.getName(interceptor.getClass().getName());
        this.interceptors.put(key, interceptor);
        return true;
    }

    public static class Builder{
        Integer port;
        HttpServer httpServer;
        ExecutorService executors;
        Support support;

        public Builder withPort(Integer port){
            this.port = port;
            return this;
        }
        public Builder withSupport(Support support){
            this.support = support;
            return this;
        }
        public Builder spawn(int numberThreads) throws IOException {
            this.executors = Executors.newFixedThreadPool(numberThreads);
            this.httpServer = HttpServer.create(new InetSocketAddress(this.port), 0);
            this.httpServer.setExecutor(executors);
            return this;
        }
        public Eos make() {
            return new Eos(this);
        }
    }


    public static class Cache {
        Object events;

        Settings settings;

        Map<String, Pointcut> pointcuts;
        Map<String, Interceptor> interceptors;

        ObjectStorage objectStorage;
        PropertyStorage propertyStorage;
        ElementStorage elementStorage;

        Repo repo;
        UxProcessor uxProcessor;
        EndpointProcessor endpointProcessor;
        ElementProcessor elementProcessor;
        EndpointMappings endpointMappings;

        public Cache(Builder builder){
            this.repo = builder.repo;
            this.pointcuts = builder.pointcuts;
            this.interceptors = builder.interceptors;
            this.settings = builder.settings;
            this.uxProcessor = builder.uxProcessor;
            this.elementStorage = new ElementStorage();
            this.propertyStorage = new PropertyStorage();
            this.objectStorage = new ObjectStorage();
        }
        public Object getElement(String name){
            String key = name.toLowerCase();
            if(elementStorage.getElements().containsKey(key)){
                return elementStorage.getElements().get(key).getElement();
            }
            return null;
        }
        public Map<String, Element> getElements(){
            return this.elementStorage.getElements();
        }
        public ElementStorage getElementStorage() {
            return this.elementStorage;
        }
        public List<String> getResources() {
            return this.settings.getResources();
        }
        public List<String> getPropertiesFiles() {
            return this.settings.getPropertiesFiles();
        }
        public void setResources(List<String> resources) {
            this.settings.setResources(resources);
        }
        public void setPropertiesFiles(List<String> propertiesFiles) {
            this.settings.setPropertiesFiles(propertiesFiles);
        }
        public Map<String, ObjectDetails> getObjects() {
            return this.objectStorage.getObjects();
        }
        public Map<String, Interceptor> getInterceptors() {
            return this.interceptors;
        }
        public UxProcessor getUxProcessor() {
            return this.uxProcessor;
        }
        public Map<String, Pointcut> getPointCuts() {
            return this.pointcuts;
        }

        public static class Builder{

            Repo repo;
            Settings settings;
            UxProcessor uxProcessor;
            Map<String, Pointcut> pointcuts;
            Map<String, Interceptor> interceptors;

            public Builder withSettings(Settings settings) {
                this.settings = settings;
                return this;
            }
            public Builder withPointCuts(Map<String, Pointcut> pointcuts) {
                this.pointcuts = pointcuts;
                return this;
            }
            public Builder withInterceptors(Map<String, Interceptor> interceptors) {
                this.interceptors = interceptors;
                return this;
            }
            public Builder withUxProcessor(UxProcessor uxProcessor) {
                this.uxProcessor = uxProcessor;
                return this;
            }
            public Builder withRepo(Repo repo) {
                this.repo = repo;
                return this;
            }
            public Cache make(){
                return new Cache(this);
            }
        }

        public Object getEvents() {
            return events;
        }

        public void setEvents(Object events) {
            this.events = events;
        }

        public ObjectStorage getObjectStorage() {
            return objectStorage;
        }

        public void setObjectStorage(ObjectStorage objectStorage) {
            this.objectStorage = objectStorage;
        }

        public PropertyStorage getPropertyStorage() {
            return propertyStorage;
        }

        public void setPropertyStorage(PropertyStorage propertyStorage) {
            this.propertyStorage = propertyStorage;
        }

        public EndpointProcessor getEndpointProcessor() {
            return endpointProcessor;
        }

        public void setEndpointProcessor(EndpointProcessor endpointProcessor) {
            this.endpointProcessor = endpointProcessor;
        }

        public ElementProcessor getElementProcessor() {
            return elementProcessor;
        }

        public void setElementProcessor(ElementProcessor elementProcessor) {
            this.elementProcessor = elementProcessor;
        }

        public EndpointMappings getEndpointMappings() {
            return endpointMappings;
        }

        public void setEndpointMappings(EndpointMappings endpointMappings) {
            this.endpointMappings = endpointMappings;
        }


    }

}
