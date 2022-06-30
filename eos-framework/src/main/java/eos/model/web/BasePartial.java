package eos.model.web;

import java.util.List;

public abstract class BasePartial {

    List<String> entries;
    StringBuilder output;

    public List<String> getEntries() {
        return entries;
    }

    public void setEntries(List<String> entries) {
        this.entries = entries;
    }

    public StringBuilder getOutput() {
        return output;
    }

    public void setOutput(StringBuilder output) {
        this.output = output;
    }
}
