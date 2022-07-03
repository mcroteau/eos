package eos.model.web;

public class SpecResult {
    boolean initialize;
    String spec;

    public boolean init(){
        return this.initialize;
    }

    public void setInitialize(boolean initialize) {
        this.initialize = initialize;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }
}
