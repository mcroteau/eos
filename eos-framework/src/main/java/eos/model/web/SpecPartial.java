package eos.model.web;

import java.util.List;

public class SpecPartial extends BasePartial{

    String type = "spec";

    boolean iterable;

    public boolean isIterable() {
        return iterable;
    }

    public void setIterable(boolean iterable) {
        this.iterable = iterable;
    }
}
