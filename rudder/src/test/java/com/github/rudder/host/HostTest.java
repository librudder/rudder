package com.github.rudder.host;

import com.github.rudder.RudderApplication;
import com.github.rudder.client.ContaineredApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@RunWith(JUnit4.class)
public class HostTest {

    @Test
    public void test() throws Throwable {
        final String foo = "foo";
        final String bar = "bar";
        Coordinator.main(new String[]{RudderApp.class.getName(), foo, bar});
        final List<String> keys = new ArrayList<>(Coordinator.objectStorage.keySet());

        Assert.assertEquals(1, keys.size());

        final String mainObjectKey = keys.get(0);
        final Object rudderAppObject = Coordinator.objectStorage.get(mainObjectKey);

        Assert.assertNotNull(rudderAppObject);
        Assert.assertEquals(rudderAppObject.getClass(), RudderApp.class);

        RudderApp rudderApp = (RudderApp) rudderAppObject;

        Assert.assertEquals(foo, rudderApp.val1);
        Assert.assertEquals(bar, rudderApp.val2);
    }

    private static class App {

        String val1;

        public App(final String val1) {
            this.val1 = val1;
        }

        public static void main(String[] args) {
        }

    }

    private static class RudderApp extends HostTest.App implements RudderApplication<HostTest.App> {

        String val2;

        private static Consumer<HostTest.RudderApp> callback;

        public RudderApp(final String val1) {
            super(val1);
        }

        public static void setReadyCallback(final Consumer<HostTest.RudderApp> callback) {
            HostTest.RudderApp.callback = callback;
        }

        public static void main(String[] args) {
            HostTest.App.main(args);
            final RudderApp t = new RudderApp(args[0]);
            t.val2 = args[1];
            callback.accept(t);
        }

    }

}
