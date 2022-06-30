package eos.model.web;

import java.util.List;

public class Iterable {

    public Iterable(){
        this.field = "";
    }

    int stop;
    int go;
    String field;
    StringBuilder content;
    List<Object> pojos;
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

    public StringBuilder getContent() {
        return content;
    }

    public void setContent(StringBuilder content) {
        this.content = content;
    }

    public List<Object> getPojos() {
        return pojos;
    }

    public void setPojos(List<Object> pojos) {
        this.pojos = pojos;
    }

    public List<String> getEntries() {
        return entries;
    }

    public void setEntries(List<String> entries) {
        this.entries = entries;
    }
}
