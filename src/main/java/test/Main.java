package test;

import a8i.A8i;

public class Main {
    public static void main(String[] args) throws Exception {
        A8i.Engine engine = new A8i.Engine.Builder().withPort(3000).spawn(10000).make();
        engine.run();
    }
}
