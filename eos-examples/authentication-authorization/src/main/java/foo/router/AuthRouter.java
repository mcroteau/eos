package foo.router;

import eos.Security;
import eos.annotate.HttpRouter;
import eos.annotate.verbs.Get;
import eos.annotate.verbs.Post;
import eos.model.web.HttpRequest;
import eos.model.web.HttpResponse;

@HttpRouter
class AuthRouter {

    @Get("/")
    public String signin() {
        return "/pages/signin.jsa";
    }

    @Post("/authenticate")
    public String authenticate(HttpRequest req) {
        String user = req.get("user").value();
        String pass = req.get("pass").value();

        if(Security.signin(user, pass)){
            return "[redirect]/secret";
        }

        return "[redirect]/";
    }

    @Get("/secret")
    public String secret(HttpResponse resp) {
        if(Security.isAuthenticated()){
            return "/pages/passed.jsa";
        }
        resp.set("message", "authenticate pour favor.");
        return "[redirect]/";
    }

    @Get("/signout")
    public String signout() {
        Security.signout();
        return "[redirect]/";
    }

}