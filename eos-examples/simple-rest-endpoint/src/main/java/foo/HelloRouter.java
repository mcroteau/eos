package foo;

import com.google.gson.Gson;
import eos.annotate.HttpRouter;
import eos.annotate.Json;
import eos.annotate.Text;
import eos.annotate.verbs.Get;
import eos.model.web.HttpResponse;

@HttpRouter
public class HelloRouter {

    Gson gson = new Gson();

    @Text
    @Get("/")
    public String eos(){
        return "(((  Eos.  )))";
    }

    @Json
    @Get("/json")
    public String json(HttpResponse resp){
        resp.set("message", "Eos.");
        return gson.toJson(resp);
    }

}
