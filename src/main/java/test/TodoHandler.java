package test;

import eros.annotate.Bind;
import eros.annotate.HttpHandler;
import eros.annotate.Variable;
import eros.annotate.verbs.Get;
import eros.annotate.verbs.Post;
import eros.model.web.HttpRequest;
import eros.model.web.HttpResponse;

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
