package eos.model.web;

import java.util.List;

public class Iterable {

    int stop;
    int go;
    String field;
    List<Object> mojos;
    List<String> entries;

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

    public List<String> getEntries() {
        return entries;
    }

    public void setEntries(List<String> entries) {
        this.entries = entries;
    }
}
