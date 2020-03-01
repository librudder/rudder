package com.github.rudder.host;

import com.github.rudder.RudderApplication;
import com.github.rudder.client.Runner;
import com.github.rudder.shared.*;
import retrofit2.Retrofit;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Consumer;

public class Coordinator {

    public static final int COORDINATOR_CONTROL_PORT = 9513;

    static ObjectStorage objectStorage = new ObjectStorage();

    public static void main(String[] args) throws Throwable {
        final String className = args[0];
        final String[] appArgs = Arrays.copyOfRange(args, 1, args.length);

        final Class<?> appClass = Class.forName(className);
        if (!RudderApplication.class.isAssignableFrom(appClass)) {
            System.err.println("WARNING! This app can't notify that it is running");
        }

        final Method setReadyCallback = Util.findMethod(appClass, "setReadyCallback", new Class[]{Consumer.class});

        setReadyCallback.invoke(null, (Consumer<Object>) o -> {
            final String mainObjectId = objectStorage.put(o);

            final HttpApp httpApp = new HttpApp(COORDINATOR_CONTROL_PORT);

            final InvocationController invocationController = new InvocationController(objectStorage);
            httpApp.add(new HelloController(mainObjectId, port -> {
                final Retrofit retrofit = Runner.createRetrofit("rudder.host", port);
                final InvocationClient invocationClient = retrofit.create(InvocationClient.class);
                invocationController.setClient(invocationClient);
            }));

            httpApp.add(invocationController);

            httpApp.start();
        });

        final Method main = Util.findMethod(appClass, "main", new Class[]{String[].class});

        main.invoke(null, (Object) appArgs);
    }

}
