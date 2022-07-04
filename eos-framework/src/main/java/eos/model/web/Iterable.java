package eos.model.web;

import java.util.List;

public class Iterable {
    int stop;
    int go;//todo:?
    String field;
    Integer mojosCount;
    List<Object> mojos;
    List<BasePartial> entries;
    BasicEntry basicEntry;

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

    public Integer getMojosCount() {
        return mojosCount;
    }

    public void setMojosCount(Integer mojosCount) {
        this.mojosCount = mojosCount;
    }

    public void setMojos(List<Object> mojos) {
        this.mojos = mojos;
    }

    public List<BasePartial> getEntries() {
        return entries;
    }

    public void setEntries(List<BasePartial> entries) {
        this.entries = entries;
    }

    public BasicEntry getBasicEntry() {
        return basicEntry;
    }

    public void setBasicEntry(BasicEntry basicEntry) {
        this.basicEntry = basicEntry;
    }
}
