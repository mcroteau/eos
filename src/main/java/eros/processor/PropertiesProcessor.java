package eros.processor;

import eros.A8i;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

public class PropertiesProcessor {

    A8i a8i;

    public PropertiesProcessor(A8i a8i){
        this.a8i = a8i;
    }

    protected InputStream getPropertiesFile(String propertyFile) throws Exception{

        InputStream is = this.getClass().getResourceAsStream(A8i.RESOURCES + propertyFile);

        if(is == null) {
            String resourceUri = a8i.getResourceUri();
            File file = new File(resourceUri + File.separator + propertyFile);
            if(!file.exists()) {
                throw new Exception(propertyFile + " properties file cannot be located...");
            }
            is = new FileInputStream(file);
        }
        return is;
    }

    public void run() throws IOException {

        if (a8i.getPropertiesFiles() != null) {

            for (String propertyFile : a8i.getPropertiesFiles()) {
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
                        a8i.getPropertyStorage().getProperties().put(key, value);
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
