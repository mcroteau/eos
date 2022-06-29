package eos;

import eos.model.web.HttpResponse;
import eos.util.Support;
import foo.Person;
import foo.Pet;
import foo.Todo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import eos.ux.ExperienceProcessor;
import eos.startup.ContainerStartup;
import eos.util.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseTest {

    Eos eos;
    Eos.Cache cache;
    Support support;
    ExperienceProcessor exp;

    @AfterEach
    public void shutdown() throws Exception {
        eos.stop();
    }

    @BeforeEach
    public void setup() throws Exception {
        eos = new Eos.Builder().port(3001).ambiance(10).create();
        eos.start();

        support = new Support();
        Settings settings = new Settings();
        settings.setCreateDb(true);
        settings.setDropDb(true);
        settings.setNoAction(false);
        settings.setResources(new ArrayList());

        List propertiesFiles = new ArrayList<String>();
        propertiesFiles.add("eos.props");
        settings.setPropertiesFiles(propertiesFiles);

        Eos.Repo repo = new Eos.Repo();
        cache = new Eos.Cache.Builder()
            .withSettings(settings)
            .withPointCuts(new HashMap<>())
            .withInterceptors(new HashMap<>())
            .withUxProcessor(new ExperienceProcessor())
            .withRepo(repo)
            .make();
        new ContainerStartup.Builder()
            .withRepo(repo)
            .withCache(cache)
            .withSettings(settings)
            .build();
    }


    public HttpResponse create() {

        HttpResponse resp = new HttpResponse();

        List<Todo> todos = new ArrayList<>();
        for(int idx = 0; idx < 11; idx++) {
            Todo todo = new Todo();
            todo.setId(idx);
            todo.setTitle("Todo #" + idx);

            List<Person> people = new ArrayList<>();
            for (int idxn = 0; idxn < 11; idxn++) {
                Person person = new Person();
                person.setId(idxn);
                person.setName("Pep " + idx);
                List<Pet> pets = new ArrayList<>();
                for (int idxx = 0; idxx < 10; idxx++) {
                    Pet pet = new Pet();
                    pet.setId(idxx);
                    pet.setName("Nginx #" + idxx);
                    pets.add(pet);
                }
                person.setPets(pets);
            }
            todo.setPeople(people);
            todos.add(todo);
        }

        //todo.id = 1
        //todo.person.id = 2
        //todo.person.pet.id = 3
        //todo.person.pet = 'Apache';
        //todo.person = 'Pep Love'
        //todo.title = 'Paint'
        Todo todo = getTodo();

        //todo.person.pet = '';
        //todo.person.name != 'George Straight'
        //todo.title != 'draw'
        Todo todoDos = getBlankPet();

        //todo.person.pet = null;
        //todo.person.name != ''
        //todo.title != ''
        Todo todoTres = getNilPet();


        resp.set("null", null);
        resp.set("blank", "");
        resp.set("value", "value");
        resp.set("notnil", new Object());
        resp.set("notblank", "value");
        resp.set("notvalue", "value");
        resp.set("todo", todo);
        resp.set("todoDos", todoDos);
        resp.set("todoTres", todoTres);
        resp.set("todos", todos);
        resp.set("true", true);
        resp.set("condition", false);

        return resp;
    }

    public Todo getTodo(){
        Pet pet = new Pet();
        pet.setId(3);
        pet.setName("Apache");

        Person person = new Person();
        person.setId(2);
        person.setName("Pep Love");
        person.setPet(pet);

        Todo todo = new Todo();
        todo.setId(1);
        todo.setTitle("Paint");
        todo.setPerson(person);

        return todo;
    }

    public Todo getBlankPet(){
        Pet pet = new Pet();
        pet.setId(4);
        pet.setName("");

        Person person = new Person();
        person.setId(5);
        person.setName("Pep Love");
        person.setPet(pet);

        Todo todo = new Todo();
        todo.setId(6);
        todo.setTitle("Paint");
        todo.setPerson(person);

        return todo;
    }

    public Todo getNilPet(){
        Person person = new Person();
        person.setId(8);
        person.setName("Pep Love");
        person.setPet(null);

        Todo todo = new Todo();
        todo.setId(9);
        todo.setTitle("Paint");
        todo.setPerson(person);

        return todo;
    }

}