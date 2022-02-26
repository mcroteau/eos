package test;

import eos.Eos;
import eos.util.Support;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;

public class Main {
    public static void main(String[] args) throws Exception {
        Support support = new Support();
        Eos eos = new Eos.Builder().withSupport(support).withPort(3001).spawn(1301).make();
        eos.run();
    }
}
