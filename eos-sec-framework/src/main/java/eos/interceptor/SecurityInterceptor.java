package eos.interceptor;

import eos.model.web.HttpRequest;
import eos.web.Interceptor;
import com.sun.net.httpserver.HttpExchange;
import eos.Security;

public class SecurityInterceptor implements Interceptor {
    @Override
    public void intercept(HttpRequest request, HttpExchange httpExchange) {
        Security.SAVE(request);
        Security.SAVE(httpExchange);
    }
}
