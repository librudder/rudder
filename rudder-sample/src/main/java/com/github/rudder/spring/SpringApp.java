package com.github.rudder.spring;

import com.github.rudder.RudderApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import javax.inject.Inject;
import java.util.function.Consumer;

@SpringBootApplication
public class SpringApp {

    @Inject
    private SomeEntityService service;

    public static void main(String[] args) {
        SpringApplication.run(SpringApp.class);
    }

    public SomeEntityService getService() {
        return service;
    }

    public static class SpringRudderApp extends SpringApp implements ApplicationListener<ApplicationReadyEvent>, RudderApplication<SpringApp> {

        private static Consumer<SpringApp> callback;

        public static void setReadyCallback(final Consumer<SpringApp> callback) {
            SpringRudderApp.callback = callback;
        }

        @Override
        public void onApplicationEvent(final ApplicationReadyEvent applicationReadyEvent) {
            callback.accept(this);
        }
    }

}
