package eos.model.web;

import java.util.List;

public class Iterable {

    int stop;
    int go;
    String field;
    List<Object> mojos;
    List<BasicEntry> entries;
    BasicEntry entry;

    public int getStop() {
        return stop;
    }

    public void setStop(int stop) {
        this.stop = stop;
    }

    public int getGo() {
        return go;
    }

    public void setGo(int go) {
        this.go = go;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public List<Object> getMojos() {
        return mojos;
    }

    public void setMojos(List<Object> mojos) {
        this.mojos = mojos;
    }

    public List<BasicEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<BasicEntry> entries) {
        this.entries = entries;
    }

    public BasicEntry getEntry() {
        return entry;
    }

    public void setEntry(BasicEntry entry) {
        this.entry = entry;
    }
}
