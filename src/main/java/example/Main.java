package example;

import eos.EOS;

public class Main {
    public static void main(String[] args) throws Exception {
        EOS eos = new EOS.Builder().withPort(3000).spawn(30).make();
        eos.start();
    }
}
