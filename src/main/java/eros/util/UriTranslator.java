package eros.util;

import eros.A8i;
import com.sun.net.httpserver.HttpExchange;

public class UriTranslator {

    A8i a8i;
    HttpExchange httpExchange;

    public UriTranslator(A8i a8i, HttpExchange httpExchange){
        this.a8i = a8i;
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
            uri = a8i.removeLast(uri);
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
