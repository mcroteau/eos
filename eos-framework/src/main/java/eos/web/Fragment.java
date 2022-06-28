package eos.web;

import com.sun.net.httpserver.HttpExchange;
import eos.model.web.HttpRequest;

public interface Fragment {

    String KEY         = "key:attribute";
    Boolean EVALUATION = false;

    public String getKey();

    public Boolean isEvaluation();

    public boolean evaluatesTrue(HttpRequest httpRequest, HttpExchange exchange);

    public String process(HttpRequest httpRequest, HttpExchange exchange);

}
