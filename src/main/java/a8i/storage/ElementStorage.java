package a8i.storage;

import a8i.model.Element;

import java.util.HashMap;
import java.util.Map;

public class ElementStorage {

    Map<String, Element> elements;

    public ElementStorage(){
        this.elements = new HashMap<>();
    }

    public Map<String, Element> getElements(){
        return this.elements;
    }

}
