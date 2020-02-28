package com.github.rudder;

import com.github.rudder.client.ContaineredApplication;

import java.io.File;
import java.util.List;
import java.util.function.Function;

public class Sample {

    public static void main(String[] args) throws Exception {
        final Class<TestApplication> testApplicationClass = TestApplication.class;
        final ContaineredApplication<TestApplication> container = new ContaineredApplication<>("Sasuke", "adoptopenjdk/openjdk11:x86_64-ubuntu-jdk-11.28", testApplicationClass, List.of("raz", "dva"));
        container.stop();
        container.start();

        final TestApplication application = container.getApplication();

        final TestApplication.Pupa pupa = application.getPupa();

        final String exec = pupa.exec();
        System.out.println(exec);

        final TestApplication.Lupa lupa = pupa.methodWithPrimitive();

        final String foobar = lupa.foobar("ppx");

        System.out.println(foobar);
        System.out.println(lupa.isSamePupa(pupa));
        final File someFile = lupa.getSomeFile("/Users/danilov/");
        System.out.println(someFile.getName());

        lupa.setHoller(new Function<String, String>() {
            @Override
            public String apply(final String s) {
                System.out.println("called back");
                return "New phone, " + s;
            }
        });

        lupa.call("who dis?");

        System.out.println(lupa.getValue());

        container.stop();
    }

}
