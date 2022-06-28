package foo;

import eos.EOS;

public class Main {
    public static void main(String[] args) throws Exception {
        EOS eos = new EOS.Builder()
                        .port(8080)
                        .ambiance(200)
                        .create();
        eos.start();
    }
}
