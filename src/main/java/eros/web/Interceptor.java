package eros.web;

import eros.model.web.HttpRequest;
import com.sun.net.httpserver.HttpExchange;

public interface Interceptor {
    public void intercept(HttpRequest request, HttpExchange httpExchange);
}
