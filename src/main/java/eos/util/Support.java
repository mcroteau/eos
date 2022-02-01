package eos.util;

import com.sun.net.httpserver.Headers;
import eos.Eos;
import eos.model.web.HttpRequest;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Support {

    boolean isJar;

    public Support(){
        this.isJar = this.isFat();
    }

    public String removeLast(String s) {
        return (s == null || s.length() == 0) ? ""
                : (s.substring(0, s.length() - 1));
    }

    public boolean isJar(){ return this.isJar; }

    public Boolean isFat(){
        String uri = null;
        try {
            uri = getClassesUri();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return uri.contains("jar:file:") ? true : false;
    }

    public String getPayload(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append((char) b);
        }
        return sb.toString();
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

    public Enumeration getJarEntries(){
        JarFile jarFile = getJarFile();
        return jarFile.entries();
    }

    public String getName(String nameWithExt){
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

    public JarFile getJarFile(){
        try {
            URL jarUri = Eos.class.getClassLoader().getResource("eos/");
            String jarPath = jarUri.getPath().substring(5, jarUri.getPath().indexOf("!"));

            return new JarFile(jarPath);
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return null;
    }

    public String getClassesUri() throws Exception {
        String classesUri = Paths.get("src", "main", "java")
                .toAbsolutePath()
                .toString();
        File classesDir = new File(classesUri);
        if(classesDir.exists()){
            return classesUri;
        }

        classesUri = this.getClass().getResource("").toURI().toString();
        if(classesUri == null){
            throw new Exception("A8i : unable to locate class uri");
        }
        return classesUri;
    }

    public Object get(HttpRequest request, Class cls){
        return this.propagate(request, cls);
    }

    public Object propagate(HttpRequest request, Class cls){
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

    public String getProject() {
        if(isJar) {
            JarFile jarFile = getJarFile();
            String path = jarFile.getName();
            String[] bits = path.split("/");
            if(bits.length == 0){
                bits = path.split("\\");
            }
            String namePre = bits[bits.length - 1];
            return namePre.replace(".jar", "");
        }else{
            return "";
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


    public String getCookie(String cookieName, Headers headers){
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

    public static String getResourceUri() throws Exception{
        String resourceUri = Paths.get("src", "main", "resources")
                .toAbsolutePath()
                .toString();
        File resourceDir = new File(resourceUri);
        if(resourceDir.exists()){
            return resourceUri;
        }

        final String RESOURCES_URI = "/src/main/resources/";
        URL indexUri = Eos.class.getResource(RESOURCES_URI);
        if (indexUri == null) {
            throw new FileNotFoundException("A8i : unable to find resource " + RESOURCES_URI);
        }

        return indexUri.toURI().toString();
    }

}
