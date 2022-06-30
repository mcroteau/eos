package eos.model.web;

import java.util.List;

public class IterablePartial extends BasePartial {

    String type = "iterable";

    Iterable iterable;

    public Iterable getIterable() {
        return iterable;
    }

    public void setIterable(Iterable iterable) {
        this.iterable = iterable;
    }
}
