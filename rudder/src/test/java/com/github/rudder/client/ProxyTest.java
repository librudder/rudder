package com.github.rudder.client;


import com.github.rudder.RudderApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.function.Consumer;

@RunWith(JUnit4.class)
public class ProxyTest {

    @Test
    public void interfaceTest() throws Throwable {
        final Long someNum = 8855L;
        final var container = new ContaineredApplication<>("adoptopenjdk/openjdk11:x86_64-ubuntu-jdk-11.28", ProxyTestRudderApp.class, List.of("" + someNum));

        container.start();

        final ProxyTestRudderApp application = container.getApplication();

        // receiving interface implementation
        final ProxyTestInterface obj = application.obj();

        final Long resultOfMethodOfInterface = obj.callMyMethod();

        Assert.assertEquals(someNum, resultOfMethodOfInterface);

        final Long moreComplicatedMethodCall = application.handleSomeInterface(obj);

        Assert.assertEquals(someNum, moreComplicatedMethodCall);

        final Long localValue = 999L;

        final Long localObjectTransferedToMethodResult = application.handleSomeInterface(new ProxyTestInterface() {
            @Override
            public Long callMyMethod() {
                return localValue;
            }
        });

        Assert.assertEquals(localValue, localObjectTransferedToMethodResult);

        container.stop();
    }


    interface ProxyTestInterface {

        Long callMyMethod();

    }

    private static class ProxyTestApp {

        private final Long num;

        public ProxyTestApp(final String num) {
            this.num = Long.valueOf(num);
        }

        public static void main(String[] args) {

        }

        ProxyTestInterface obj() {
            final ProxyTestInterface obj = (ProxyTestInterface) Proxy.newProxyInstance(ProxyTestInterface.class.getClassLoader(),
                    new Class[]{ProxyTestInterface.class}, (proxy, method, args) -> num);
            return obj;
        }

        Long handleSomeInterface(final ProxyTestInterface proxyTestInterface) {
            return proxyTestInterface.callMyMethod();
        }

    }

    private static class ProxyTestRudderApp extends ProxyTestApp implements RudderApplication<ProxyTestApp> {

        private static Consumer<ProxyTestRudderApp> callback;

        public ProxyTestRudderApp(final String num) {
            super(num);
        }

        public static void setReadyCallback(final Consumer<ProxyTestRudderApp> callback) {
            ProxyTestRudderApp.callback = callback;
        }

        public static void main(String[] args) {
            ProxyTestApp.main(args);
            callback.accept(new ProxyTestRudderApp(args[0]));
        }

    }

}
