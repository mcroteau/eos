package foo.assist;

import eos.Security;
import eos.Eos;
import eos.annotate.Events;
import eos.events.StartupEvent;

@Events
public class Init implements StartupEvent {

    public void setupComplete(Eos.Cache cache) {
        var authAccess = (AuthAccess) cache.getElement("authAccess");
        Security.configure(authAccess);
    }

}