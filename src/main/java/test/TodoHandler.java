package test;

import eos.annotate.Bind;
import eos.annotate.HttpHandler;
import eos.annotate.Variable;
import eos.annotate.verbs.Get;
import eos.annotate.verbs.Post;
import eos.model.web.HttpRequest;
import eos.model.web.HttpResponse;

@HttpHandler
public class TodoHandler {

    @Bind
    TodoService todoService;

    @Get("/")
    public String hi(){
        return "/pages/index.htm";
    }

    @Get("/todos")
    public String getList(HttpResponse resp){
        return todoService.getList(resp);
    }

    @Get("/todos/create")
    public String getCreate(){
        return "/pages/todo/create.htm";
    }

    @Post("/todos/save")
    public String saveTodo(HttpRequest req,
                           HttpResponse resp){
        return todoService.saveTodo(req, resp);
    }

    @Get("/todos/edit/{{id}}")
    public String getEdit(HttpResponse resp,
                          @Variable Long id){
        return todoService.getEdit(id, resp);
    }

    @Post("/todos/update/{{id}}")
    public String updateTodo(HttpRequest req,
                             HttpResponse resp,
                             @Variable Long id){
        return todoService.updateTodo(id, req, resp);
    }

    @Post("/todos/delete/{{id}}")
    public String deleteTodo(HttpResponse resp,
                             @Variable Long id){
        return todoService.deleteTodo(id, resp);
    }

}
