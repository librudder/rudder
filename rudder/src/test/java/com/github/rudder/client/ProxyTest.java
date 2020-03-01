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
        final var container = new ContaineredApplication<>("Itachi", "adoptopenjdk/openjdk11:x86_64-ubuntu-jdk-11.28", ProxyTestRudderProxyTestApp.class, List.of("" + someNum));
        container.stop();
        container.start();

        final ProxyTestRudderProxyTestApp application = container.getApplication();

        // receiving interface implementation
        final ProxyMeInterface obj = application.obj();

        final Long resultOfMethodOfInterface = obj.callMyMethod();

        Assert.assertEquals(someNum, resultOfMethodOfInterface);

        final Long moreComplicatedMethodCall = application.handleSomeInterface(obj);

        Assert.assertEquals(someNum, moreComplicatedMethodCall);

        final Long localValue = 999L;

        final Long localObjectTransferedToMethodResult = application.handleSomeInterface(new ProxyMeInterface() {
            @Override
            public Long callMyMethod() {
                return localValue;
            }
        });

        Assert.assertEquals(localValue, localObjectTransferedToMethodResult);

        container.stop();
    }


    interface ProxyMeInterface {

        Long callMyMethod();

    }

    static class ProxyTestApp {

        private final Long num;

        public static void main(String[] args) {

        }

        public ProxyTestApp(final String num) {
            this.num = Long.valueOf(num);
        }

        ProxyMeInterface obj() {
            final ProxyMeInterface obj = (ProxyMeInterface) Proxy.newProxyInstance(ProxyMeInterface.class.getClassLoader(),
                    new Class[]{ProxyMeInterface.class}, (proxy, method, args) -> num);
            return obj;
        }

        Long handleSomeInterface(final ProxyMeInterface proxyMeInterface) {
            return proxyMeInterface.callMyMethod();
        }

    }

    static class ProxyTestRudderProxyTestApp extends ProxyTestApp implements RudderApplication<ProxyTestApp> {

        private static Consumer<ProxyTestRudderProxyTestApp> callback;

        public ProxyTestRudderProxyTestApp(final String num) {
            super(num);
        }

        public static void setReadyCallback(final Consumer<ProxyTestRudderProxyTestApp> callback) {
            ProxyTestRudderProxyTestApp.callback = callback;
        }

        public static void main(String[] args) {
            ProxyTestApp.main(args);
            callback.accept(new ProxyTestRudderProxyTestApp(args[0]));
        }

    }

}
