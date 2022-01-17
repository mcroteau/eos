package a8i;

import a8i.model.Element;
import a8i.model.ObjectDetails;
import a8i.model.web.EndpointMappings;
import a8i.model.web.HttpRequest;
import a8i.processor.ElementProcessor;
import a8i.processor.EndpointProcessor;
import a8i.processor.UxProcessor;
import a8i.storage.ElementStorage;
import a8i.storage.PropertyStorage;
import a8i.support.ExchangeStartup;
import a8i.support.Initializer;
import a8i.web.HttpTransmission;
import a8i.web.Interceptor;
import a8i.web.Pointcut;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;

import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class A8i {

    public static final String A8i         = "a8i";
    public static final String SECURITYTAG = "a8i.sessions";
    public static final String DBMEDIATOR  = "dbmediator";
    public static final String DATASOURCE  = "datasource";
    public static final String REDIRECT    = "[redirect]";
    public static final String RESOURCES   = "/src/main/resources/";

    public static class Server{

        A8i a8i;
        HttpServer httpServer;
        Map<String, Pointcut> pointcuts;
        Map<String, Interceptor> interceptors;

        public Server(ServerBuilder builder){
            this.httpServer = builder.httpServer;
            this.pointcuts = new HashMap<>();
            this.interceptors = new HashMap<>();
        }

        public A8i run() throws Exception {
            UxProcessor uxProcessor = new UxProcessor();
            ExchangeStartup exchangeStartup = new ExchangeStartup(pointcuts, interceptors, uxProcessor);
            this.a8i = exchangeStartup.start();
            HttpTransmission modulator = new HttpTransmission(a8i);
            httpServer.createContext("/", modulator);
            httpServer.start();
            return a8i;
        }

        public boolean registerPointcut(Pointcut pointcut){
            String key = getName(pointcut.getClass().getName());
            this.pointcuts.put(key, pointcut);
            return true;
        }

        public boolean registerInterceptor(Interceptor interceptor){
            String key = getName(interceptor.getClass().getName());
            this.interceptors.put(key, interceptor);
            return true;
        }

    }


    public static class ServerBuilder{
        Integer port;
        HttpServer httpServer;
        ExecutorService executors;

        public ServerBuilder withPort(Integer port){
            this.port = port;
            return this;
        }
        public ServerBuilder spawn(int numberThreads) throws IOException {
            this.executors = Executors.newFixedThreadPool(numberThreads);
            this.httpServer = HttpServer.create(new InetSocketAddress(this.port), 0);
            this.httpServer.setExecutor(executors);
            return this;
        }
        public Server make() {
            return new Server(this);
        }
    }

    Boolean fatJar;
    Object events;
    ElementStorage elementStorage;
    DataSource dataSource;

    public Boolean createDb;
    public Boolean dropDb;
    public Boolean noAction;
    public String contextPath;
    public String dbScript;

    Map<String, Pointcut> pointcuts;
    Map<String, Interceptor> interceptors;

    List<String> resources;
    List<String> propertiesFiles;
    PropertyStorage propertyStorage;
    EndpointProcessor endpointProcessor;
    ElementProcessor elementProcessor;
    Map<String, ObjectDetails> objects;
    EndpointMappings endpointMappings;

    UxProcessor uxProcessor;

    public A8i(ElementStorage elementStorage){
        this.elementStorage = elementStorage;
    }

    public A8i(Injector injector) throws Exception {
        this.dbScript        = "create-db.sql";
        this.noAction        = injector.noAction;
        this.createDb        = injector.createDb;
        this.dropDb          = injector.dropDb;
        this.contextPath     = injector.contextPath;
        this.resources       = injector.resources;
        this.propertiesFiles = injector.propertyFiles;
        this.uxProcessor = injector.uxProcessor;
        this.pointcuts       = injector.pointcuts;
        this.interceptors    = injector.interceptors;
        this.elementStorage  = new ElementStorage();
        this.propertyStorage = new PropertyStorage();
        this.objects         = new HashMap<>();
        this.fatJar          = getFatJar();
        new Initializer.Builder()
                .with(this)
                .build();
    }

    public Boolean isJar(){
        return this.fatJar;
    }

    private Boolean getFatJar(){
        String uri = null;
        try {
            uri = getClassesUri();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uri.contains("jar:file:") ? true : false;
    }

    public DataSource getDatasource(){
        return this.dataSource;
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


    public static Object get(HttpRequest request, Class cls){
        Object object =  null;
        try {
            object = cls.getConstructor().newInstance();
            Field[] fields = cls.getDeclaredFields();
            for(Field field : fields){
                String name = field.getName();
                String value = request.value(name);
                if(value != null &&
                        !value.equals("")){

                    field.setAccessible(true);

                    Type type = field.getType();

                    if (type.getTypeName().equals("int") || type.getTypeName().equals("java.lang.Integer")) {
                        field.set(object, Integer.valueOf(value));
                    }
                    else if (type.getTypeName().equals("double") || type.getTypeName().equals("java.lang.Double")) {
                        field.set(object, Double.valueOf(value));
                    }
                    else if (type.getTypeName().equals("float") || type.getTypeName().equals("java.lang.Float")) {
                        field.set(object, Float.valueOf(value));
                    }
                    else if (type.getTypeName().equals("long") || type.getTypeName().equals("java.lang.Long")) {
                        field.set(object, Long.valueOf(value));
                    }
                    else if (type.getTypeName().equals("boolean") || type.getTypeName().equals("java.lang.Boolean")) {
                        field.set(object, Boolean.valueOf(value));
                    }
                    else if (type.getTypeName().equals("java.math.BigDecimal")) {
                        field.set(object, new BigDecimal(value));
                    }
                    else if (type.getTypeName().equals("java.lang.String")) {
                        field.set(object, value);
                    }
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return object;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String removeLast(String s) {
        return (s == null || s.length() == 0)
                ? ""
                : (s.substring(0, s.length() - 1));
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


    public static class Injector{

        Boolean noAction;
        Boolean createDb;
        Boolean dropDb;
        List<String> resources;
        List<String> propertyFiles;
        String contextPath;
        UxProcessor uxProcessor;

        Map<String, Pointcut> pointcuts;
        Map<String, Interceptor> interceptors;

        public Injector(){}

        public Injector setNoAction(boolean noAction){
            this.noAction = noAction;
            return this;
        }
        public Injector setCreateDb(boolean createDb){
            this.createDb = createDb;
            return this;
        }
        public Injector setDropDb(boolean dropDb){
            this.dropDb = dropDb;
            return this;
        }
        public Injector withPropertyFiles(List<String> propertyFiles){
            this.propertyFiles = propertyFiles;
            return this;
        }
        public Injector withContextPath(String contextPath){
            this.contextPath = contextPath;
            return this;
        }
        public Injector withWebResources(List<String> resources){
            this.resources = resources;
            return this;
        }
        public Injector withViewProcessor(UxProcessor uxProcessor) {
            this.uxProcessor = uxProcessor;
            return this;
        }
        public Injector withPointcuts(Map<String, Pointcut> pointcuts) {
            this.pointcuts = pointcuts;
            return this;
        }
        public Injector withInterceptors(Map<String, Interceptor> interceptors) {
            this.interceptors = interceptors;
            return this;
        }
        public A8i inject() throws Exception {
            return new A8i(this);
        }
    }

    public String getDbUrl() throws Exception {
        if(propertyStorage.getProperties().containsKey("db.url")){
            return propertyStorage.getProperties().get("db.url");
        }

        if(!noAction &&
                !propertyStorage.getProperties().containsKey("db.url")){
            throw new Exception("\n\n           Reminder, in order to be in dev mode \n" +
                    "           you need to configure a datasource.\n\n\n");
        }

        throw new Exception("           \n\ndb.url is missing from a8i.props file\n\n\n");

    }

    public static void command(String command){
        try {
            System.out.println(new String(command.getBytes(), "UTF-8"));
        }catch(UnsupportedEncodingException ueex){
            ueex.printStackTrace();
        }
    }

    public String getResourceUri() throws Exception{
        return getResourceUri(this.contextPath);
    }

    public static String getResourceUri(String contextPath) throws Exception{
        String resourceUri = Paths.get("src", "main", "resources")
                .toAbsolutePath()
                .toString();
        File resourceDir = new File(resourceUri);
        if(resourceDir.exists()){
            return resourceUri;
        }
        String classesUri = Paths.get("webapps", contextPath, "WEB-INF", "classes")
                .toAbsolutePath()
                .toString();
        File classesDir = new File(classesUri);
        if(classesDir.exists()) {
            return classesUri;
        }

        final String RESOURCES_URI = "/src/main/resources/";
        URL indexUri = A8i.class.getResource(RESOURCES_URI);
        if (indexUri == null) {
            throw new FileNotFoundException("A8i : unable to find resource " + RESOURCES_URI);
        }

        return indexUri.toURI().toString();

    }

    public String getClassesUri() throws Exception {
        String classesUri = Paths.get("webapps", getContextPath(), "WEB-INF", "classes")
                .toAbsolutePath()
                .toString();
        File classesDir = new File(classesUri);
        if(classesDir.exists()){
            return classesUri;
        }

        classesUri = Paths.get("src", "main", "java")
                .toAbsolutePath()
                .toString();
        classesDir = new File(classesUri);
        if(classesDir.exists()){
            return classesUri;
        }

        classesUri = this.getClass().getResource("").toURI().toString();
        if(classesUri == null){
            throw new Exception("A8i : unable to locate class uri");
        }
        return classesUri;
    }

    public static String getTypeName(String typeName) {
        int index = typeName.lastIndexOf(".");
        if(index > 0){
            typeName = typeName.substring(index + 1);
        }
        return typeName;
    }

    public static String getName(String nameWithExt){
        int index = nameWithExt.lastIndexOf(".");
        String qualifiedName = nameWithExt;
        if(index > 0){
            qualifiedName = qualifiedName.substring(index + 1);
        }
        return qualifiedName.toLowerCase();
    }

    public String getMain(){
        try {
            JarFile jarFile = getJarFile();
            JarEntry jarEntry = jarFile.getJarEntry("META-INF/MANIFEST.MF");
            InputStream in = jarFile.getInputStream(jarEntry);
            Scanner scanner = new Scanner(in);

            String line = "";
            do{
                line = scanner.nextLine();
                if(line.contains("Main-Class")){
                    line = line.replace("Main-Class", "");
                    break;
                }
            }
            while(scanner.hasNext());

            line = line.replace(":", "").trim();
            return line;

        } catch (IOException ioex) {
            ioex.printStackTrace();
        }


        throw new IllegalStateException("Apologies, it seems you are trying to run this as a jar but have not main defined.");
    }

    public Enumeration getJarEntries(){
        JarFile jarFile = getJarFile();
        return jarFile.entries();
    }

    public JarFile getJarFile(){
        try {
            URL jarUri = A8i.class.getClassLoader().getResource("a8i/");
            String jarPath = jarUri.getPath().substring(5, jarUri.getPath().indexOf("!"));

            return new JarFile(jarPath);
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return null;
    }


    public static Enumeration getEntries(){
        try {
            URL jarUriTres = A8i.class.getClassLoader().getResource("a8i/");
            String jarPath = jarUriTres.getPath().substring(5, jarUriTres.getPath().indexOf("!"));

            return new JarFile(jarPath).entries();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return null;
    }

    public String getProjectName() {
        if(isJar()) {
            JarFile jarFile = getJarFile();
            String path = jarFile.getName();
            String[] bits = path.split("/");
            if(bits.length == 0){
                bits = path.split("\\");
            }
            String namePre = bits[bits.length - 1];
            return namePre.replace(".jar", "");
        }else{
            return this.getContextPath();
        }
    }

    public StringBuilder convert(InputStream in){
        StringBuilder builder = new StringBuilder();
        Scanner scanner = new Scanner(in);
        do{
            builder.append(scanner.nextLine());
        }while(scanner.hasNext());
        try {
            in.close();
        }catch (IOException ioex) {
            ioex.printStackTrace();
        }
        return builder;
    }

    public static String getCookie(String cookieName, Headers headers){
        String value = "";
        if(headers != null) {
            List<String> cookies = headers.get("Cookie");
            if(cookies != null) {
                for (String cookie : cookies) {
                    String[] bits = cookie.split(";");
                    for (String completes : bits) {
                        String[] parts = completes.split("=");
                        String key = parts[0].trim();
                        if (parts.length > 1) {

                            if (key.equals(cookieName)) {
                                value = parts[1].trim();
                            }
                        }
                    }
                }
            }
        }
        //returning the last one.
        return value;
    }

    public static String GUID(int z) {
        String CHARS = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz1234567890";
        StringBuilder uuid = new StringBuilder();
        Random rnd = new Random();
        while (uuid.length() < z) {
            int index = (int) (rnd.nextFloat() * CHARS.length());
            uuid.append(CHARS.charAt(index));
        }
        return uuid.toString();
    }

    public static String SESSION_GUID(int z) {
        String CHARS = ".01234567890";
        StringBuilder guid = new StringBuilder();
        guid.append("A8i.");
        Random rnd = new Random();
        while (guid.length() < z) {
            int index = (int) (rnd.nextFloat() * CHARS.length());
            guid.append(CHARS.charAt(index));
        }
        return guid.toString();
    }

    public String getPayload(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append((char) b);
        }
        return sb.toString();
    }

    public byte[] getPayloadBytes(InputStream requestStream) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            byte[] buf = new byte[1024 * 19];
            int bytesRead=0;
            while ((bytesRead = requestStream.read(buf)) != -1){
                bos.write(buf, 0, bytesRead);
            }
            requestStream.close();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bos.toByteArray();
    }

    public static <T> Collector<T, ?, T> toSingleton() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1) {
                        throw new IllegalStateException();
                    }
                    return list.get(0);
                }
        );
    }
    public String getContextPath(){
        return this.contextPath;
    }
    public String getDbScript(){
        return this.dbScript;
    }
    public Object getEvents(){
        return this.events;
    }
    public void setEvents(Object events){
        this.events = events;
    }
    public List<String> getResources(){
        return this.resources;
    }
    public void setResources(List<String> resources){
        this.resources = resources;
    }
    public List<String> getPropertiesFiles(){
        return this.propertiesFiles;
    }
    public void setPropertiesFiles(List<String> propertiesFiles){
        this.propertiesFiles = propertiesFiles;
    }
    public ElementStorage getElementStorage(){
        return this.elementStorage;
    }
    public PropertyStorage getPropertyStorage(){
        return this.propertyStorage;
    }
    public EndpointProcessor getEndpointProcessor(){
        return this.endpointProcessor;
    }
    public void setEndpointProcessor(EndpointProcessor endpointProcessor){ this.endpointProcessor = endpointProcessor; }
    public ElementProcessor getElementProcessor(){
        return this.elementProcessor;
    }
    public void setElementProcessor(ElementProcessor elementProcessor){
        this.elementProcessor = elementProcessor;
    }
    public EndpointMappings getEndpointMappings() {
        return endpointMappings;
    }
    public void setEndpointMappings(EndpointMappings endpointMappings) {
        this.endpointMappings = endpointMappings;
    }
    public Map<String, ObjectDetails> getObjects() {
        return this.objects;
    }
    public void setObjects(Map<String, ObjectDetails> objects) {
        this.objects = objects;
    }
    public UxProcessor getViewProcessor(){ return this.uxProcessor; }
    public Map<String, Interceptor> interceptors() { return this.interceptors; }
    public Map<String, Pointcut> pointcuts() { return this.pointcuts; }


}
