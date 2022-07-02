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
public class SingleTest extends BaseTest {

    ExperienceProcessor exp;

    SingleTest(){
        exp = new ExperienceProcessor();
    }

    @Test
    public void c() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:if spec=\"${null == null}\">\n");
        sb.append("Eos.\n");
        sb.append("</eos:if>\n");
        String result = exp.process(new HashMap<>(), sb.toString(), resp, null,null).trim();
        assertEquals("Eos.", result);
    }

    @Test
    public void d() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:if spec=\"${blank == ''}\">\n");
        sb.append("Eos.\n");
        sb.append("</eos:if>\n");
        String result = exp.process(new HashMap<>(), sb.toString(), resp, null,null).trim();
        assertEquals("Eos.", result);
    }

    @Test
    public void e() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:if spec=\"${value == 'value'}\">\n");
        sb.append("Eos.\n");
        sb.append("</eos:if>\n");
        String result = exp.process(new HashMap<>(), sb.toString(), resp, null,null).trim();
        assertEquals("Eos.", result);
    }

    @Test
    public void f() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:if spec=\"${notnil != null}\">\n");
        sb.append("Eos.\n");
        sb.append("</eos:if>\n");
        String result = exp.process(new HashMap<>(), sb.toString(), resp, null,null).trim();
        assertEquals("Eos.", result);
    }

    @Test
    public void g() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:if spec=\"${notblank != ''}\">\n");
        sb.append("Eos.\n");
        sb.append("</eos:if>\n");
        String result = exp.process(new HashMap<>(), sb.toString(), resp, null,null).trim();
        assertEquals("Eos.", result);
    }

    @Test
    public void h() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:if spec=\"${notvalue != 'notvalue'}\">\n");
        sb.append("Eos.\n");
        sb.append("</eos:if>\n");
        String result = exp.process(new HashMap<>(), sb.toString(), resp, null,null).trim();
        assertEquals("Eos.", result);
    }

}
