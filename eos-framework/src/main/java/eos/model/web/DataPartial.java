package eos.model.web;

public class DataPartial extends BasePartial{

    String type = "iterable";

    String entry;
    boolean iterable;

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public boolean isIterable() {
        return iterable;
    }

    public void setIterable(boolean iterable) {
        this.iterable = iterable;
    }
}
