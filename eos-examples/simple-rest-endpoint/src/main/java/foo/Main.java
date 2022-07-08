package foo;

import eos.Eos;

public class Main {
    public static void main(String[] args) throws Exception {
        Eos eos = new Eos.Builder()
                        .port(8080)
                        .ambiance(1200)
                        .create();
        eos.start();
    }
}
