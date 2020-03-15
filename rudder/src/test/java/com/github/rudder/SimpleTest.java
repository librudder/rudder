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
        final var container = new ContaineredApplication<>("Itachi", "adoptopenjdk/openjdk11:x86_64-ubuntu-jdk-11.28", SimpleTestRudderApp.class, List.of(testValue));

        container.start();

        final SimpleTestRudderApp application = container.getApplication();

        final int number = application.getNumber("451");
        Assert.assertEquals(451, number);

        final String anotherValue = "Another Value";
        final String expected = testValue + " " + anotherValue;
        Assert.assertEquals(expected, application.getValue(anotherValue));

        container.stop();
    }


    private static class SimpleTestApp {

        private static String value;

        public static void main(String[] args) {
            value = args[0];
        }

        int getNumber(final String number) {
            return Integer.parseInt(number);
        }

        public String getValue(final String anotherValue) {
            return value + " " + anotherValue;
        }

    }

    private static class SimpleTestRudderApp extends SimpleTestApp implements RudderApplication<SimpleTestApp> {

        private static Consumer<SimpleTestRudderApp> callback;

        public static void setReadyCallback(final Consumer<SimpleTestRudderApp> callback) {
            SimpleTestRudderApp.callback = callback;
        }

        public static void main(String[] args) {
            SimpleTestApp.main(args);
            callback.accept(new SimpleTestRudderApp());
        }

    }

}
