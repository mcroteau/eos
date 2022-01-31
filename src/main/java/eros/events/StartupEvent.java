package eros.events;

import eros.Eros;

public interface StartupEvent {
    public void setupComplete(Eros.Cache cache);
}
