package eros.util;

import eros.A8i;
import com.sun.net.httpserver.HttpExchange;

public class UriTranslator {

    Support support;
    HttpExchange httpExchange;

    public UriTranslator(Support support, HttpExchange httpExchange){
        this.support = support;
        this.httpExchange = httpExchange;
    }

    public String translate(){
        String uriPre = httpExchange.getRequestURI().toString();
        String[] parts = uriPre.split("\\?");
        String uri = parts[0];

        if(uri.equals("")) {
            uri = "/";
        }
        if(uri.endsWith("/") &&
                !uri.equals("/")){
            uri = support.removeLast(uri);
        }
        return uri;
    }

    public String getParameters() {
        String uriPre = httpExchange.getRequestURI().toString();
        String[] parts = uriPre.split("\\?");
        if(parts.length > 1){
            return parts[1];
        }
        return "";
    }
}
