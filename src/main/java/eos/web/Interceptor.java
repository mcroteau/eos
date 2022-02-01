package eos.web;

import eos.model.web.HttpRequest;
import com.sun.net.httpserver.HttpExchange;

public interface Interceptor {
    public void intercept(HttpRequest request, HttpExchange httpExchange);
}
