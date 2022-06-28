package eos;

import eos.util.Support;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import eos.ux.ExperienceProcessor;
import eos.startup.ContainerStartup;
import eos.util.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseTest {

    Eos eos;
    Eos.Cache cache;
    Support support;

    @AfterEach
    public void shutdown() throws Exception {
        eos.stop();
    }

    @BeforeEach
    public void setup() throws Exception {
        eos = new Eos.Builder().port(8080).ambiance(10).create();
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

        Eos.Repo repo = new Eos.Repo();
        cache = new Eos.Cache.Builder()
            .withSettings(settings)
            .withPointCuts(new HashMap<>())
            .withInterceptors(new HashMap<>())
            .withUxProcessor(new ExperienceProcessor())
            .withRepo(repo)
            .make();
        new ContainerStartup.Builder()
            .withRepo(repo)
            .withCache(cache)
            .withSettings(settings)
            .build();
    }
}