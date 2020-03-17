package com.github.rudder.client;


import com.github.rudder.RudderApplication;
import com.github.rudder.shared.http.MethodCallFailedException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.function.Consumer;

@RunWith(JUnit4.class)
public class FinalClassTest {

    @Test
    public void interfaceOfFinalClassTest() throws Throwable {
        final int someNum = 1337;
        final var container = new ContaineredApplication<>("adoptopenjdk/openjdk11:x86_64-ubuntu-jdk-11.28", FinalClassTestRudderApp.class, List.of("" + someNum));

        container.start();

        final FinalClassTestRudderApp application = container.getApplication();

        final FinalClassInterface obj = application.obj();

        final int numFromContainer = obj.num();

        Assert.assertEquals(someNum, numFromContainer);

        final int numLocal = application.handleFinalClassInterface(new FinalClass());

        Assert.assertEquals(someNum, numLocal);
    }

    @Test(expected = MethodCallFailedException.class)
    public void finalClassFailTest() throws Throwable {
        final int someNum = 1337;
        final var container = new ContaineredApplication<>("adoptopenjdk/openjdk11:x86_64-ubuntu-jdk-11.28", FinalClassTestRudderApp.class, List.of("" + someNum));

        container.start();

        final FinalClassTestRudderApp application = container.getApplication();
        final int numLocal = application.handleFinalClass(new FinalClass());
        Assert.fail();
    }


    public interface FinalClassInterface {
        int num();
    }

    public static final class FinalClass implements FinalClassInterface {

        @Override
        public int num() {
            return 1337;
        }

    }

    private static class FinalClassTestApp {

        int num;

        public FinalClassTestApp(final int num) {
            this.num = num;
        }

        FinalClassInterface obj() {
            return new FinalClass();
        }

        int handleFinalClassInterface(final FinalClassInterface obj) {
            return obj.num();
        }

        int handleFinalClass(final FinalClassInterface obj) {
            return obj.num();
        }

        int handleFinalClass(final FinalClass obj) {
            return obj.num() + 1;
        }

        public static void main(String[] args) {

        }

    }

    private static class FinalClassTestRudderApp extends FinalClassTestApp implements RudderApplication<FinalClassTestApp> {

        private static Consumer<FinalClassTestRudderApp> callback;

        public FinalClassTestRudderApp(final int num) {
            super(num);
        }

        public static void setReadyCallback(final Consumer<FinalClassTestRudderApp> callback) {
            FinalClassTestRudderApp.callback = callback;
        }

        public static void main(String[] args) {
            FinalClassTestApp.main(args);
            callback.accept(new FinalClassTestRudderApp(Integer.parseInt(args[0])));
        }

    }

}
