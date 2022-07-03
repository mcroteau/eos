package eos.web;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import eos.Eos;
import eos.annotate.*;
import eos.model.web.*;
import eos.util.MimeGetter;
import eos.util.ResourceResponse;
import eos.util.Support;
import eos.util.UriTranslator;

import java.io.*;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpTransmission implements HttpHandler {

    public static final String REDIRECT = "[redirect]";

    Eos.Cache cache;
    Support support;
    Map<String, HttpSession> sessions;

    public HttpTransmission(Eos.Cache cache){
        this.cache = cache;
        this.support = new Support();
        this.sessions = new ConcurrentHashMap<>();
    }

    @Override
    public void handle(HttpExchange httpExchange) {

        OutputStream outputStream = httpExchange.getResponseBody();

        try {

            InputStream is = httpExchange.getRequestBody();
            byte[] payloadBytes = support.getPayloadBytes(is);

            ElementCompiler requestCompiler = new ElementCompiler(cache, payloadBytes, sessions, httpExchange);
            HttpRequest httpRequest = requestCompiler.compile();
            String payload = support.getPayload(payloadBytes);
            httpRequest.setRequestBody(payload);


            Map<String, Interceptor> interceptors = cache.getInterceptors();
            for(Map.Entry<String, Interceptor> entry: interceptors.entrySet()){
                Interceptor interceptor = entry.getValue();
                interceptor.intercept(httpRequest, httpExchange);
            }

            UriTranslator transformer = new UriTranslator(support, httpExchange);
            String requestUri = transformer.translate();
            httpRequest.setValues(transformer.getParameters());

            String httpVerb = httpExchange.getRequestMethod().toLowerCase();
            if(ResourceResponse.isResource(requestUri, cache)){
                new ResourceResponse.Builder()
                        .withCache(cache)
                        .withRequestUri(requestUri)
                        .withHttpVerb(httpVerb)
                        .withHttpExchange(httpExchange)
                        .make()
                        .serve();
                return;
            }

            HttpResponse httpResponse = getHttpResponse(httpExchange);

            if(httpRequest.getSession() != null){
                HttpSession httpSession = httpRequest.getSession();
                Map<String, String> session = new HashMap<>();
                for(Map.Entry<String, Object> entry: httpSession.data().entrySet()){
                    String key = entry.getKey();
                    String value = String.valueOf(entry.getValue());
                    httpResponse.set(key, value);
                }
            }


            EndpointMapping endpointMapping = getHttpMapping(httpVerb, requestUri);
            if(endpointMapping == null){
                try {
                    String message = "404 not found.";
                    httpExchange.sendResponseHeaders(200, message.length());
                    outputStream.write(message.getBytes());
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }


            Object[] signature = getEndpointParameters(requestUri, httpRequest, httpResponse, endpointMapping, httpExchange);

            Method method = endpointMapping.getMethod();
            method.setAccessible(true);


            String design = null;
            if(method.isAnnotationPresent(Design.class)){
                Design annotationDos = method.getAnnotation(Design.class);
                design = annotationDos.value();
            }

            Object object = endpointMapping.getClassDetails().getObject();

            String methodResponse = (String) method.invoke(object, signature);
            if (methodResponse == null)throw new Exception("something went wrong when calling " + method);

            if(method.isAnnotationPresent(Basic.class) ||
                    method.isAnnotationPresent(Text.class) ||
                        method.isAnnotationPresent(Plain.class)) {
                Headers headers = httpExchange.getResponseHeaders();
                headers.add("content-type", "text/html");
                httpExchange.sendResponseHeaders(200, methodResponse.length());
                outputStream.write(methodResponse.getBytes());
            }else if(method.isAnnotationPresent(Json.class)){
                Headers headers = httpExchange.getResponseHeaders();
                headers.add("content-type", "application/json");
                httpExchange.sendResponseHeaders(200, methodResponse.length());
                outputStream.write(methodResponse.getBytes());
            }else if(method.isAnnotationPresent(Media.class)){
                MimeGetter mimeGetter = new MimeGetter(requestUri);
                Headers headers = httpExchange.getResponseHeaders();
                headers.add("content-type", mimeGetter.resolve());
                outputStream.write(methodResponse.getBytes());
            }else if(methodResponse.startsWith(this.REDIRECT)){
                httpExchange.setAttribute("message", httpResponse.get("message"));
                String redirect = getRedirect(methodResponse);
                Headers headers = httpExchange.getResponseHeaders();
                headers.add("Location", redirect);
                httpExchange.sendResponseHeaders(302, -1);
                httpExchange.close();
                return;
            }else{

                String title = httpResponse.getTitle();
                String keywords = httpResponse.getKeywords();
                String description = httpResponse.getDescription();

                if(!support.isJar()) {

                    Path webPath = Paths.get("web-ux");
                    if(methodResponse.startsWith("/")){
                        methodResponse = methodResponse.replaceFirst("/", "");
                    }
                    String htmlPath = webPath.toFile().getAbsolutePath().concat(File.separator + methodResponse);
                    File viewFile = new File(htmlPath);

                    if(!viewFile.exists()) {
                        try {
                            String message = "view " + htmlPath + " cannot be found.";
                            httpExchange.sendResponseHeaders(200, message.length());
                            outputStream.write(message.getBytes());
                            outputStream.flush();
                            outputStream.close();
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    InputStream fis = new FileInputStream(viewFile);
                    ByteArrayOutputStream unebaos = new ByteArrayOutputStream();
                    byte[] bytes = new byte[1024 * 13];
                    int unelength;
                    while ((unelength = fis.read(bytes)) != -1) {
                        unebaos.write(bytes, 0, unelength);
                    }

                    String pageContent = unebaos.toString(StandardCharsets.UTF_8.name());

                    if(design != null) {

                        String designPath = webPath.toFile().getAbsolutePath().concat(File.separator + design);
                        File designFile = new File(designPath);

                        if (!designFile.exists()) {
                            try {
                                String message = "design " + designPath + " cannot be found.";
                                httpExchange.sendResponseHeaders(200, message.length());
                                outputStream.write(message.getBytes());
                                outputStream.flush();
                                outputStream.close();
                                return;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                        InputStream dis = new FileInputStream(designFile);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        int deuxlength;
                        while ((deuxlength = dis.read(bytes)) != -1) {
                            baos.write(bytes, 0, deuxlength);
                        }

                        String designContent = baos.toString(StandardCharsets.UTF_8.name());

                        if(!designContent.contains("<eos:content/>")){
                            try {
                                String message = "Your html template file is missing the <a:content/> tag";
                                httpExchange.sendResponseHeaders(200, message.length());
                                outputStream.write(message.getBytes());
                                outputStream.flush();
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        String[] bits = designContent.split("<eos:content/>");
                        String header = bits[0];
                        String bottom = bits[1];

                        header = header.concat(pageContent);
                        String completePage = header.concat(bottom);
                        completePage = completePage.replace("${title}", title);

                        if(keywords != null) {
                            completePage = completePage.replace("${keywords}", keywords);
                        }
                        if(description != null){
                            completePage = completePage.replace("${description}", description);
                        }

                        String designOutput = "";
                        try{

                            ExperienceProcessor experienceProcessor = cache.getUxProcessor();
                            Map<String, Fragment> pointcuts = cache.getPointCuts();
                            designOutput = experienceProcessor.execute(pointcuts, completePage, httpResponse, httpRequest, httpExchange);


                        }catch(Exception ex){
                            ex.printStackTrace();
                            try {
                                String message = "Please check your html template file. " + ex.getMessage();
                                httpExchange.sendResponseHeaders(200, message.length());
                                outputStream.write(message.getBytes());
                                outputStream.flush();
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        byte[] bs = designOutput.getBytes("utf-8");
                        httpExchange.sendResponseHeaders(200, bs.length);
                        outputStream.write(bs);

                    }else {

                        String pageOutput = "";

                        try{

                            ExperienceProcessor experienceProcessor = cache.getUxProcessor();
                            Map<String, Fragment> pointcuts = cache.getPointCuts();
                            pageOutput = experienceProcessor.execute(pointcuts, pageContent, httpResponse, httpRequest, httpExchange);

                            if(!pageOutput.startsWith("<html>")){
                                pageOutput = "<html>" + pageOutput;
                                pageOutput = pageOutput + "</html>";
                            }

                            httpExchange.sendResponseHeaders(200, pageOutput.length());
                            outputStream.write(pageOutput.getBytes());

                        }catch(Exception ex){
                            ex.printStackTrace();
                            try {
                                String message = "Please check your html template file. " + ex.getMessage();
                                httpExchange.sendResponseHeaders(200, message.length());
                                outputStream.write(message.getBytes());
                                outputStream.flush();
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        //todo:cleanup
//                        for(Map.Entry<String, Interceptor> entry: interceptors.entrySet()){
//                            Interceptor security.interceptor = entry.getValue();
//                            security.interceptor.post(httpRequest, httpExchange);
//                        }

                    }
                }else{

                    if(methodResponse.startsWith("/"))methodResponse = methodResponse.replaceFirst("/","");
                    String pagePath = "/web-ux/" + methodResponse;

                    InputStream pageInput = this.getClass().getResourceAsStream(pagePath);

                    ByteArrayOutputStream unebaos = new ByteArrayOutputStream();
                    byte[] bytes = new byte[1024 * 13];
                    int unelength;
                    while ((unelength = pageInput.read(bytes)) != -1) {
                        unebaos.write(bytes, 0, unelength);
                    }

                    String pageContent = unebaos.toString(StandardCharsets.UTF_8.name());


                    if(design != null) {
                        String designPath = "/web-ux/" + design;
                        InputStream designInput = this.getClass().getResourceAsStream(designPath);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        int length;
                        while ((length = designInput.read(bytes)) != -1) {
                            baos.write(bytes, 0, length);
                        }

                        String designContent = baos.toString(StandardCharsets.UTF_8.name());
                        if(!designContent.contains("<eos:content/>")){
                            try {
                                String message = "Your html template file is missing the <a:content/> tag";
                                httpExchange.sendResponseHeaders(200, message.length());
                                outputStream.write(message.getBytes());
                                outputStream.flush();
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        String[] bits = designContent.split("<eos:content/>");
                        String header = bits[0];
                        String bottom = bits[1];

                        header = header.concat(pageContent);
                        String completePage = header.concat(bottom);
                        completePage = completePage.replace("${title}", title);

                        if(keywords != null) {
                            completePage = completePage.replace("${keywords}", keywords);
                        }
                        if(description != null){
                            completePage = completePage.replace("${description}", description);
                        }

                        String designOutput = "";
                        try{

                            ExperienceProcessor experienceProcessor = cache.getUxProcessor();
                            Map<String, Fragment> pointcuts = cache.getPointCuts();
                            designOutput = experienceProcessor.execute(pointcuts, completePage, httpResponse, httpRequest, httpExchange);


                        }catch(Exception ex){
                            ex.printStackTrace();
                            try {
                                String message = "Please check your html template file. " + ex.getMessage();
                                httpExchange.sendResponseHeaders(200, message.length());
                                outputStream.write(message.getBytes());
                                outputStream.flush();
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        byte[] bs = designOutput.getBytes("utf-8");
                        httpExchange.sendResponseHeaders(200, bs.length);
                        outputStream.write(bs);

                    }else{

                        String pageOutput = "";

                        try{

                            ExperienceProcessor experienceProcessor = cache.getUxProcessor();
                            Map<String, Fragment> pointcuts = cache.getPointCuts();
                            pageOutput = experienceProcessor.execute(pointcuts, pageContent, httpResponse, httpRequest, httpExchange);

                            if(!pageOutput.startsWith("<html>")){
                                pageOutput = "<html>" + pageOutput;
                                pageOutput = pageOutput + "</html>";
                            }

                            httpExchange.sendResponseHeaders(200, pageOutput.length());
                            outputStream.write(pageOutput.getBytes());

                        }catch(Exception ex){
                            ex.printStackTrace();
                            try {
                                String message = "Please check your html template file. " + ex.getMessage();
                                httpExchange.sendResponseHeaders(200, message.length());
                                outputStream.write(message.getBytes());
                                outputStream.flush();
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            //todo: cleanup
//            for(Map.Entry<String, Interceptor> entry: interceptors.entrySet()){
//                Interceptor security.interceptor = entry.getValue();
//                security.interceptor.post(httpRequest, httpExchange);
//            }

            outputStream.flush();
            outputStream.close();

        }catch(ClassCastException ccex){
            System.out.println("Attempted to cast an object at the data layer with an incorrect Class type.");
            ccex.printStackTrace();
        }catch (Exception ex){
            ex.printStackTrace();
            try {
                String message = "Sorry, we must have forgot an ampersand. Let us know so we can track it down";
                httpExchange.sendResponseHeaders(200, message.length());
                outputStream.write(message.getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected String getRedirect(String uri){
        String[] redirectBits = uri.split("]");
        if(redirectBits.length > 1){
            return redirectBits[1];
        }
        return "";
    }

    private Object[] getEndpointParameters(String requestUri,
                                           HttpRequest httpRequest,
                                           HttpResponse httpResponse,
                                           EndpointMapping endpointMapping,
                                           HttpExchange httpExchange){

        List<EndpointPosition> endpointValues = getEndpointValues(requestUri, endpointMapping);
        List<Object> params = new ArrayList<>();
        List<String> typeNames = endpointMapping.getTypeNames();
        int idx = 0;
        for(int z = 0; z <  typeNames.size(); z++){
            String type = typeNames.get(z);
            if(type.equals("com.sun.net.httpserver.HttpExchange")){
                params.add(httpExchange);
            }
            if(type.equals("eos.model.web.HttpRequest")){
                params.add(httpRequest);
            }
            if(type.equals("eos.model.web.HttpResponse")){
                params.add(httpResponse);
            }
            if(type.equals("java.lang.Integer")){
                params.add(Integer.valueOf(endpointValues.get(idx).getValue()));
                idx++;
            }
            if(type.equals("java.lang.Long")){
                params.add(Long.valueOf(endpointValues.get(idx).getValue()));
                idx++;
            }
            if(type.equals("java.math.BigDecimal")){
                params.add(new BigDecimal(endpointValues.get(idx).getValue()));
                idx++;
            }
            if(type.equals("java.lang.String")){
                params.add(endpointValues.get(idx).getValue());
                idx++;
            }
        }

        return params.toArray();
    }


    protected List<EndpointPosition> getEndpointValues(String uri, EndpointMapping mapping){
        List<String> pathParts = getPathParts(uri);
        List<String> regexParts = getRegexParts(mapping);

        List<EndpointPosition> httpValues = new ArrayList<>();
        for(int n = 0; n < regexParts.size(); n++){
            String regex = regexParts.get(n);
            if(regex.contains("A-Za-z0-9")){
                httpValues.add(new EndpointPosition(n, pathParts.get(n)));
            }
        }
        return httpValues;
    }

    protected List<String> getPathParts(String uri){
        return Arrays.asList(uri.split("/"));
    }

    protected List<String> getRegexParts(EndpointMapping mapping){
        return Arrays.asList(mapping.getRegexedPath().split("/"));
    }

    protected EndpointMapping getHttpMapping(String verb, String uri){

        for (Map.Entry<String, EndpointMapping> mappingEntry : cache.getEndpointMappings().getMappings().entrySet()) {
            EndpointMapping mapping = mappingEntry.getValue();

            String mappingUri = mapping.getPath();
            if(!mapping.getPath().startsWith("/")){
                mappingUri = "/" + mappingUri;
            }

            if(mappingUri.equals(uri)){
                return mapping;
            }
        }

        for (Map.Entry<String, EndpointMapping> mappingEntry : cache.getEndpointMappings().getMappings().entrySet()) {
            EndpointMapping mapping = mappingEntry.getValue();
            Matcher matcher = Pattern.compile(mapping.getRegexedPath())
                    .matcher(uri);

            String mappingUri = mapping.getPath();
            if(!mapping.getPath().startsWith("/")){
                mappingUri = "/" + mappingUri;
            }

            if(matcher.matches() &&
                    mapping.getVerb().toLowerCase().equals(verb) &&
                    variablesMatchUp(uri, mapping) &&
                    lengthMatches(uri, mappingUri)){
                return mapping;
            }
        }

        return null;
    }

    protected boolean lengthMatches(String uri, String mappingUri){
        String[] uriBits = uri.split("/");
        String[] mappingBits = mappingUri.split("/");
        return uriBits.length == mappingBits.length;
    }

    protected boolean variablesMatchUp(String uri, EndpointMapping endpointMapping){
        List<String> bits = Arrays.asList(uri.split("/"));

        UrlBitFeatures urlBitFeatures = endpointMapping.getUrlBitFeatures();
        List<UrlBit> urlBits = urlBitFeatures.getUrlBits();

        Class[] typeParameters = endpointMapping.getMethod().getParameterTypes();
        List<String> parameterTypes = getParameterTypes(typeParameters);

        int idx = 0;
        for(int q = 0; q < urlBits.size(); q++){
            UrlBit urlBit = urlBits.get(q);
            if(urlBit.isVariable()){

                try {
                    String methodType = parameterTypes.get(idx);
                    String bit = bits.get(q);
                    if (!bit.equals("")) {


                        if (methodType.equals("java.lang.Integer")) {
                            Integer.parseInt(bit);
                        }
                        if(methodType.equals("java.lang.Long")){
                            Long.parseLong(bit);
                        }
                    }

                    idx++;

                }catch(Exception ex){
                    return false;
                }
            }
        }
        return true;
    }

    public List<String> getParameterTypes(Class[] clsParamaters){
        List<String> parameterTypes = new ArrayList<>();
        for(Class cls : clsParamaters){
            String type = cls.getTypeName();
            if(!type.contains("HttpExchange") &&
                    !type.contains("HttpRequest") &&
                    !type.contains("HttpResponse")){
                parameterTypes.add(type);
            }
        }
        return parameterTypes;
    }

    protected HttpResponse getHttpResponse(HttpExchange exchange){
        HttpResponse httpResponse = new HttpResponse();
        if(exchange.getAttribute("message") != null){
            httpResponse.set("message", exchange.getAttribute("message"));
            exchange.setAttribute("message", "");
        }
        return httpResponse;
    }

}
