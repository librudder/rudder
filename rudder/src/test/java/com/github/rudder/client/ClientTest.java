package com.github.rudder.client;

import com.github.rudder.RudderApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.function.Consumer;

@RunWith(JUnit4.class)
public class ClientTest {


    @Test
    public void interfaceTest() throws Throwable {
        final Long someNum = 1337L;
        final var container = new ContaineredApplication<>("Itachi", "adoptopenjdk/openjdk11:x86_64-ubuntu-jdk-11.28", ClientTest.RudderApp.class, List.of("" + someNum));
        container.stop();
        container.start();

        final ClientTest.RudderApp application = container.getApplication();

        // receiving interface implementation
        final SomeInterface obj = application.obj();

        final Long resultOfMethodOfInterface = obj.callMyMethod();

        Assert.assertEquals(someNum, resultOfMethodOfInterface);

        final Long moreComplicatedMethodCall = application.handleSomeInterface(obj);

        Assert.assertEquals(someNum, moreComplicatedMethodCall);

        final Long localValue = 322L;

        final Long localObjectTransferedToMethodResult = application.handleSomeInterface(new SomeInterface() {
            @Override
            public Long callMyMethod() {
                return localValue;
            }
        });

        Assert.assertEquals(localValue, localObjectTransferedToMethodResult);

        container.stop();
    }


    interface SomeInterface {

        Long callMyMethod();

    }

    static class App {

        private final Long num;

        public static void main(String[] args) {

        }

        public App(final String num) {
            this.num = Long.valueOf(num);
        }

        SomeInterface obj() {
            return new SomeInterface() {
                @Override
                public Long callMyMethod() {
                    return num;
                }
            };
        }

        Long handleSomeInterface(final SomeInterface someInterface) {
            return someInterface.callMyMethod();
        }

    }

    static class RudderApp extends ClientTest.App implements RudderApplication<ClientTest.App> {

        private static Consumer<ClientTest.RudderApp> callback;

        public RudderApp(final String num) {
            super(num);
        }

        public static void setReadyCallback(final Consumer<ClientTest.RudderApp> callback) {
            ClientTest.RudderApp.callback = callback;
        }

        public static void main(String[] args) {
            ClientTest.App.main(args);
            callback.accept(new ClientTest.RudderApp(args[0]));
        }

    }

}
