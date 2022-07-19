package eos.model.web;

public class IterablePartial extends BasePartial {
    public IterablePartial(){this.type = ITeRABLE; }

    String field;
    String iterable;
    IterableResult iterableResult;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getIterable() {
        return iterable;
    }

    public void setIterable(String iterable) {
        this.iterable = iterable;
    }

    public IterableResult getMojosResult() {
        return iterableResult;
    }

    public void setMojosResult(IterableResult iterableResult) {
        this.iterableResult = iterableResult;
    }
}
