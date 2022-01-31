package eros;

import com.sun.net.httpserver.HttpServer;
import eros.cargo.ElementStorage;
import eros.cargo.ObjectStorage;
import eros.cargo.PropertyStorage;
import eros.jdbc.Repo;
import eros.model.Element;
import eros.model.web.EndpointMappings;
import eros.processor.ElementProcessor;
import eros.processor.EndpointProcessor;
import eros.processor.UxProcessor;
import eros.startup.ExchangeStartup;
import eros.util.Settings;
import eros.util.Support;
import eros.web.HttpTransmission;
import eros.web.Interceptor;
import eros.web.Pointcut;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Eros {

    Support support;
    HttpServer httpServer;
    Map<String, Pointcut> pointcuts;
    Map<String, Interceptor> interceptors;

    public Eros(Builder builder){
        this.support = builder.support;
        this.httpServer = builder.httpServer;
        this.pointcuts = new HashMap<>();
        this.interceptors = new HashMap<>();
    }

    public Eros run() throws Exception {
        UxProcessor uxProcessor = new UxProcessor();
        ExchangeStartup exchangeStartup = new ExchangeStartup(pointcuts, interceptors, uxProcessor);
        exchangeStartup.start();
        HttpTransmission modulator = new HttpTransmission(a8i);
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
        public Eros make() {
            return new Eros(this);
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

        public Data(Data.Builder builder){
            this.pointcuts = builder.pointcuts;
            this.interceptors = builder.interceptors;
            this.settings = builder.settings;
            this.uxProcessor = builder.uxProcessor;
            this.repo = builder.repo;
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
            public Eros.Data make(){
                return new Eros.Data(this);
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