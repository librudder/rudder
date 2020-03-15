package com.github.rudder.spring;

import com.github.rudder.client.ContaineredApplication;

import java.util.List;

public class SpringSample {

    public static void main(String[] args) throws Exception {
        final var clazz = SpringApp.SpringRudderApp.class;
        final var container = new ContaineredApplication<>("adoptopenjdk/openjdk11:x86_64-ubuntu-jdk-11.28", clazz, List.of());

        container.start();

        final SpringApp application = container.getApplication();

        final SomeEntityService service = application.getService();
        final SomeEntityRepository repo = service.getRepo();

        final SomeEntity save = service.createSome("Test");

        final Long id = save.getId();

        final SomeEntity some = service.findSomeEntityById(id);
        System.out.println(some.getTitle());

        final List<SomeEntity> all = repo.findAll();
        final SomeEntity someEntity = all.get(0);

        System.out.println(someEntity.getTitle());

        container.stop();
    }


}
