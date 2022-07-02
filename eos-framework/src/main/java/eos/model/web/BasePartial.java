package eos.model.web;

import java.util.List;

public abstract class BasePartial {

    int go;
    int stop;
    String type;
    BasicEntry entry;
    List<BasicEntry> entries;

    public static String BASiC = "basic";
    public static String ITeRABLE = "iterable";
    public static String SPeC = "spec";

    public int getGo() {
        return go;
    }

    public void setGo(int go) {
        this.go = go;
    }

    public int getStop() {
        return stop;
    }

    public void setStop(int stop) {
        this.stop = stop;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BasicEntry getEntry() {
        return entry;
    }

    public void setEntry(BasicEntry entry) {
        this.entry = entry;
    }

    public List<BasicEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<BasicEntry> entries) {
        this.entries = entries;
    }
}
