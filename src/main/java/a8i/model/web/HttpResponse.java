package a8i.model.web;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    String design;
    String pageTitle;
    Map<String, Object> data;

    public HttpResponse(){
        this.data = new HashMap<>();
    }

    public void set(String key, Object value){
        this.data.put(key, value);
    }

    public Object get(String key){
        if(this.data.containsKey(key)){
            return this.data.get(key);
        }
        return null;
    }

    public void setPageTitle(String pageTitle){
        this.pageTitle = pageTitle;
    }

    public String getPageTitle(){
        if(this.pageTitle != null){
            return this.pageTitle;
        }
        return "";
    }

    public void setDesign(String design){
        this.design = design;
    }

    public String getDesign(){ return this.design; }

    public Map<String, Object> data(){
        return this.data;
    }

}
