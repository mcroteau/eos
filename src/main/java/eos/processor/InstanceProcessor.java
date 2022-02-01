package eos.processor;

import eos.Eos;
import eos.annotate.Events;
import eos.model.ObjectDetails;
import eos.util.Support;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class InstanceProcessor {

    Eos.Cache cache;
    Support support;
    ClassLoader cl;
    List<String> jarDeps;
    Map<String, ObjectDetails> objects;

    public InstanceProcessor(Eos.Cache cache){
        this.cache = cache;
        this.support = new Support();
        this.objects = new HashMap<>();
        this.cl = Thread.currentThread().getContextClassLoader();
    }

    public InstanceProcessor run() {
        if (support.isJar()) {
            setJarDeps();
            getClassesJar();
        }else{
            String uri = null;
            try {
                uri = support.getClassesUri();
            } catch (Exception e) {
                e.printStackTrace();
            }
            getClasses(uri);
        }
        return this;
    }


    private List<String> setJarDeps(){
        jarDeps = new ArrayList<>();

        Enumeration<JarEntry> entries = support.getJarEntries();

        do{

            JarEntry jarEntry = entries.nextElement();
            String path = getPath(jarEntry.toString());

            if(!path.contains("META-INF.maven."))continue;

            String dep = path.substring(14);
            jarDeps.add(dep);

        }while(entries.hasMoreElements());


        return jarDeps;
    }

    protected boolean isDep(String jarEntry){
        String jarPath = getPath(jarEntry);
        for(String dep : jarDeps){
            if(jarPath.contains(dep))return true;
        }
        return false;
    }

    //Thank you walen
    private int getLastIndxOf(int nth, String ch, String string) {
        if (nth <= 0) return string.length();
        return getLastIndxOf(--nth, ch, string.substring(0, string.lastIndexOf(ch)));
    }

    protected Boolean isWithinRunningProgram(String jarEntry){
        String main = support.getMain();
        String path = main.substring(0, getLastIndxOf(1, ".", main) + 1);
        String jarPath = getPath(jarEntry);
        return jarPath.contains(path) ? true : false;
    }

    protected Boolean isDirt(String jarEntry){
        if(support.isJar() &&
                !isWithinRunningProgram(jarEntry) &&
                    isDep(jarEntry))return true;

        if(support.isJar() && !jarEntry.endsWith(".class"))return true;
        if(jarEntry.contains("org/h2"))return true;
        if(jarEntry.contains("javax/servlet/http"))return true;
        if(jarEntry.contains("package-info"))return true;
        if(jarEntry.startsWith("module-info"))return true;
        if(jarEntry.contains("META-INF/"))return true;
        if(jarEntry.contains("$"))return true;
        if(jarEntry.endsWith("Exception"))return true;

        return false;
    }

    protected void getClassesJar(){
        try {
            //todo:
            URL jarUriTres = this.cl.getResource("eos/");//was 5
            String jarPath = jarUriTres.getPath().substring(6, jarUriTres.getPath().indexOf("!"));

            JarFile file = new JarFile(jarPath);
            Enumeration jarFile = file.entries();

            while (jarFile.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) jarFile.nextElement();

                if(jarEntry.isDirectory()){
                    continue;
                }

                if(isDirt(jarEntry.toString()))continue;

//                if(jarEntry.toString().contains("javax"))continue;
//                if(jarEntry.toString().contains("org/apache/jasper"))continue;
//                if(jarEntry.toString().contains("org/apache/taglibs"))continue;
//                if(jarEntry.toString().contains("org/apache/tools"))continue;
//                if(jarEntry.toString().contains("org/eclipse/"))continue;
//                if(jarEntry.toString().contains("org/h2"))continue;

                String path = getPath(jarEntry.toString());
                Class cls = this.cl.loadClass(path);

                if (cls.isAnnotation() ||
                        cls.isInterface() ||
                        (cls.getName() == this.getClass().getName())) {
                    continue;
                }

                if(cls.isAnnotationPresent(Events.class)){
                    cache.setEvents(getObject(cls));
                }

                ObjectDetails objectDetails = getObjectDetails(cls);
                cache.getObjects().put(objectDetails.getName(), objectDetails);
            }
        }catch (Exception ex){ex.printStackTrace();}
    }


    protected void getClasses(String uri){
        File pathFile = new File(uri);

        File[] files = pathFile.listFiles();
        for (File file : files) {

            if (file.isDirectory()) {
                getClasses(file.getPath());
                continue;
            }

            try {

                if(isDirt(file.getPath()))continue;

                if(!file.getPath().endsWith(".class") &&
                        !file.getPath().endsWith(".java"))continue;

                String path = getPath("java", "java", file.getPath());
                if(file.toString().endsWith(".class")){
                    path = getPath("class", "classes", file.getPath());
                }

                Class cls = cl.loadClass(path);

                if (cls.isAnnotation() ||
                        cls.isInterface() ||
                        (cls.getName() == this.getClass().getName())) {
                    continue;
                }

                if(cls.isAnnotationPresent(Events.class)){
                    cache.setEvents(getObject(cls));
                }

                ObjectDetails objectDetails = getObjectDetails(cls);
                cache.getObjects().put(objectDetails.getName(), objectDetails);

            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    protected String getPath(String path){
        if(path.startsWith("/"))path = path.replaceFirst("/", "");
        return path
                .replace("\\", ".")
                .replace("/",".")
                .replace(".class", "");
    }

    protected String getPath(String name, String key, String path){
        String separator = System.getProperty("file.separator");
        String regex = key + "\\" + separator;
        String[] pathParts = path.split(regex);
        return pathParts[1]
                .replace("\\", ".")
                .replace("/",".")
                .replace("."+ name, "");
    }

    protected ObjectDetails getObjectDetails(Class cls) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        ObjectDetails objectDetails = new ObjectDetails();
        objectDetails.setClazz(cls);
        objectDetails.setName(support.getName(cls.getName()));
        Object object = getObject(cls);
        objectDetails.setObject(object);
        return objectDetails;
    }

    protected Object getObject(Class cls) {
        Object object = null;
        try {
            object = cls.getConstructor().newInstance();
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        } catch (NoSuchMethodException e) {
        }
//        Constructor constructor = null;
//        Constructor[] constructors = cls.getDeclaredConstructors();
//        for(Constructor activeConstructor : constructors){
//            if(activeConstructor.getParameterCount() == 0){
//                activeConstructor;
//                break;
//            }
//        }
//        constructor.setAccessible(true);
//        try {
//            object = constructor.newInstance();
//        } catch (InstantiationException e) {
//        } catch (IllegalAccessException e) {
//        } catch (InvocationTargetException e) {
//        }
        return object;
    }

}
