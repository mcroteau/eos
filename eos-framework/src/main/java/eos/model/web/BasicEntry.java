package eos.model.web;

import java.util.UUID;

public class BasicEntry {

    String guid;
    Integer idx;//todo:?
    String entry;
    Object mojo;
    Iterable iterable;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Integer getIdx() {
        return idx;
    }

    public void setIdx(Integer idx) {
        this.idx = idx;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public Object getMojo() {
        return mojo;
    }

    public void setMojo(Object mojo) {
        this.mojo = mojo;
    }

    public Iterable getIterable() {
        return iterable;
    }

    public void setIterable(Iterable iterable) {
        this.iterable = iterable;
    }

}
