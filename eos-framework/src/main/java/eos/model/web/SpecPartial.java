package eos.model.web;

import java.util.List;

public class SpecPartial extends BasePartial{
    public SpecPartial(){
        this.type = SPeC;
    }

    String spec;
    BasicEntry basicEntry;

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public BasicEntry getBasicEntry() {
        return basicEntry;
    }

    public void setBasicEntry(BasicEntry basicEntry) {
        this.basicEntry = basicEntry;
    }
}
