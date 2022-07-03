package eos.model.web;

import java.util.ArrayList;
import java.util.List;

public class IterablePartial extends BasePartial {

    public IterablePartial(){this.type = ITeRABLE; }

    Iterable iterable;

    public Iterable getIterable() {
        return iterable;
    }

    public void setIterable(Iterable iterable) {
        this.iterable = iterable;
    }

}
