package eos;

import eos.exception.EosException;
import eos.model.web.HttpResponse;
import eos.web.ExperienceProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

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
        String result = exp.execute(new HashMap<>(), sb.toString(), resp, null,null).trim();
        assertEquals("Eos.", result);
    }

    @Test
    public void b() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:if spec=\"${!condition}\">\n");
        sb.append("Eos.\n");
        sb.append("</eos:if>\n");
        String result = exp.execute(new HashMap<>(), sb.toString(), resp, null,null).trim();
        assertEquals("Eos.", result);
    }

    @Test
    public void c() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:if spec=\"${true}\">\n");
        sb.append(" <eos:each items=\"${todos}\" var=\"tdo\">\n");
        sb.append("${tdo.person.pet.name}.\n");
        sb.append(" </eos:each>\n");
        sb.append("</eos:if>\n");
        String result = exp.execute(new HashMap<>(), sb.toString(), resp, null,null).replaceAll("([^\\S\\r\\n])+|(?:\\r?\\n)+", "");
        assertEquals("Apache*0.Apache*1.Apache*2.", result);//no ego, just listening.
    }


    @Test
    public void d() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:if spec=\"${!condition}\">\n");
        sb.append(" <eos:each items=\"${todos}\" var=\"tdo\">\n");
        sb.append("${tdo.person.pet.name}.\n");
        sb.append(" </eos:each>\n");
        sb.append("</eos:if>\n");
        String result = exp.execute(new HashMap<>(), sb.toString(), resp, null,null).replaceAll("([^\\S\\r\\n])+|(?:\\r?\\n)+", "");
        assertEquals("", result);//no ego, just listening.
    }

}
