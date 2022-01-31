package eros.cargo;

import eros.model.ObjectDetails;

import java.util.Map;

public class ObjectStorage {

    Map<String, ObjectDetails> objects;

    public Map<String, ObjectDetails> getObjects() {
        return objects;
    }

    public void setObjects(Map<String, ObjectDetails> objects) {
        this.objects = objects;
    }
}
