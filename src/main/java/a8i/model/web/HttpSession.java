package a8i.model.web;

import a8i.A8i;
import com.sun.net.httpserver.HttpExchange;

import java.util.HashMap;
import java.util.Map;

public class HttpSession {

    String id;
    HttpExchange httpExchange;
    Map<String, HttpSession> sessions;
    Map<String, Object> attributes;

    public HttpSession(Map<String, HttpSession> sessions, HttpExchange httpExchange){
        this.id = A8i.SESSION_GUID(27);
        this.sessions = sessions;
        this.httpExchange = httpExchange;
        this.attributes = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public boolean set(String key, Object value){
        this.attributes.put(key, value);
        return true;
    }

    public Object get(String key){
        if(this.attributes.containsKey(key)){
            return this.attributes.get(key);
        }
        return "";
    }

    public Map<String, Object> data(){
        return this.attributes;
    }

    public boolean remove(String key){
        this.attributes.remove(key);
        return true;
    }

    public boolean dispose(){
        this.httpExchange.getResponseHeaders().set("Set-Cookie", A8i.SECURITYTAG + "=" + this.id + "; max-age=0" );
        if(this.sessions.containsKey(this.id)){
            this.sessions.remove(this.id);
            return true;
        }
        return false;
    }

}
