package eos.processor;

import eos.Eos;
import eos.annotate.Variable;
import eos.annotate.verbs.Delete;
import eos.annotate.verbs.Get;
import eos.annotate.verbs.Post;
import eos.annotate.verbs.Put;
import eos.model.ObjectDetails;
import eos.model.web.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EndpointProcessor {

    public static final String GET    = "Get";
    public static final String POST   = "Post";
    public static final String PUT    = "Put";
    public static final String DELETE = "Delete";

    Eos.Cache cache;

    Map<String, ObjectDetails> processed;
    EndpointMappings endpointMappings;

    public EndpointProcessor(Eos.Cache cache){
        this.cache = cache;
        this.processed = new HashMap<>();
        this.endpointMappings = new EndpointMappings();
    }

    public EndpointProcessor run() throws Exception{
        while(!allAnnotationsProcessed()){
            processWebAnnotations();
        }
        return this;
    }

    private boolean allAnnotationsProcessed(){
        return this.processed.size() == cache.getElementProcessor().getHttpClasses().size();
    }

    private void processWebAnnotations() throws Exception{
        for(Map.Entry<String, ObjectDetails> entry : cache.getElementProcessor().getHttpClasses().entrySet()){
            Class clazz = entry.getValue().getClazz();
            Method[] methods = clazz.getDeclaredMethods();
            for(Method method: methods){

                if(method.isAnnotationPresent(Get.class)){
                    setGetMapping(method, entry.getValue());
                    processed.put(entry.getKey(), entry.getValue());
                }
                if(method.isAnnotationPresent(Post.class)){
                    setPostMapping(method, entry.getValue());
                    processed.put(entry.getKey(), entry.getValue());
                }
                if(method.isAnnotationPresent(Put.class)){
                    setPutMapping(method, entry.getValue());
                    processed.put(entry.getKey(), entry.getValue());
                }
                if(method.isAnnotationPresent(Delete.class)){
                    setDeleteMapping(method, entry.getValue());
                    processed.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    protected void setGetMapping(Method method, ObjectDetails objectDetails) throws Exception{
        Get get = method.getAnnotation(Get.class);
        String path = get.value();
        EndpointMapping mapping = new EndpointMapping();
        mapping.setVerb(GET);
        setBaseDetailsAdd(path, mapping, method, objectDetails);
    }

    protected void setPostMapping(Method method, ObjectDetails objectDetails) throws Exception{
        Post post = method.getAnnotation(Post.class);
        String path = post.value();
        EndpointMapping mapping = new EndpointMapping();
        mapping.setVerb(POST);
        setBaseDetailsAdd(path, mapping, method, objectDetails);
    }

    protected void setPutMapping(Method method, ObjectDetails objectDetails) throws Exception{
        Put put = method.getAnnotation(Put.class);
        String path = put.value();
        EndpointMapping mapping = new EndpointMapping();
        mapping.setVerb(PUT);
        setBaseDetailsAdd(path, mapping, method, objectDetails);
    }

    protected void setDeleteMapping(Method method, ObjectDetails objectDetails) throws Exception{
        Delete delete = method.getAnnotation(Delete.class);
        String path = delete.value();
        EndpointMapping mapping = new EndpointMapping();
        mapping.setVerb(DELETE);
        setBaseDetailsAdd(path, mapping, method, objectDetails);
    }

    protected void setBaseDetailsAdd(String path, EndpointMapping mapping, Method method, ObjectDetails objectDetails) throws Exception{

        mapping.setTypeNames(new ArrayList<>());
        Type[] types = method.getGenericParameterTypes();
        for(Type type : types){
            mapping.getTypeNames().add(type.getTypeName());
        }

        List<TypeFeature> typeDetails = new ArrayList<>();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        Class[] paramTypes = method.getParameterTypes();
        for (int n = 0; n < paramAnnotations.length; n++) {
            for (Annotation a: paramAnnotations[n]) {
                if (a instanceof Variable) {
                    TypeFeature details = new TypeFeature();
                    details.setName(paramTypes[n].getTypeName());
                    details.setType(paramTypes[n].getTypeName());
                    typeDetails.add(details);
                }
            }
        }

//https://regex101.com/r/sYeDyN/1
//\/(post){1}\/[A-Za-z0-9]*\/(paul){1}$
//\/(get){1}\/[A-Za-z0-9]\/[A-Za-z0-9]\/[A-Za-z0-9]\/$

        StringBuilder regexPath = new StringBuilder();
        regexPath.append("\\/(");
        int count = 0;
        String[] parts = path.split("/");
        for(String part: parts){
            count++;
            if(!part.equals("")) {
                if (part.matches("(\\{\\{[a-zA-Z]*\\}\\})")) {
                    regexPath.append("(.*[A-Za-z0-9])");
                    mapping.getVariablePositions().add(count - 1);
                } else {
                    regexPath.append("(" + part.toLowerCase() + "){1}");
                }
                if (count < parts.length) {
                    regexPath.append("\\/");
                }
            }
        }
        regexPath.append(")$");

        mapping.setRegexedPath(regexPath.toString());
        mapping.setTypeDetails(typeDetails);
        mapping.setPath(path);
        mapping.setMethod(method);
        mapping.setClassDetails(objectDetails);

        String key = mapping.getVerb().concat("-").concat(path);
        if(endpointMappings.contains(key)){
            throw new Exception("Request path + " + path + " exists multiple times.");
        }

        String[] bits = path.split("/");
        UrlBitFeatures urlBitFeatures = new UrlBitFeatures();
        List<UrlBit> urlBits = new ArrayList<>();
        for(String bit : bits){
            UrlBit urlBit = new UrlBit();
            if(bit.contains("{{") && bit.contains("}}")){
                urlBit.setVariable(true);
            }else{
                urlBit.setVariable(false);
            }
            urlBits.add(urlBit);
        }
        urlBitFeatures.setUrlBits(urlBits);
        mapping.setUrlBitFeatures(urlBitFeatures);

        endpointMappings.add(key, mapping);

    }


    public EndpointMappings getMappings() {
        return endpointMappings;
    }
}
