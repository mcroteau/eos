package a8i.support;

import a8i.A8i;
import com.sun.net.httpserver.HttpExchange;

public class UriTranslator {

    HttpExchange httpExchange;

    public UriTranslator(HttpExchange httpExchange){
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
            uri = A8i.Util.removeLast(uri);
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
