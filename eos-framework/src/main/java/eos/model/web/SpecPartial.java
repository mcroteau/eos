package eos.model.web;

import java.util.List;
import java.util.Map;

public class SpecPartial extends BasePartial{
    public SpecPartial(){
        this.type = SPeC;
    }

    String spec;
    Map<String, MojosResult> mojosResult;

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public Map<String, MojosResult> getMojosResult() {
        return mojosResult;
    }

    public void setMojosResult(Map<String, MojosResult> mojosResult) {
        this.mojosResult = mojosResult;
    }
}
