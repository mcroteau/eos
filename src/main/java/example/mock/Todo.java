package example.mock;

import java.util.List;

public class Todo {

    Integer id;
    String title;
    boolean complete;
    List<TodoPerson> people;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public List<TodoPerson> getPeople() {
        return people;
    }

    public void setPeople(List<TodoPerson> people) {
        this.people = people;
    }
}
