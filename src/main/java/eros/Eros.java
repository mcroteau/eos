package eros;

import com.sun.net.httpserver.HttpServer;
import eros.processor.UxProcessor;
import eros.startup.ExchangeStartup;
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

    A8i a8i;
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
        this.a8i = exchangeStartup.start();
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
}
