package eos;

import com.sun.net.httpserver.HttpServer;
import eos.model.Element;
import eos.model.ObjectDetails;
import eos.model.web.EndpointMappings;
import eos.processor.ElementProcessor;
import eos.processor.EndpointProcessor;
import eos.startup.ExchangeInitializer;
import eos.storage.ElementStorage;
import eos.storage.ObjectStorage;
import eos.storage.PropertyStorage;
import eos.util.Settings;
import eos.util.Support;
import eos.ux.ExperienceProcessor;
import eos.web.HttpTransmission;
import eos.web.Interceptor;
import eos.web.Pointcut;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EOS {

    public static final String SECURITYTAG = "eos.sessions";
    public static final String RESOURCES   = "/src/main/resources/";

    Support support;
    HttpServer httpServer;
    Map<String, Pointcut> pointcuts;
    Map<String, Interceptor> interceptors;

    public EOS(Builder builder){
        this.support = builder.support;
        this.httpServer = builder.httpServer;
        this.pointcuts = new HashMap<>();
        this.interceptors = new HashMap<>();
    }

    public EOS start() throws Exception {
        ExperienceProcessor experienceProcessor = new ExperienceProcessor();
        ExchangeInitializer exchangeInitializer = new ExchangeInitializer(pointcuts, interceptors, experienceProcessor);
        exchangeInitializer.start();
        Cache cache = exchangeInitializer.getCache();
        HttpTransmission modulator = new HttpTransmission(cache);
        httpServer.createContext("/", modulator);
        httpServer.start();
        return this;
    }

    public EOS stop() throws Exception {
        httpServer.stop(0);
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
        Support support;
        HttpServer httpServer;
        ExecutorService executors;

        public Builder withPort(Integer port){
            this.port = port;
            return this;
        }
        public Builder luminosity(int numberThreads) throws IOException {
            support = new Support();
            this.executors = Executors.newFixedThreadPool(numberThreads);
            this.httpServer = HttpServer.create(new InetSocketAddress(this.port), 0);
            this.httpServer.setExecutor(executors);
            return this;
        }
        public EOS create() {
            return new EOS(this);
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
        ExperienceProcessor experienceProcessor;
        EndpointProcessor endpointProcessor;
        ElementProcessor elementProcessor;
        EndpointMappings endpointMappings;

        public Cache(Builder builder){
            this.repo = builder.repo;
            this.pointcuts = builder.pointcuts;
            this.interceptors = builder.interceptors;
            this.settings = builder.settings;
            this.experienceProcessor = builder.experienceProcessor;
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
        public ExperienceProcessor getUxProcessor() {
            return this.experienceProcessor;
        }
        public Map<String, Pointcut> getPointCuts() {
            return this.pointcuts;
        }

        public static class Builder{

            Repo repo;
            Settings settings;
            ExperienceProcessor experienceProcessor;
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
            public Builder withUxProcessor(ExperienceProcessor experienceProcessor) {
                this.experienceProcessor = experienceProcessor;
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

    public static class Repo {

        DataSource dataSource;

        public void setDataSource(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        public Object get(String preSql, Object[] params, Class<?> cls){
            Object result = null;
            String sql = "";
            try {
                sql = hydrateSql(preSql, params);
                Connection connection = dataSource.getConnection();
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                if(rs.next()){
                    result = extractData(rs, cls);
                }
                if(result == null){
                    throw new Exception(cls + " not found using '" + sql + "'");
                }

                connection.commit();
                connection.close();

            } catch (SQLException ex) {
                System.out.println("bad sql grammar : " + sql);
                System.out.println("\n\n\n");
                ex.printStackTrace();
            } catch (Exception ex) {}

            return result;
        }

        public Integer getInteger(String preSql, Object[] params){
            Integer result = null;
            String sql = "";
            try {
                sql = hydrateSql(preSql, params);
                Connection connection = dataSource.getConnection();
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                if(rs.next()){
                    result = Integer.parseInt(rs.getObject(1).toString());
                }

                if(result == null){
                    throw new Exception("no results using '" + sql + "'");
                }

                connection.commit();
                connection.close();

            } catch (SQLException ex) {
                System.out.println("bad sql grammar : " + sql);
                System.out.println("\n\n\n");
                ex.printStackTrace();
            } catch (Exception ex) {}

            return result;
        }

        public Long getLong(String preSql, Object[] params){
            Long result = null;
            String sql = "";
            try {
                sql = hydrateSql(preSql, params);
                Connection connection = dataSource.getConnection();
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                if(rs.next()){
                    result = Long.parseLong(rs.getObject(1).toString());
                }

                if(result == null){
                    throw new Exception("no results using '" + sql + "'");
                }

                connection.commit();
                connection.close();
            } catch (SQLException ex) {
                System.out.println("bad sql grammar : " + sql);
                System.out.println("\n\n\n");
                ex.printStackTrace();
            } catch (Exception ex) {}

            return result;
        }

        public boolean save(String preSql, Object[] params){
            try {
                String sql = hydrateSql(preSql, params);
                Connection connection = dataSource.getConnection();
                Statement stmt = connection.createStatement();
                stmt.execute(sql);
                connection.commit();
                connection.close();
            }catch(Exception ex){
                ex.printStackTrace();
                return false;
            }
            return true;
        }

        public List<Object> getList(String preSql, Object[] params, Class cls){
            List<Object> results = new ArrayList<>();
            try {
                String sql = hydrateSql(preSql, params);
                Connection connection = dataSource.getConnection();
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                results = new ArrayList<>();
                while(rs.next()){
                    Object obj = extractData(rs, cls);
                    results.add(obj);
                }
                connection.commit();
                connection.close();
            }catch(ClassCastException ccex){
                System.out.println("");
                System.out.println("Wrong Class type, attempted to cast the return data as a " + cls);
                System.out.println("");
                ccex.printStackTrace();
            }catch (Exception ex){ ex.printStackTrace(); }
            return results;
        }

        public boolean update(String preSql, Object[] params){
            try {
                String sql = hydrateSql(preSql, params);
                Connection connection = dataSource.getConnection();
                Statement stmt = connection.createStatement();
                Boolean rs = stmt.execute(sql);
                connection.commit();
                connection.close();
            }catch(Exception ex){
                ex.printStackTrace();
                return false;
            }
            return true;
        }

        public boolean delete(String preSql, Object[] params){
            try {
                String sql = hydrateSql(preSql, params);

                Connection connection = dataSource.getConnection();
                Statement stmt = connection.createStatement();
                stmt.execute(sql);
                connection.commit();
                connection.close();
            }catch(Exception ex){
                return false;
            }
            return true;
        }


        protected String hydrateSql(String sql, Object[] params){
            for(Object object : params){
                if(object != null) {
                    String parameter = object.toString();
                    if (object.getClass().getTypeName().equals("java.lang.String")) {
                        parameter = parameter.replace("'", "''")
                                .replace("$", "\\$")
                                .replace("#", "\\#")
                                .replace("@", "\\@");
                    }
                    sql = sql.replaceFirst("\\[\\+\\]", parameter);
                }else{
                    sql = sql.replaceFirst("\\[\\+\\]", "null");
                }
            }
            return sql;
        }

        protected Object extractData(ResultSet rs, Class cls) throws Exception{
            Object object = new Object();
            Constructor[] constructors = cls.getConstructors();
            for(Constructor constructor: constructors){
                if(constructor.getParameterCount() == 0){
                    object = constructor.newInstance();
                }
            }

            Field[] fields = object.getClass().getDeclaredFields();
            for(Field field: fields){
                field.setAccessible(true);
                String originalName = field.getName();
                String regex = "([a-z])([A-Z]+)";
                String replacement = "$1_$2";
                String name = originalName.replaceAll(regex, replacement).toLowerCase();
                Type type = field.getType();
                if (hasColumn(rs, name)) {
                    if (type.getTypeName().equals("int") || type.getTypeName().equals("java.lang.Integer")) {
                        field.set(object, rs.getInt(name));
                    } else if (type.getTypeName().equals("double") || type.getTypeName().equals("java.lang.Double")) {
                        field.set(object, rs.getDouble(name));
                    } else if (type.getTypeName().equals("float") || type.getTypeName().equals("java.lang.Float")) {
                        field.set(object, rs.getFloat(name));
                    } else if (type.getTypeName().equals("long") || type.getTypeName().equals("java.lang.Long")) {
                        field.set(object, rs.getLong(name));
                    } else if (type.getTypeName().equals("boolean") || type.getTypeName().equals("java.lang.Boolean")) {
                        field.set(object, rs.getBoolean(name));
                    } else if (type.getTypeName().equals("java.math.BigDecimal")) {
                        field.set(object, rs.getBigDecimal(name));
                    } else if (type.getTypeName().equals("java.lang.String")) {
                        field.set(object, rs.getString(name));
                    }
                }
            }
            return object;
        }

        public static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
            ResultSetMetaData rsmd = rs.getMetaData();
            for (int x = 1; x <= rsmd.getColumnCount(); x++) {
                if (columnName.equals(rsmd.getColumnName(x).toLowerCase())) {
                    return true;
                }
            }
            return false;
        }
    }

}
