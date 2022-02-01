package test;

import eros.Eros;

public class Main {
    public static void main(String[] args) throws Exception {
        Eros eros = new Eros.Builder().withPort(3000).spawn(2000).make();
        eros.run();
    }
}
