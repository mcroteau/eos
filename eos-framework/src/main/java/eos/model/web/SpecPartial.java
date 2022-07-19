package eos.model.web;

import java.util.Map;

public class SpecPartial extends BasePartial{
    public SpecPartial(){
        this.type = SPeC;
    }

    String spec;
    Map<String, IterableResult> mojosResult;

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public Map<String, IterableResult> getMojosResult() {
        return mojosResult;
    }

    public void setMojosResult(Map<String, IterableResult> mojosResult) {
        this.mojosResult = mojosResult;
    }
}
