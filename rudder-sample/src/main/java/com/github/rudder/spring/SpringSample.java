package com.github.rudder.spring;

import com.github.rudder.client.ContaineredApplication;

import java.util.List;

public class SpringSample {

    public static void main(String[] args) throws Exception {
        final var container = new ContaineredApplication<>("Sasuke", "adoptopenjdk/openjdk11:x86_64-ubuntu-jdk-11.28", SpringApp.SpringRudderApp.class, List.of());
        container.stop();
        container.start();

        final var application = container.getApplication();

        final SomeEntityService repo = application.getService();
        final SomeEntityRepository repo1 = repo.getRepo();

        final SomeEntity save = repo.createSome("Test");

        final Long id = save.getId();

        final SomeEntity some = repo.findSomeEntityById(id);
        System.out.println(some.getTitle());

        final List<SomeEntity> all = repo1.findAll();
        final SomeEntity someEntity = all.get(0);

        System.out.println(someEntity.getTitle());

        container.stop();
    }


}
