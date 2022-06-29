package eos;

import eos.exception.EosException;
import eos.model.web.HttpResponse;
import eos.ux.ExperienceProcessor;
import foo.Person;
import foo.Pet;
import foo.Todo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExperienceTest extends BaseTest {

    ExperienceProcessor exp;

    ExperienceTest(){
        exp = new ExperienceProcessor();
    }

    @Test
    public void a() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:if spec=\"${true}\">\n");
        sb.append("Eos.\n");
        sb.append("</eos:if>\n");
        String result = exp.process(new HashMap<>(), sb.toString(), resp, null,null).trim();
        assertEquals("Eos.", result);
    }

    @Test
    public void b() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:if spec=\"${!condition}\">\n");
        sb.append("Eos.\n");
        sb.append("</eos:if>\n");
        String result = exp.process(new HashMap<>(), sb.toString(), resp, null,null).trim();
        assertEquals("Eos.", result);
    }

    public HttpResponse create() {

        HttpResponse resp = new HttpResponse();

        List<Todo> todos = new ArrayList<>();
        for(int idx = 0; idx < 3; idx++) {
            Todo todo = new Todo();
            todo.setId(idx);
            todo.setTitle("Assignment #" + idx);

            List<Person> people = new ArrayList<>();
            for (int idxn = 0; idxn < 3; idxn++) {
                Person person = new Person();
                person.setId(idxn);
                person.setName("Pep " + idx);
                List<Pet> pets = new ArrayList<>();
                for (int idxx = 0; idxx < 23; idxx++) {
                    Pet pet = new Pet();
                    pet.setId(idxx);
                    pet.setName("Nginx ");
                    pets.add(pet);
                }
                person.setPets(pets);
            }
            todo.setPeople(people);
            todos.add(todo);
        }

        resp.set("todos", todos);
        resp.set("true", true);
        resp.set("condition", false);

        return resp;
    }
}
