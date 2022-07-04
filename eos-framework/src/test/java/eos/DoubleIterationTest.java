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
public class DoubleIterationTest extends BaseTest {

    ExperienceProcessor exp;

    DoubleIterationTest(){
        exp = new ExperienceProcessor();
    }

    //tdo.id == 3
    //tdo.id == todo.id
    //tdo.id != todo.id
    //tdo.person.pet.name == todo.person.pet.name
    //tdo.person.pet.name == 'Abdul'
    //tdo.person.pet.name == ''
    
//    @Test
//    public void a() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
//        HttpResponse resp = this.create();
//        StringBuilder sb = new StringBuilder();
//        sb.append("<eos:each in=\"${todos}\" item=\"tdo\">\n");
//        sb.append("${tdo.id}\n");
//        sb.append("<eos:if spec=\"${tdo.id == 3}\">\n");
//        sb.append("Eos.\n");
//        sb.append("</eos:if>\n");
//        sb.append("</eos:each>\n");
//        String result = exp.execute(new HashMap<>(), sb.toString(), resp, null,null).replaceAll("([^\\S\\r\\n])+|(?:\\r?\\n)+", "");
//        assertEquals("0123Eos.45678910", result);
//    }
//
//    @Test
//    public void b() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
//        HttpResponse resp = this.create();
//        StringBuilder sb = new StringBuilder();
//        sb.append("<eos:each in=\"${todos}\" item=\"tdo\">\n");
//        sb.append("${tdo.id}\n");
//        sb.append("<eos:if spec=\"${tdo.id == todo.id}\">\n");
//        sb.append("Eos.\n");
//        sb.append("</eos:if>\n");
//        sb.append("</eos:each>\n");
//        String result = exp.execute(new HashMap<>(), sb.toString(), resp, null,null).replaceAll("([^\\S\\r\\n])+|(?:\\r?\\n)+", "");
//        assertEquals("01Eos.2345678910", result);
//    }
//
//    @Test
//    public void c() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
//        HttpResponse resp = this.create();
//        StringBuilder sb = new StringBuilder();
//        sb.append("<eos:each in=\"${todos}\" item=\"tdo\">\n");
//        sb.append("${tdo.id}\n");
//        sb.append("<eos:if spec=\"${tdo.id != todo.id}\">\n");
//        sb.append("Eos.\n");
//        sb.append("</eos:if>\n");
//        sb.append("</eos:each>\n");
//        String result = exp.execute(new HashMap<>(), sb.toString(), resp, null,null).replaceAll("([^\\S\\r\\n])+|(?:\\r?\\n)+", "");
//        assertEquals("0Eos.12Eos.3Eos.4Eos.5Eos.6Eos.7Eos.8Eos.9Eos.10Eos.", result);
//    }
//
//    @Test
//    public void d() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
//        HttpResponse resp = this.create();
//        StringBuilder sb = new StringBuilder();
//        sb.append("<eos:each in=\"${todos}\" item=\"tdo\">\n");
//        sb.append("${tdo.person.pet.id}\n");
//        sb.append("<eos:if spec=\"${tdo.person.pet.name == todo.person.pet.name}\">\n");
//        sb.append("Eos.\n");
//        sb.append("</eos:if>\n");
//        sb.append("</eos:each>\n");
//        String result = exp.execute(new HashMap<>(), sb.toString(), resp, null,null).replaceAll("([^\\S\\r\\n])+|(?:\\r?\\n)+", "");
//        assertEquals("0123456Eos.78910", result);
//    }
//
//    @Test
//    public void e() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
//        HttpResponse resp = this.create();
//        StringBuilder sb = new StringBuilder();
//        sb.append("<eos:each in=\"${todos}\" item=\"tdo\">\n");
//        sb.append("${tdo.person.pet.id}\n");
//        sb.append("<eos:if spec=\"${tdo.person.pet.name == 'Apache *3'}\">\n");
//        sb.append("Eos.\n");
//        sb.append("</eos:if>\n");
//        sb.append("</eos:each>\n");
//        String result = exp.execute(new HashMap<>(), sb.toString(), resp, null,null).replaceAll("([^\\S\\r\\n])+|(?:\\r?\\n)+", "");
//        assertEquals("0123Eos.45678910", result);
//    }
//i love you guys

    @Test
    public void f() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("<eos:each items=\"${todos}\" var=\"tdo\">\n");
        sb.append("     ${tdo.id}\n");
        sb.append("     <eos:if spec=\"${tdo.person.name == ''}\">\n");
        sb.append("         Eos.\n");
        sb.append("     </eos:if>\n");
        sb.append("</eos:each>\n");
        //birdman had to do with michael keaton or the basketball player. he was rating them out
        String result = exp.execute(new HashMap<>(), sb.toString(), resp, null,null).replaceAll("([^\\S\\r\\n])+|(?:\\r?\\n)+", "");
        assertEquals("01Eos.2345678910", result);
    }

    @Test
    public void e() throws EosException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        HttpResponse resp = this.create();
        StringBuilder sb = new StringBuilder();
        sb.append("${todo.person.name}\n");
        sb.append("<eos:each items=\"${todos}\" var=\"tdo\">\n");
        sb.append("     ${tdo.id}\n");
        sb.append("     <eos:if spec=\"${tdo.person.name == ''}\">\n");
        sb.append("         <eos:each items=\"${tdo.people}\" var=\"person\">\n");
        sb.append("             <eos:if spec=\"${person.pet.name == ''}\">\n");
        sb.append("                 ${person.pet.name}\n");
        sb.append("             </eos:if>\n");
        sb.append("         </eos:each>\n");;
        sb.append("     </eos:if>\n");
        sb.append("</eos:each>\n");
        //birdman had to do with michael keaton or the basketball player. he was rating them out
        String result = exp.execute(new HashMap<>(), sb.toString(), resp, null,null).replaceAll("([^\\S\\r\\n])+|(?:\\r?\\n)+", "");
        assertEquals("01Eos.2345678910", result);
    }
}
