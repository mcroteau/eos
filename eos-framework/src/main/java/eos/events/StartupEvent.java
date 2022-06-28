package eos.events;

import eos.Eos;

public interface StartupEvent {
    public void setupComplete(Eos.Cache cache);
}
