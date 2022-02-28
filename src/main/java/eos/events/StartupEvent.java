package eos.events;

import eos.EOS;

public interface StartupEvent {
    public void setupComplete(EOS.Cache cache);
}
