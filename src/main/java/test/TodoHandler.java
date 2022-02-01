package test;

import eos.annotate.Bind;
import eos.annotate.HttpHandler;
import eos.annotate.Variable;
import eos.annotate.verbs.Get;
import eos.annotate.verbs.Post;
import eos.model.web.HttpRequest;
import eos.model.web.HttpResponse;
import eos.util.Support;

import java.util.List;

@HttpHandler
public class TodoHandler {

    @Bind
    Support support;

    @Bind
    TodoRepo todoRepo;


    @Get("/")
    public String base(HttpResponse resp){
        List<Todo> todos = todoRepo.getList();
        resp.set("todos", todos);
        return "/pages/todo/list.htm";
    }

    @Get("/todos")
    public String getList(HttpResponse resp){
        List<Todo> todos = todoRepo.getList();
        resp.set("todos", todos);
        return "/pages/todo/list.htm";
    }

    @Get("/todos/create")
    public String getCreate(){
        return "/pages/todo/create.htm";
    }

    @Post("/todos/save")
    public String saveTodo(HttpRequest req,
                           HttpResponse resp){
        Todo todo = (Todo) support.get(req, Todo.class);
        todoRepo.save(todo);
        resp.set("message", "Successfully added todo!");
        return "[redirect]/todos";
    }

    @Get("/todos/edit/{{id}}")
    public String getEdit(HttpResponse resp,
                          @Variable Integer id){
        Todo todo = todoRepo.getById(id);
        resp.set("todo", todo);
        return "/pages/todo/edit.htm";
    }

    @Post("/todos/update/{{id}}")
    public String updateTodo(HttpRequest req,
                             HttpResponse resp,
                             @Variable Integer id){
        Todo todo = todoRepo.getById(id);
        todo.setTitle(req.value("title"));
        todoRepo.update(todo);
        resp.set("message", "Successfully updated todo!");
        return "[redirect]/todos/edit/" + todo.getId();
    }

    @Post("/todos/delete/{{id}}")
    public String deleteTodo(HttpResponse resp,
                             @Variable Integer id){
        todoRepo.delete(id);
        resp.set("message", "Successfully deleted todo!");
        return "[redirect]/todos";
    }

    @Get("/todos/person/add/{{id}}")
    public String addPersonView(HttpResponse resp,
                                @Variable Integer id){
        Todo todo = todoRepo.getById(id);
        List<TodoPerson> people = todoRepo.getPeople(id);
        resp.set("people", people);
        resp.set("todo", todo);
        return "/pages/todo/add_person.htm";
    }

    @Post("/todos/person/add")
    public String addPerson(HttpRequest req,
                            HttpResponse resp){
        TodoPerson todoPerson = (TodoPerson) support.get(req, TodoPerson.class);
        todoRepo.savePerson(todoPerson);
        return "[redirect]/todos/person/add/" + todoPerson.getTodoId();
    }

    @Post("/todos/person/delete/{{id}}")
    public String deletePerson(HttpResponse resp,
                               @Variable Integer id){
        todoRepo.deletePerson(id);
        resp.set("message", "Successfully deleted person from todo!");
        return "[redirect]/" ;
    }

}
