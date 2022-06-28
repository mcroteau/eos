package foo;

import eos.Eos;
import eos.fragment.AuthenticatedFragment;
import eos.fragment.GuestFragment;
import eos.fragment.IdentityFragment;
import eos.fragment.UserFragment;
import eos.interceptor.SecurityInterceptor;

public class Main {

    public static void main(String[] args) throws Exception {
        Eos eos = new Eos.Builder()
                    .port(8080)
                    .ambiance(20)//# threads
                    .create();

        eos.registerInterceptor(new SecurityInterceptor());

        eos.addFragment(new IdentityFragment());
        eos.addFragment(new UserFragment());
        eos.addFragment(new AuthenticatedFragment());
        eos.addFragment(new GuestFragment());

        eos.start();
    }

}