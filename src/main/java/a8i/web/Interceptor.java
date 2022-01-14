package a8i.web;

import a8i.model.web.HttpRequest;
import com.sun.net.httpserver.HttpExchange;

public interface Interceptor {
    public void intercept(HttpRequest request, HttpExchange httpExchange);
}
