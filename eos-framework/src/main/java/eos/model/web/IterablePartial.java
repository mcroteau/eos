package eos.model.web;

import java.util.ArrayList;
import java.util.List;

public class IterablePartial extends BasePartial {

    public IterablePartial(){
        this.type = ITeRABLE;
        partials = new ArrayList<>();
    }

    Iterable iterable;
    List<IterablePartial> partials;

    public Iterable getIterable() {
        return iterable;
    }

    public void setIterable(Iterable iterable) {
        this.iterable = iterable;
    }

    public List<IterablePartial> getPartials() {
        return partials;
    }

    public void setPartials(List<IterablePartial> partials) {
        this.partials = partials;
    }
}
