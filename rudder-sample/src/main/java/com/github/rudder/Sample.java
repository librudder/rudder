package com.github.rudder;

import com.github.rudder.client.ContaineredApplication;

import java.io.File;
import java.util.List;
import java.util.function.Function;

public class Sample {

    public static void main(String[] args) throws Exception {
        final Class<TestApplication> testApplicationClass = TestApplication.class;
        final ContaineredApplication<TestApplication> container = new ContaineredApplication<>("adoptopenjdk/openjdk11:x86_64-ubuntu-jdk-11.28", testApplicationClass, List.of("raz", "dva"));
        container.stop();
        container.start();

        final TestApplication application = container.getApplication();

        final TestApplication.ClassOne classOne = application.getClassOneObject();

        final String exec = classOne.exec();
        System.out.println(exec);

        final TestApplication.ClassTwo classTwo = classOne.methodWithPrimitive();

        final String foobar = classTwo.foobar("ppx");

        System.out.println(foobar);
        System.out.println(classTwo.isSamePupa(classOne));
        final File someFile = classTwo.getSomeFile("/Users/danilov/");
        System.out.println(someFile.getName());

        classTwo.setSomeFunc(new Function<String, String>() {
            @Override
            public String apply(final String s) {
                System.out.println("called back");
                return "New phone, " + s;
            }
        });

        classTwo.call("who dis?");

        System.out.println(classTwo.getValue());

        container.stop();
    }

}
