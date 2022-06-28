package eos.fragment;

import eos.model.web.HttpRequest;
import eos.web.Fragment;
import com.sun.net.httpserver.HttpExchange;
import eos.Security;

public class UserFragment implements Fragment {

    String KEY         = "sec:user";
    Boolean EVALUATION = false;

    public boolean evaluatesTrue(HttpRequest httpRequest, HttpExchange exchange){ return true; }

    public String process(HttpRequest httpRequest, HttpExchange exchange){
        return Security.getUser();
    }

    public String getKey() {
        return this.KEY;
    }

    public Boolean isEvaluation() {
        return this.EVALUATION;
    }
}