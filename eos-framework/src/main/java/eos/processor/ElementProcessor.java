package eos.processor;

import eos.Eos;
import eos.annotate.*;
import eos.model.Element;
import eos.model.ObjectDetails;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElementProcessor {

    Eos.Cache cache;
    Integer jdbcCount;
    Integer serviceCount;
    Integer elementCount;
    List<Class> configs;
    Map<String, ObjectDetails> httpClasses;
    Map<String, ObjectDetails> annotatedClasses;

    public ElementProcessor(Eos.Cache cache){
        this.cache = cache;
        jdbcCount = 0;
        serviceCount = 0;
        elementCount = 0;
        configs = new ArrayList<>();
        httpClasses = new HashMap<>();
        annotatedClasses = new HashMap<>();
    }

    public ElementProcessor run() {
        for (Map.Entry<String, ObjectDetails> entry : cache.getObjects().entrySet()) {
            Class cls = entry.getValue().getClazz();
            if (cls.isAnnotationPresent(Configuration.class)) {
                configs.add(cls);
            }
        }
        for (Map.Entry<String, ObjectDetails> entry : cache.getObjects().entrySet()) {
            Class cls = entry.getValue().getClazz();

            if (cls.isAnnotationPresent(eos.annotate.Element.class)) {
                buildAddElement(entry);
                elementCount++;
            }
            if (cls.isAnnotationPresent(DataStore.class) ||
                    cls.isAnnotationPresent(Repo.class) ||
                        cls.isAnnotationPresent(Persistence.class)) {
                buildAddElement(entry);
                jdbcCount++;
            }
            if (cls.isAnnotationPresent(Service.class)) {
                buildAddElement(entry);
                serviceCount++;
            }
            if (cls.isAnnotationPresent(HttpRouter.class)) {
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
        cache.getElementStorage().getElements().put(key, element);
    }
}
