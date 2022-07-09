package eos.model.web;

import java.util.List;
import java.util.UUID;

public abstract class BasePartial {
    String guid;
    Integer idx;
    String entry;
    Object mojo;
    String type;
    String field;
    String spec;
    boolean withinIterable;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public boolean isWithinIterable() {
        return withinIterable;
    }

    public void setWithinIterable(boolean withinIterable) {
        this.withinIterable = withinIterable;
    }

    public static String BASiC = "basic";
    public static String ITeRABLE = "iterable";
    public static String SPeC = "spec";

}
