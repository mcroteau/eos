package eos.model.web;

import java.util.ArrayList;
import java.util.List;

public class IterablePartial extends BasePartial {
    public IterablePartial(){this.type = ITeRABLE; }

    String field;
    MojosResult mojosResult;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public MojosResult getMojosResult() {
        return mojosResult;
    }

    public void setMojosResult(MojosResult mojosResult) {
        this.mojosResult = mojosResult;
    }
}
