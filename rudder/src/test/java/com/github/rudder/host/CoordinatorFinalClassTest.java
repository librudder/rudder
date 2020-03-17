package com.github.rudder.host;

import com.github.rudder.RudderApplication;
import com.github.rudder.client.CoordinatorClient;
import com.github.rudder.client.Runner;
import com.github.rudder.shared.http.HttpApp;
import com.github.rudder.shared.http.MethodCallFailedException;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import retrofit2.Retrofit;

import java.util.function.Consumer;

@RunWith(JUnit4.class)
public class CoordinatorFinalClassTest {

    @After
    public void tearDown() throws Exception {
        Coordinator.objectStorage.clear();
        Coordinator.httpApp.stop();
    }

    @Test(expected = MethodCallFailedException.class)
    public void basicTest() throws Throwable {
        Coordinator.main(new String[]{FinalClassTestRudderApp.class.getName()});

        final Retrofit retrofit = Runner.createRetrofit("localhost", HttpApp.COORDINATOR_CONTROL_PORT);
        final CoordinatorClient coordinatorClient = retrofit.create(CoordinatorClient.class);

        final String objectIdReceivedByHTTP = coordinatorClient.hello(10_000).execute().body();

        final FinalClassTestRudderApp proxy = Runner.createProxy(coordinatorClient, Coordinator.objectStorage, objectIdReceivedByHTTP, FinalClassTestRudderApp.class);
        proxy.handleFinalClass(new FinalClass());
    }

    private static class FinalClassTestApp {

        public static void main(String[] args) {
        }

        void handleFinalClass(final FinalClass finalClassObject) {
            finalClassObject.num();
        }

    }

    private static final class FinalClass {

        int num() {
            return 0;
        }

    }

    private static class FinalClassTestRudderApp extends FinalClassTestApp implements RudderApplication<FinalClassTestApp> {

        private static Consumer<FinalClassTestRudderApp> callback;

        public static void setReadyCallback(final Consumer<FinalClassTestRudderApp> callback) {
            FinalClassTestRudderApp.callback = callback;
        }

        public static void main(String[] args) {
            FinalClassTestApp.main(args);
            final FinalClassTestRudderApp t = new FinalClassTestRudderApp();
            callback.accept(t);
        }

    }

}
