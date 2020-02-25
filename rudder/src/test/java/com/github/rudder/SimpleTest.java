package com.github.rudder;

import com.github.rudder.client.ContaineredApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.function.Consumer;

@RunWith(JUnit4.class)
public class SimpleTest {

    @Test
    public void test() throws Exception {
        final String testValue = "Test Value";
        final var container = new ContaineredApplication<>("Itachi", "adoptopenjdk/openjdk11:x86_64-ubuntu-jdk-11.28", RudderApp.class, List.of(testValue));
        container.stop();
        container.start();

        final RudderApp application = container.getApplication();

        final int number = application.getNumber("451");
        Assert.assertEquals(451, number);

        Assert.assertEquals(testValue, application.getValue());

        container.stop();
    }


    static class App {

        private static String value;

        public static void main(String[] args) {
            value = args[0];
        }

        int getNumber(final String number) {
            return Integer.parseInt(number);
        }

        public String getValue() {
            return value;
        }

    }

    static class RudderApp extends App implements RudderApplication<App> {

        private static Consumer<RudderApp> callback;

        public static void setReadyCallback(final Consumer<RudderApp> callback) {
            RudderApp.callback = callback;
        }

        public static void main(String[] args) {
            App.main(args);
            callback.accept(new RudderApp());
        }

    }

}
