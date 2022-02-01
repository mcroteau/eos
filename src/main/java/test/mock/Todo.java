package test.mock;

import java.util.List;

public class Todo {

    Integer id;
    String title;
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

    public List<TodoPerson> getPeople() {
        return people;
    }

    public void setPeople(List<TodoPerson> people) {
        this.people = people;
    }
}
