package eos.model.web;

import java.util.List;
import java.util.UUID;

public abstract class BasePartial {
    String guid;
    int go;//todo:?
    int stop;
    Integer idx;//todo:?
    String entry;
    Object mojo;
    String type;
    BasicEntry basicEntry;
    List<BasicEntry> entries;

    public BasePartial(){
        guid = UUID.randomUUID().toString();
    }

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

    public BasicEntry getBasicEntry() {
        return basicEntry;
    }

    public void setBasicEntry(BasicEntry basicEntry) {
        this.basicEntry = basicEntry;
    }

    public List<BasicEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<BasicEntry> entries) {
        this.entries = entries;
    }

    public static String BASiC = "basic";
    public static String ITeRABLE = "iterable";
    public static String SPeC = "spec";
}
