package test;

import eos.Eos;

public class Main {
    public static void main(String[] args) throws Exception {
        Eos eos = new Eos.Builder().withPort(3000).spawn(2000).make();
        eos.run();
    }
}
