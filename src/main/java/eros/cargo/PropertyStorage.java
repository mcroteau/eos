package eros.cargo;

import java.util.HashMap;
import java.util.Map;

public class PropertyStorage {
    Map<String, String> properties;

    public PropertyStorage(){
        this.properties = new HashMap<>();
    }

    public Map<String, String> getProperties(){
        return this.properties;
    }
}