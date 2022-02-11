package eos.web;

import com.sun.net.httpserver.HttpExchange;
import eos.model.web.HttpRequest;

public interface Interceptor {
    public void intercept(HttpRequest request, HttpExchange httpExchange);
}
