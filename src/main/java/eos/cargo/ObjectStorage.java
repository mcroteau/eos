package eos.cargo;

import eos.model.ObjectDetails;

import java.util.HashMap;
import java.util.Map;

public class ObjectStorage {

    Map<String, ObjectDetails> objects;

    public ObjectStorage(){
        this.objects = new HashMap<>();
    }

    public Map<String, ObjectDetails> getObjects() {
        return objects;
    }

    public void setObjects(Map<String, ObjectDetails> objects) {
        this.objects = objects;
    }
}
