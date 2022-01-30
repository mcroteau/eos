package test;

import a8i.A8i;
import a8i.annotate.DataStore;
import a8i.annotate.Bind;

import java.util.ArrayList;
import java.util.List;

@DataStore
public class TodoRepo {

    @Bind
    A8i.Repo repo;

    public Long getCount() {
        String sql = "select count(*) from todos";
        Long count = repo.getLong(sql, new Object[]{});
        return count;
    }

    public Todo getById(long id){
        String sql = "select * from todos where id = [+]";
        Todo todo = (Todo) repo.get(sql, new Object[]{ id }, Todo.class);
        return todo;
    }

    public List<Todo> getList(){
        String sql = "select * from todos";
        List<Todo> todos = (ArrayList) repo.getList(sql, new Object[]{}, Todo.class);
        return todos;
    }

    public boolean save(Todo todo){
        String sql = "insert into todos (title) values ('[+]')";
        repo.save(sql, new Object[] {
                todo.getTitle()
        });
        return true;
    }

    public boolean update(Todo todo) {
        String sql = "update todos set title = '[+]' where id = [+]";
        repo.update(sql, new Object[] {
                todo.getTitle(),
                todo.getId()
        });
        return true;
    }

    public boolean delete(long id){
        String sql = "delete from todos where id = [+]";
        repo.delete(sql, new Object[] { id });
        return true;
    }

}
