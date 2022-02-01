package test;

import eos.annotate.Bind;
import eos.annotate.Design;
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
    @Design("designs/default.htm")
    public String base(HttpResponse resp){
        List<Todo> todos = todoRepo.getList();
        StringBuilder keywords = new StringBuilder();
        int idx = 1;
        for(Todo todo : todos){
            keywords.append(todo.getTitle());
            if(idx < todos.size()){
                keywords.append(",");
            }
            List<TodoPerson> people = todoRepo.getPeople(todo.getId());
            todo.setPeople(people);
            idx++;
        }
        resp.setTitle(keywords.toString() + " that need to be done!");
        resp.setKeywords(keywords.toString());
        resp.setDescription(keywords.toString() + " that need to be done!");
        resp.set("todos", todos);
        return "/pages/todo/list.htm";
    }

    @Get("/todos")
    @Design("designs/default.htm")
    public String getList(HttpResponse resp){
        List<Todo> todos = todoRepo.getList();
        for(Todo todo : todos){
            List<TodoPerson> people = todoRepo.getPeople(todo.getId());
            todo.setPeople(people);
        }
        resp.set("todos", todos);
        return "/pages/todo/list.htm";
    }

    @Get("/todos/create")
    @Design("designs/default.htm")
    public String getCreate(HttpResponse resp){
        resp.setTitle("Create");
        resp.setKeywords("create todo, todos create, awesome");
        resp.setDescription("Create your todo now!");
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
    @Design("designs/default.htm")
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
    @Design("designs/default.htm")
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
