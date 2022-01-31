package eros;

import eros.jdbc.Repo;
import eros.model.Element;
import eros.model.ObjectDetails;
import eros.model.web.EndpointMappings;
import eros.model.web.HttpRequest;
import eros.processor.ElementProcessor;
import eros.processor.EndpointProcessor;
import eros.processor.UxProcessor;
import eros.storage.ElementStorage;
import eros.storage.PropertyStorage;
import eros.support.ExchangeStartup;
import eros.support.Initializer;
import eros.web.HttpTransmission;
import eros.web.Interceptor;
import eros.web.Pointcut;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;

import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class A8i {

    public static final String A8i         = "eros";
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
                .with(this, injector.repo)
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

                    if (type.getTypeName().equals("int") ||
                            type.getTypeName().equals("java.lang.Integer")) {
                        field.set(object, Integer.valueOf(value));
                    }
                    if (type.getTypeName().equals("double") ||
                            type.getTypeName().equals("java.lang.Double")) {
                        field.set(object, Double.valueOf(value));
                    }
                    if (type.getTypeName().equals("float") ||
                            type.getTypeName().equals("java.lang.Float")) {
                        field.set(object, Float.valueOf(value));
                    }
                    if (type.getTypeName().equals("long") ||
                            type.getTypeName().equals("java.lang.Long")) {
                        field.set(object, Long.valueOf(value));
                    }
                    if (type.getTypeName().equals("boolean") ||
                            type.getTypeName().equals("java.lang.Boolean")) {
                        field.set(object, Boolean.valueOf(value));
                    }
                    if (type.getTypeName().equals("java.math.BigDecimal")) {
                        field.set(object, new BigDecimal(value));
                    }
                    if (type.getTypeName().equals("java.lang.String")) {
                        field.set(object, value);
                    }
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return object;
    }



    public String removeLast(String s) {
        return (s == null || s.length() == 0)
                ? ""
                : (s.substring(0, s.length() - 1));
    }


    public static class Injector{

        Repo repo;
        Boolean noAction;
        Boolean createDb;
        Boolean dropDb;
        List<String> resources;
        List<String> propertyFiles;
        String contextPath;
        UxProcessor uxProcessor;

        Map<String, Pointcut> pointcuts;
        Map<String, Interceptor> interceptors;

        public Injector withRepo(Repo repo){
            this.repo = repo;
            return this;
        }
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
            URL jarUri = A8i.class.getClassLoader().getResource("eros/");
            String jarPath = jarUri.getPath().substring(5, jarUri.getPath().indexOf("!"));

            return new JarFile(jarPath);
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return null;
    }


    public static Enumeration getEntries(){
        try {
            URL jarUriTres = A8i.class.getClassLoader().getResource("eros/");
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
            builder.append(scanner.nextLine() + "\n");
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
