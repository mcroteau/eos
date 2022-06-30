package eos.model.web;

import java.util.List;

public class SpecPartial extends BasePartial{

    String type = "spec";

    StopGo stopGo;
    boolean iterable;

    public StopGo getStopGo() {
        return stopGo;
    }

    public void setStopGo(StopGo stopGo) {
        this.stopGo = stopGo;
    }

    public boolean isIterable() {
        return iterable;
    }

    public void setIterable(boolean iterable) {
        this.iterable = iterable;
    }
}
