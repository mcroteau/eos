package eos.util;

import com.sun.net.httpserver.HttpExchange;
import eos.Eos;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;

public class ResourceResponse {

    final String GET = "get";
    final String WEBAPP = "web-ux";
    final String CONTENTTYPE = "Content-Type";

    Eos.Cache cache;
    String requestUri;
    String httpVerb;
    HttpExchange httpExchange;
    Support support;

    public ResourceResponse(Builder builder){
        this.cache = builder.cache;
        this.requestUri = builder.requestUri;
        this.httpVerb = builder.httpVerb;
        this.httpExchange = builder.httpExchange;
        this.support = new Support();
    }

    public static Boolean isResource(String requestUri, Eos.Cache cache){
        if(cache.getResources() == null) return false;

        String[] bits = requestUri.split("/");
        if(bits.length > 1) {
            String resource = bits[1];
            if (cache.getResources().contains(resource)) return true;
        }
        return false;
    }

    public void serve() throws IOException {
        InputStream fis;

        if(support.isJar()){

            if(requestUri.startsWith("/"))requestUri = requestUri.replaceFirst("/","");
            String resourcePath = "/web-ux/" + requestUri;

            InputStream ris = this.getClass().getResourceAsStream(resourcePath);

            if(ris != null) {
                ByteArrayOutputStream unebaos = new ByteArrayOutputStream();
                byte[] bytes = new byte[1024 * 13];
                int unelength;
                while ((unelength = ris.read(bytes)) != -1) {
                    unebaos.write(bytes, 0, unelength);
                }

                MimeGetter mimeGetter = new MimeGetter(requestUri);
                String mimeType = mimeGetter.resolve();

                httpExchange.getResponseHeaders().set(this.CONTENTTYPE, mimeType);

                if (httpVerb.equals(GET)) {
                    httpExchange.sendResponseHeaders(200, unebaos.size());
                    OutputStream os = httpExchange.getResponseBody();
                    os.write(unebaos.toByteArray());
                    os.close();
                    os.flush();
                } else {
                    httpExchange.sendResponseHeaders(200, -1);
                }
                ris.close();
            }

        }else{
            String webPath = Paths.get(this.WEBAPP).toString();
            String filePath = webPath.concat(requestUri);
            File file = new File(filePath);
            try {
                fis = new FileInputStream(filePath);
            } catch (FileNotFoundException e) {
                //Thank you: https://stackoverflow.com/users/35070/phihag
                outputAlert(httpExchange, 404);
                return;
            }

            if(fis != null) {
                MimeGetter mimeGetter = new MimeGetter(filePath);
                String mimeType = mimeGetter.resolve();

                httpExchange.getResponseHeaders().set(this.CONTENTTYPE, mimeType);
                if (httpVerb.equals(GET)) {
                    httpExchange.sendResponseHeaders(200, file.length());
                    OutputStream os = httpExchange.getResponseBody();
                    copyStream(fis, os);
                    os.close();
                    os.flush();
                } else {
                    httpExchange.sendResponseHeaders(200, -1);
                }
                fis.close();
            }
        }
    }

    private void outputAlert(HttpExchange httpExchange, int errorCode) throws IOException {
        String message = "A8i/ resource missing! " + errorCode;
        byte[] messageBytes = message.getBytes("utf-8");

        httpExchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        httpExchange.sendResponseHeaders(errorCode, messageBytes.length);
        OutputStream os = httpExchange.getResponseBody();
        os.write(messageBytes);
        os.close();
    }

    private void copyStream(InputStream is, OutputStream os) throws IOException {
        byte[] bytes = new byte[1024 * 13];
        int n;
        while ((n = is.read(bytes)) >= 0) {
            os.write(bytes, 0, n);
        }
    }

    public static class Builder {
        Eos.Cache cache;
        String requestUri;
        String httpVerb;
        List<String> resources;
        HttpExchange httpExchange;

        public Builder withRequestUri(String requestUri){
            this.requestUri = requestUri;
            return this;
        }
        public Builder withHttpVerb(String httpVerb){
            this.httpVerb = httpVerb;
            return this;
        }
        public Builder withCache(Eos.Cache cache){
            this.cache = cache;
            return this;
        }
        public Builder withResources(List<String> resources){
            this.resources = resources;
            return this;
        }
        public Builder withHttpExchange(HttpExchange httpExchange){
            this.httpExchange = httpExchange;
            return this;
        }
        public ResourceResponse make(){
            return new ResourceResponse(this);
        }

    }
}
