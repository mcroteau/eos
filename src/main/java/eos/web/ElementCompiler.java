package eos.web;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import eos.EOS;
import eos.model.web.FormElement;
import eos.model.web.HttpRequest;
import eos.model.web.HttpSession;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Thank you Mr. Walter
 * https://gist.github.com/JensWalter/0f19780d131d903879a2
 */
public class ElementCompiler {

    EOS.Cache cache;
    byte[] bytes;
    Map<String, HttpSession> sessions;
    HttpExchange httpExchange;

    public ElementCompiler(EOS.Cache cache, byte[] bytes, Map<String, HttpSession> sessions, HttpExchange httpExchange){
        this.cache = cache;
        this.bytes = bytes;
        this.sessions = sessions;
        this.httpExchange = httpExchange;
    }

    public HttpRequest compile(){
        Headers headers = httpExchange.getRequestHeaders();

        HttpRequest httpRequest = new HttpRequest(sessions, httpExchange);

        String contentType = headers.getFirst("Content-Type");

        String delimeter = "";
        if(contentType != null) {
            String[] bits = contentType.split("boundary=");
            if (bits.length > 1) {
                delimeter = bits[1];
                StringBuilder sb = new StringBuilder();
                for (byte b : bytes) {
                    sb.append((char) b);
                }

                String payload = sb.toString();
                List<FormElement> data = getElements(delimeter, payload);
                for (FormElement formElement : data) {
                    String key = formElement.getName();
                    httpRequest.set(key, formElement);
                }
            }else if(bytes.length > 0){

                String query = "";
                try {

                    query = new String(bytes, "utf-8");
                    query = java.net.URLDecoder.decode(query, StandardCharsets.UTF_8.name());

                } catch (Exception ex){
                    ex.printStackTrace();
                }

                for (String entry : query.split("&")) {
                    FormElement element = new FormElement();
                    String[] keyValue = entry.split("=", 2);
                    String key = keyValue[0];
                    if(keyValue.length > 1){
                        String value = keyValue[1];
                        element.setName(key);
                        element.setValue(value);
                    }else{
                        element.setName(key);
                        element.setValue("");
                    }
                    httpRequest.data().put(key, element);
                }

            }
        }

        return httpRequest;
    }


    protected List<FormElement> getElements(String delimeter, String payload){
        List<FormElement> formData = new ArrayList<>();
        Integer index = payload.indexOf("name=\"");
        FormElement data = getData(index, delimeter, payload);
        formData.add(data);
        while (index >= 0) {
            index = payload.indexOf("name=\"", index + 1);
            if(index >= 0){
                FormElement dataDos = getData(index, delimeter, payload);
                formData.add(dataDos);
            }
        }
        return formData;
    }

    protected FormElement getData(int index, String delimeter, String payload){
        FormElement formElement = new FormElement();
        Integer startName = payload.indexOf("\"", index + 1);
        Integer endName = payload.indexOf("\"", startName + 1);
        String name = payload.substring(startName + 1, endName);
        formElement.setName(name);

        Integer fileIdx = payload.indexOf("filename=", endName + 1);

        //if we are equal to 3, then we are on the same line,
        //we have a file and we can proceed.
        if(fileIdx - endName == 3) {
            Integer startFile = payload.indexOf("\"", fileIdx + 1);
            Integer endFile = payload.indexOf("\"", startFile + 1);
            String fileName = payload.substring(startFile + 1, endFile);
            formElement.setFileName(fileName);

            Integer startContent = payload.indexOf("Content-Type", endFile + 1);
            Integer startType = payload.indexOf(":", startContent + 1);
            Integer endType = payload.indexOf("\r\n", startType + 1);
            String type = payload.substring(startType + 1, endType).trim();
            formElement.setContentType(type);

            Integer startBytes = payload.indexOf("\r\n", endType + 1);
            Integer endBytes = payload.indexOf(delimeter, startBytes + 4);

            String value = payload.substring(startBytes, endBytes);
            Integer startValue = startBytes + 2;
            Integer endValue = endBytes;

            if(value.endsWith("--")){
                endValue = endBytes - 2;// -- tells us we are at the end
            }

            endValue = endValue - 2;//finicky, need to remove the final 2 bytes

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for(int z = startValue; z < endValue; z++){
                byte b = bytes[z];
                baos.write(b);
            }

            formElement.setFileBytes(baos.toByteArray());

        }else{
            //a plain old value here.
            Integer startValue = payload.indexOf("\r\n", endName + 1);
            Integer endValue = payload.indexOf(delimeter, startValue + 1);
            String value = payload.substring(startValue + 1, endValue);
            if(value.endsWith("\r\n--")){
                int lastbit = value.indexOf("\r\n--");
                value = value.substring(0, lastbit).trim();
            }
            formElement.setValue(value);
        }

        return formElement;
    }
}