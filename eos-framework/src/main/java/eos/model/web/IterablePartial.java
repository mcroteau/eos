package eos.model.web;

import java.util.ArrayList;
import java.util.List;

public class IterablePartial extends BasePartial {
    public IterablePartial(){this.type = ITeRABLE; }

    String field;
    String iterable;
    MojosResult mojosResult;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getIterable() {
        return iterable;
    }

    public void setIterable(String iterable) {
        this.iterable = iterable;
    }

    public MojosResult getMojosResult() {
        return mojosResult;
    }

    public void setMojosResult(MojosResult mojosResult) {
        this.mojosResult = mojosResult;
    }
}
