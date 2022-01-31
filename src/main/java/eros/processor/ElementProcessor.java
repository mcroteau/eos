package eros.processor;

import eros.A8i;
import eros.annotate.*;
import eros.model.Element;
import eros.model.ObjectDetails;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElementProcessor {

    A8i a8i;
    Integer jdbcCount;
    Integer serviceCount;
    Integer elementCount;
    List<Class> configs;
    Map<String, ObjectDetails> httpClasses;
    Map<String, ObjectDetails> annotatedClasses;

    public ElementProcessor(A8i a8i){
        this.a8i = a8i;
        jdbcCount = 0;
        serviceCount = 0;
        elementCount = 0;
        configs = new ArrayList<>();
        httpClasses = new HashMap<>();
        annotatedClasses = new HashMap<>();
    }

    public ElementProcessor run() {
        for (Map.Entry<String, ObjectDetails> entry : a8i.getObjects().entrySet()) {
            Class cls = entry.getValue().getClazz();
            if (cls.isAnnotationPresent(Config.class)) {
                configs.add(cls);
            }
        }
        for (Map.Entry<String, ObjectDetails> entry : a8i.getObjects().entrySet()) {
            Class cls = entry.getValue().getClazz();

            if (cls.isAnnotationPresent(eros.annotate.Element.class)) {
                buildAddElement(entry);
                elementCount++;
            }
            if (cls.isAnnotationPresent(DataStore.class)) {
                buildAddElement(entry);
                jdbcCount++;
            }
            if (cls.isAnnotationPresent(Service.class)) {
                buildAddElement(entry);
                serviceCount++;
            }
            if (cls.isAnnotationPresent(HttpHandler.class)) {
                httpClasses.put(entry.getKey(), entry.getValue());
            }

            Field[] fields = cls.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Bind.class)) {
                    annotatedClasses.put(entry.getKey(), entry.getValue());
                }
                if (field.isAnnotationPresent(Property.class)) {
                    annotatedClasses.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return this;
    }

    public Map<String, ObjectDetails> getHttpClasses(){
        return this.httpClasses;
    }

    public Map<String, ObjectDetails> getAnnotatedClasses(){
        return this.annotatedClasses;
    }

    public List<Class> getConfigs(){
        return this.configs;
    }

    protected void buildAddElement(Map.Entry<String, ObjectDetails> entry){
        Element element = new Element();
        String key = entry.getKey();
        Object object = entry.getValue().getObject();
        element.setElement(object);
        a8i.getElementStorage().getElements().put(key, element);
    }
}
