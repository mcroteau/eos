package eos;

import eos.exception.EosException;
import eos.model.web.HttpResponse;
import eos.ux.ExperienceProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IterationTest extends BaseTest {

    ExperienceProcessor exp;

    IterationTest(){
        exp = new ExperienceProcessor();
    }

    //tdo.id == todo.id
    //tdo.id != todo.id
    //tdo.person.pet.name == todo.person.pet.name
    //tdo.person.pet.name == 'Abdul'


    @Test
    public void a() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:each in=\"${todos}\" item=\"tdo\">\n");
        sb.append("${tdo.id}\n");
        sb.append("<eos:if spec=\"${tdo.id == 3}\">\n");
        sb.append("Eos.\n");
        sb.append("</eos:if>\n");
        sb.append("</eos:each>\n");
        String result = exp.process(new HashMap<>(), sb.toString(), resp, null,null).replaceAll("([^\\S\\r\\n])+|(?:\\r?\\n)+", "");
        assertEquals("0123Eos.45678910", result);
    }


}
