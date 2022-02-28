package eos;

import eos.EOS;
import eos.util.Support;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import eos.ux.ExperienceProcessor;
import eos.startup.ContainerInitializer;
import eos.util.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseTest {

    EOS eos;
    EOS.Cache cache;
    Support support;

    @AfterEach
    public void shutdown() throws Exception {
        eos.stop();
    }

    @BeforeEach
    public void setup() throws Exception {
        eos = new EOS.Builder().withPort(8080).luminosity(10).create();
        eos.start();

        support = new Support();
        Settings settings = new Settings();
        settings.setCreateDb(true);
        settings.setDropDb(true);
        settings.setNoAction(false);
        settings.setResources(new ArrayList());

        List propertiesFiles = new ArrayList<String>();
        propertiesFiles.add("eos.props");
        settings.setPropertiesFiles(propertiesFiles);

//        Map<String, Interceptor> interceptors = new HashMap();
//        MockInterceptor interceptor = new MockInterceptor();
//        String interceptorName = support.getName(interceptor.class.getName();
//        interceptor.put(interceptorName, interceptor);

        EOS.Repo repo = new EOS.Repo();
        cache = new EOS.Cache.Builder()
            .withSettings(settings)
            .withPointCuts(new HashMap<>())
            .withInterceptors(new HashMap<>())
            .withUxProcessor(new ExperienceProcessor())
            .withRepo(repo)
            .make();
        new ContainerInitializer.Builder()
            .withRepo(repo)
            .withCache(cache)
            .withSettings(settings)
            .build();
    }
}