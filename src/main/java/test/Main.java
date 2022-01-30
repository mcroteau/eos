package test;

import a8i.A8i;
import java.io.IOException;

public class Main {
    public static void main(String[] arguments) throws IOException {
        new A8i.Engine.Builder().spawn(130).withPort(3001).make();
    }
}
