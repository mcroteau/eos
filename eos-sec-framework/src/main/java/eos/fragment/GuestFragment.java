package eos.fragment;

import eos.Security;
import eos.model.web.HttpRequest;
import eos.web.Fragment;
import com.sun.net.httpserver.HttpExchange;

public class GuestFragment implements Fragment {

    final String KEY         = "sec:guest";
    final Boolean EVALUATION = true;

    public boolean evaluatesTrue(HttpRequest httpRequest, HttpExchange exchange){
        return !Security.isAuthenticated();
    }

    public String process(HttpRequest httpRequest, HttpExchange exchange){
        return "";
    }

    public String getKey() {
        return this.KEY;
    }

    public Boolean isEvaluation() {
        return this.EVALUATION;
    }
}
