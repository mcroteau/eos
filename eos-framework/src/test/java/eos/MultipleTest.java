package eos;

import eos.exception.EosException;
import eos.model.web.HttpResponse;
import eos.ux.ExperienceProcessor;
import foo.Todo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MultipleTest extends BaseTest {

    ExperienceProcessor exp;

    MultipleTest(){
        exp = new ExperienceProcessor();
    }

    //todo.id = 3 *
    //todo.person.id = 2
    //todo.person.pet.id = 1
    //todo.person.pet.name = 'Apache';
    //todo.person.name = 'Pep Love'
    //todo.title = 'Paint'
    //todoDos.person.pet.name = '';
    //todo.person.pet.name != 'Tex'

    @Test
    public void o() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:if spec=\"${todo.person.pet.name != 'Tex'}\">\n");
        sb.append("Eos.\n");
        sb.append("</eos:if>\n");
        String result = exp.process(new HashMap<>(), sb.toString(), resp, null,null).trim();
        assertEquals("Eos.", result);
    }

    @Test
    public void n() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:if spec=\"${todoDos.person.pet.name == ''}\">\n");
        sb.append("Eos.\n");
        sb.append("</eos:if>\n");
        String result = exp.process(new HashMap<>(), sb.toString(), resp, null,null).trim();
        assertEquals("Eos.", result);
    }

    @Test
    public void m() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:if spec=\"${todo.title == 'Paint'}\">\n");
        sb.append("Eos.\n");
        sb.append("</eos:if>\n");
        String result = exp.process(new HashMap<>(), sb.toString(), resp, null,null).trim();
        assertEquals("Eos.", result);
    }

    @Test
    public void l() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:if spec=\"${todo.person.name == 'Pep Love'}\">\n");
        sb.append("Eos.\n");
        sb.append("</eos:if>\n");
        String result = exp.process(new HashMap<>(), sb.toString(), resp, null,null).trim();
        assertEquals("Eos.", result);
    }


    @Test
    public void k() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:if spec=\"${todo.person.pet.name == 'Apache'}\">\n");
        sb.append("Eos.\n");
        sb.append("</eos:if>\n");
        String result = exp.process(new HashMap<>(), sb.toString(), resp, null,null).trim();
        assertEquals("Eos.", result);
    }


    @Test
    public void j() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:if spec=\"${todo.person.pet.id == 3}\">\n");
        sb.append("Eos.\n");
        sb.append("</eos:if>\n");
        String result = exp.process(new HashMap<>(), sb.toString(), resp, null,null).trim();
        assertEquals("Eos.", result);
    }

    @Test
    public void ii() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:if spec=\"${todo.person.id == 2}\">\n");
        sb.append("Eos.\n");
        sb.append("</eos:if>\n");
        String result = exp.process(new HashMap<>(), sb.toString(), resp, null,null).trim();
        assertEquals("Eos.", result);
    }

    @Test
    public void i() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:if spec=\"${todo.id == 1}\">\n");
        sb.append("Eos.\n");
        sb.append("</eos:if>\n");
        String result = exp.process(new HashMap<>(), sb.toString(), resp, null,null).trim();
        assertEquals("Eos.", result);
    }

}
