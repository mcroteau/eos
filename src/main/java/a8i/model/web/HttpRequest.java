package a8i.model.web;

import a8i.A8i;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequest {

    HttpSession httpSession;
    HttpExchange httpExchange;
    Map<String, HttpSession> sessions;
    Map<String, FormElement> elements;
    String requestBody;

    public HttpRequest(Map<String, HttpSession> sessions, HttpExchange httpExchange){
        this.sessions = sessions;
        this.elements = new HashMap<>();
        this.httpExchange = httpExchange;
        this.setSession();
    }

    public Map<String, FormElement> data(){
        return this.elements;
    }

    public Headers getHeaders() {
        return httpExchange.getRequestHeaders();
    }

    public HttpSession getSession() {
        return httpSession;
    }

    public void setSession(){
        String id = A8i.Assets.getCookie(A8i.SECURITYTAG, httpExchange.getRequestHeaders());
        if(this.sessions.containsKey(id)) {
            setSession(this.sessions.get(id));
        }
    }

    public void setSession(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    public HttpSession getSession(boolean newitup){
        String id = A8i.Assets.getCookie(A8i.SECURITYTAG, httpExchange.getRequestHeaders());
        if(!newitup){
            if(this.sessions.containsKey(id)){
                setSession(this.sessions.get(id));
                return this.sessions.get(id);
            }
        }else if(newitup){
            return getHttpSession();
        }
        return null;
    }

    private HttpSession getHttpSession(){
        HttpSession httpSession = new HttpSession(this.sessions, httpExchange);
        this.sessions.put(httpSession.getId(), httpSession);
        String compound = A8i.SECURITYTAG + "=" + httpSession.getId();
        this.httpExchange.getResponseHeaders().set("Set-Cookie", compound);
        setSession(httpSession);
        return httpSession;
    }


    public void set(String key, FormElement formElement){
        this.elements.put(key, formElement);
    }

    /**
     * getValue(String key) is a lookup
     * for a given form field and returns the
     * value for the given FormElement
     *
     * @see FormElement
     *
     * @param key
     * @return returns the value for the given form field
     */
    public String value(String key){
        if(elements.containsKey(key)){
            return elements.get(key).value();
        }
        return null;
    }

    public FormElement get(String key){
        if(elements.containsKey(key)){
            return elements.get(key);
        }
        return null;
    }

    public Object[] getMultiple(String key){
        List<String> values = new ArrayList<>();
        for(Map.Entry<String, FormElement> entry : elements.entrySet()){
            if(key.equals(entry.getKey()) &&
                    entry.getValue().value() != null){
                values.add(entry.getValue().value());
            }
        }
        return values.toArray();
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public byte[] getPayload(String key){
        if(elements.containsKey(key)){
            if(elements.get(key).getFileBytes() != null) {
                return elements.get(key).getFileBytes();
            }
        }
        return null;
    }

    public void setValues(String parameters) {
        String[] keyValues = parameters.split("&");
        for(String keyValue : keyValues){
            String[] parts = keyValue.split("=");
            if(parts.length > 1){
                String key = parts[0];
                String value = parts[1];
                FormElement formElement = new FormElement();
                formElement.setName(key);
                formElement.setValue(value);
                elements.put(key, formElement);
            }
        }
    }
}
