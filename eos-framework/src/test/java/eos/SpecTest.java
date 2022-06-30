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
public class SpecTest extends BaseTest {

    ExperienceProcessor exp;

    SpecTest(){
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

    @Test
    public void c() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:if spec=\"${true}\">\n");
        sb.append(" <eos:each in=\"${todos}\" item=\"tdo\">\n");
        sb.append("Noop.\n");
        sb.append(" </eos:each>\n");
        sb.append("</eos:if>\n");
        String result = exp.process(new HashMap<>(), sb.toString(), resp, null,null).trim();
        assertEquals("Noop.", result);
    }

    @Test
    public void d() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:if spec=\"${!true}\">\n");
        sb.append(" <eos:each in=\"${todos}\" item=\"tdo\">\n");
        sb.append("supa.\n");
        sb.append("cala.\n");
        sb.append("${todo.person.pet.name}.\n");
        sb.append("fraja.\n");
        sb.append("expi.\n");
        sb.append(" </eos:each>\n");
        sb.append("</eos:if>\n");
        String result = exp.process(new HashMap<>(), sb.toString(), resp, null,null).trim();
        assertEquals("supa.cala.Apache *6.fraja.expi.", result);//no ego, just listening.
    }

}
