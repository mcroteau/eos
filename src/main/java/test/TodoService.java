package test;

import eros.annotate.Bind;
import eros.model.web.HttpRequest;
import eros.model.web.HttpResponse;
import eros.annotate.Service;
import eros.util.Support;

import java.util.List;

@Service
public class TodoService {

    @Bind
    Support support;

    @Bind
    TodoRepo todoRepo;

    public String getList(HttpResponse resp) {
        List<Todo> todos = todoRepo.getList();
        resp.set("todos", todos);
        return "/pages/todo/list.htm";
    }

    public String saveTodo(HttpRequest req, HttpResponse resp) {
        Todo todo = (Todo) support.get(req, Todo.class);
        todoRepo.save(todo);
        resp.set("message", "Successfully added todo!");
        return "[redirect]/todos";
    }

    public String getEdit(Long id, HttpResponse resp) {
        Todo todo = todoRepo.getById(id);
        resp.set("todo", todo);
        return "/pages/todo/edit.htm";
    }

    public String updateTodo(Long id, HttpRequest req, HttpResponse resp) {
        Todo todo = todoRepo.getById(id);
        todo.setTitle(req.value("title"));
        todoRepo.update(todo);
        resp.set("message", "Successfully updated todo!");
        return "[redirect]/todos/edit/" + todo.getId();
    }

    public String deleteTodo(Long id, HttpResponse resp) {
        todoRepo.delete(id);
        resp.set("message", "Successfully deleted todo!");
        return "[redirect]/todos";
    }
}
