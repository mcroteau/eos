package example.foo;


import eos.EOS;

public class Main {
    public static void main(String[] args) throws Exception {
        EOS eos = new EOS.Builder().withPort(8080).luminosity(200).create();
        eos.start();
    }
}
