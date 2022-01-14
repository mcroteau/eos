package a8i.web;

import a8i.model.web.HttpRequest;
import com.sun.net.httpserver.HttpExchange;

public interface Pointcut {

    final String KEY         = "key:attribute";

    final Boolean EVALUATION = false;
    public String getKey();

    public Boolean isEvaluation();

    public boolean isTrue(HttpRequest httpRequest, HttpExchange exchange);

    public String halloween(HttpRequest httpRequest, HttpExchange exchange);

}
