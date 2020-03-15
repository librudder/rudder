package com.github.rudder.host;

import com.github.rudder.RudderApplication;
import com.github.rudder.client.CoordinatorClient;
import com.github.rudder.client.Runner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import retrofit2.Retrofit;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@RunWith(JUnit4.class)
public class CoordinatorTest {

    @Test
    public void basicTest() throws Throwable {
        final String foo = "foo";
        final String bar = "bar";
        Coordinator.main(new String[]{CoordinatorTestRudderApp.class.getName(), foo, bar});
        final List<String> keys = new ArrayList<>(Coordinator.objectStorage.keySet());

        Assert.assertEquals(1, keys.size());

        final String mainObjectKey = keys.get(0);
        final Object rudderAppObject = Coordinator.objectStorage.get(mainObjectKey);

        Assert.assertNotNull(rudderAppObject);
        Assert.assertEquals(rudderAppObject.getClass(), CoordinatorTestRudderApp.class);

        CoordinatorTestRudderApp rudderApp = (CoordinatorTestRudderApp) rudderAppObject;

        Assert.assertEquals(foo, rudderApp.val1);
        Assert.assertEquals(bar, rudderApp.val2);

        final Retrofit retrofit = Runner.createRetrofit("localhost", Coordinator.COORDINATOR_CONTROL_PORT);
        final CoordinatorClient coordinatorClient = retrofit.create(CoordinatorClient.class);

        final String objectIdReceivedByHTTP = coordinatorClient.hello(10_000).execute().body();

        Assert.assertEquals(mainObjectKey, objectIdReceivedByHTTP);
    }

    @Test(expected = IllegalApplicationException.class)
    public void testNotRudderApplication() throws Throwable {
        Coordinator.main(new String[]{CoordinatorTestApp.class.getName(), "don't", "care"});
        Assert.fail("This must fail as it's not a Rudder Application");
    }

    private static class CoordinatorTestApp {

        String val1;

        public CoordinatorTestApp(final String val1) {
            this.val1 = val1;
        }

        public static void main(String[] args) {
        }

    }

    private static class CoordinatorTestRudderApp extends CoordinatorTestApp implements RudderApplication<CoordinatorTestApp> {

        private static Consumer<CoordinatorTestRudderApp> callback;
        String val2;

        public CoordinatorTestRudderApp(final String val1) {
            super(val1);
        }

        public static void setReadyCallback(final Consumer<CoordinatorTestRudderApp> callback) {
            CoordinatorTestRudderApp.callback = callback;
        }

        public static void main(String[] args) {
            CoordinatorTestApp.main(args);
            final CoordinatorTestRudderApp t = new CoordinatorTestRudderApp(args[0]);
            t.val2 = args[1];
            callback.accept(t);
        }

    }

}
