package test;

import eos.Eos;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;

public class Main {
    public static void main(String[] args) throws Exception {
        Eos eos = new Eos.Builder().withPort(3000).spawn(2000).make();
        eos.run();

        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        junit.run(SaveTest.class);
    }
}
