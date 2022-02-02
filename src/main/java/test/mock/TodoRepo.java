package test.mock;

import eos.annotate.DataStore;
import eos.annotate.Bind;
import eos.jdbc.Repo;

import java.util.ArrayList;
import java.util.List;

@DataStore
public class TodoRepo {

    @Bind
    Repo repo;

    public Long getCount() {
        String sql = "select count(*) from todos";
        Long count = repo.getLong(sql, new Object[]{});
        return count;
    }

    public Todo getById(int id){
        String sql = "select * from todos where id = [+]";
        Todo todo = (Todo) repo.get(sql, new Object[]{ id }, Todo.class);
        return todo;
    }

    public List<Todo> getList(){
        String sql = "select * from todos";
        List<Todo> todos = (ArrayList) repo.getList(sql, new Object[]{}, Todo.class);
        return todos;
    }

    public void save(Todo todo){
        String sql = "insert into todos (title) values ('[+]')";
        repo.save(sql, new Object[] {
                todo.getTitle()
        });
    }

    public void update(Todo todo) {
        String sql = "update todos set title = '[+]', complete = [+] where id = [+]";
        repo.update(sql, new Object[] {
                todo.getTitle(),
                todo.isComplete(),
                todo.getId()
        });
    }

    public void delete(int id){
        String sql = "delete from todos where id = [+]";
        repo.delete(sql, new Object[] { id });
    }

    public List<TodoPerson> getPeople(int id){
        String sql = "select * from todo_people where todo_id = [+]";
        List<TodoPerson> todoPeople = (ArrayList) repo.getList(sql, new Object[]{ id }, TodoPerson.class);
        return todoPeople;
    }

    public void savePerson(TodoPerson todoPerson) {
        String sql = "insert into todo_people (todo_id, person) values ([+],'[+]')";
        repo.save(sql, new Object[] {
                todoPerson.getTodoId(),
                todoPerson.getPerson()
        });
    }

    public void deletePerson(Integer id) {
        String sql = "delete from todo_people where id = [+]";
        repo.delete(sql, new Object[] { id });
    }
}
