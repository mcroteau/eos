package eos.fragment;

import eos.model.web.HttpRequest;
import eos.web.Fragment;
import com.sun.net.httpserver.HttpExchange;
import eos.Security;

public class IdentityFragment implements Fragment {

    final String KEY         = "sec:id";
    final Boolean EVALUATION = false;

    public boolean evaluatesTrue(HttpRequest httpRequest, HttpExchange exchange){
        return true;
    }

    public String process(HttpRequest httpRequest, HttpExchange exchange){
        return Security.get("userId");
    }

    public String getKey() {
        return this.KEY;
    }

    public Boolean isEvaluation() {
        return this.EVALUATION;
    }
}
