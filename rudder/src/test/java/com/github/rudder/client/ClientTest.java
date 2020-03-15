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
        final var container = new ContaineredApplication<>("Itachi", "adoptopenjdk/openjdk11:x86_64-ubuntu-jdk-11.28", ClientTestRudderApp.class, List.of("" + someNum));

        container.start();

        final ClientTestRudderApp application = container.getApplication();

        // receiving interface implementation
        final ClientTestInterface obj = application.obj();

        final Long resultOfMethodOfInterface = obj.callMyMethod();

        Assert.assertEquals(someNum, resultOfMethodOfInterface);

        final Long moreComplicatedMethodCall = application.handleSomeInterface(obj);

        Assert.assertEquals(someNum, moreComplicatedMethodCall);

        final Long localValue = 322L;

        final Long localObjectTransferedToMethodResult = application.handleSomeInterface(new ClientTestInterface() {
            @Override
            public Long callMyMethod() {
                return localValue;
            }
        });

        Assert.assertEquals(localValue, localObjectTransferedToMethodResult);

        container.stop();
    }


    interface ClientTestInterface {

        Long callMyMethod();

    }

    private static class ClientTestApp {

        private final Long num;

        public ClientTestApp(final String num) {
            this.num = Long.valueOf(num);
        }

        public static void main(String[] args) {

        }

        ClientTestInterface obj() {
            return new ClientTestInterface() {
                @Override
                public Long callMyMethod() {
                    return num;
                }
            };
        }

        Long handleSomeInterface(final ClientTestInterface clientTestInterface) {
            return clientTestInterface.callMyMethod();
        }

    }

    private static class ClientTestRudderApp extends ClientTestApp implements RudderApplication<ClientTestApp> {

        private static Consumer<ClientTestRudderApp> callback;

        public ClientTestRudderApp(final String num) {
            super(num);
        }

        public static void setReadyCallback(final Consumer<ClientTestRudderApp> callback) {
            ClientTestRudderApp.callback = callback;
        }

        public static void main(String[] args) {
            ClientTestApp.main(args);
            callback.accept(new ClientTestRudderApp(args[0]));
        }

    }

}
