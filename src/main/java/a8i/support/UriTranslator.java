package a8i.support;

import a8i.A8i;
import com.sun.net.httpserver.HttpExchange;

public class UriTranslator {

    A8i a8i;
    HttpExchange httpExchange;

    public UriTranslator(A8i a8i, HttpExchange httpExchange){
        this.a8i = a8i;
        this.httpExchange = httpExchange;
    }

    public String translate(){
        String uri = httpExchange.getRequestURI().toString();

        if(uri.equals("")) {
            uri = "/";
        }
        if(uri.endsWith("/") &&
                !uri.equals("/")){
            uri = a8i.removeLast(uri);
        }
        return uri;
    }
}
