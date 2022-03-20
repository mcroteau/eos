package eos.model.web;

import java.util.List;

public class Iterable {

    public Iterable(){
        this.field = "";
    }

    int start;
    int stop;
    String field;
    StringBuilder content;
    List<Object> pojos;

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getStop() {
        return stop;
    }

    public void setStop(int stop) {
        this.stop = stop;
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
}
