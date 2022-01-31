package test;

import a8i.A8i;

public class Main {
    public static void main(String[] args) throws Exception {
        A8i.Server server = new A8i.ServerBuilder().withPort(3000).spawn(10).make();
        server.run();
    }
}
