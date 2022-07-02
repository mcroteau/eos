package eos.model.web;

import java.util.ArrayList;
import java.util.List;

public class SpecPartial extends BasePartial{

    public SpecPartial(){
        this.type = SPeC;
    }
//    StopGo stopGo;//todo:nest
//    boolean iterable;
    IgnoreFragment ignoreFragment;

//    public StopGo getStopGo() {
//        return stopGo;
//    }
//
//    public void setStopGo(StopGo stopGo) {
//        this.stopGo = stopGo;
//    }

    public IgnoreFragment getIgnoreFragment() {
        return ignoreFragment;
    }

    public void setIgnoreFragment(IgnoreFragment ignoreFragment) {
        this.ignoreFragment = ignoreFragment;
    }

//    public boolean isIterable() {
//        return iterable;
//    }
//
//    public void setIterable(boolean iterable) {
//        this.iterable = iterable;
//    }
}
