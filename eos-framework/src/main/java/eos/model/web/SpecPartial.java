package eos.model.web;

import java.util.List;
import java.util.Map;

public class SpecPartial extends BasePartial{
    public SpecPartial(){
        this.type = SPeC;
    }

    String spec;
    Object mojo;
    BasicEntry basicEntry;
    Map<String, MojosResult> mojosResult;

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    @Override
    public Object getMojo() {
        return mojo;
    }

    @Override
    public void setMojo(Object mojo) {
        this.mojo = mojo;
    }

    public BasicEntry getBasicEntry() {
        return basicEntry;
    }

    public void setBasicEntry(BasicEntry basicEntry) {
        this.basicEntry = basicEntry;
    }

    public Map<String, MojosResult> getMojosResult() {
        return mojosResult;
    }

    public void setMojosResult(Map<String, MojosResult> mojosResult) {
        this.mojosResult = mojosResult;
    }
}
