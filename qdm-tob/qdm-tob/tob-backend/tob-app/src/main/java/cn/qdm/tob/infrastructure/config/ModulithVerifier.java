package cn.qdm.tob.infrastructure.config;

import cn.qdm.tob.TobApplication;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.stereotype.Component;

@Component
public class ModulithVerifier implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        ApplicationModules.of(TobApplication.class).verify();
    }
}