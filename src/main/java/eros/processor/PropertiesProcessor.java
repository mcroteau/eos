package eros.processor;

import eros.Eros;
import eros.util.Support;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

public class PropertiesProcessor {

    Eros.Cache cache;
    Support support;

    public PropertiesProcessor(Eros.Cache cache){
        this.cache = cache;
        this.support = new Support();
    }

    protected InputStream getPropertiesFile(String propertyFile) throws Exception{

        InputStream is = this.getClass().getResourceAsStream(Eros.RESOURCES + propertyFile);

        if(is == null) {
            String resourceUri = support.getResourceUri();
            File file = new File(resourceUri + File.separator + propertyFile);
            if(!file.exists()) {
                throw new Exception(propertyFile + " properties file cannot be located...");
            }
            is = new FileInputStream(file);
        }
        return is;
    }

    public void run() throws IOException {

        if (cache.getPropertiesFiles() != null) {

            for (String propertyFile : cache.getPropertiesFiles()) {
                InputStream is = null;
                Properties prop = null;
                try {

                    is = getPropertiesFile(propertyFile);
                    prop = new Properties();
                    prop.load(is);

                    Enumeration properties = prop.propertyNames();
                    while (properties.hasMoreElements()) {
                        String key = (String) properties.nextElement();
                        String value = prop.getProperty(key);
                        cache.getPropertyStorage().getProperties().put(key, value);
                    }

                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            }

        }

    }

}
