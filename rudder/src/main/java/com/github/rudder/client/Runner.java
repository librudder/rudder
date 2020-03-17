package com.github.rudder.client;

import com.github.rudder.shared.*;
import com.github.rudder.shared.gson.GsonUtil;
import com.github.rudder.shared.http.InvocationClient;
import com.github.rudder.shared.http.MethodCallFailedException;
import com.github.rudder.shared.http.api.MethodArguments;
import com.github.rudder.shared.http.api.MethodCallResult;
import net.sf.cglib.proxy.*;
import okhttp3.OkHttpClient;
import org.objenesis.ObjenesisStd;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Runner {

    public static final ObjenesisStd OBJENESIS_STD = new ObjenesisStd();


    public static Retrofit createRetrofit(final String host, final int port) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .connectTimeout(120, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(String.format("http://%s:%d", host, port))
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(GsonUtil.gson))
                .build();
    }

    public static <T> T createProxy(final InvocationClient coordinatorClient,
                                    final ObjectStorage objectStorage,
                                    final String objectId, final Class<T> clazz) {
        Enhancer enhancer = new Enhancer() {
            @Override
            protected void filterConstructors(final Class sc, final List constructors) {

            }
        };

        if (!clazz.isInterface()) {
            enhancer.setSuperclass(clazz);
            enhancer.setInterfaces(new Class[]{__ObjectIdInterface.class});
        } else {
            enhancer.setInterfaces(new Class[]{clazz, __ObjectIdInterface.class});
        }

        enhancer.setCallbackType(MethodInterceptor.class);

        final Class<T> newClass = enhancer.createClass();
        final T t = OBJENESIS_STD.newInstance(newClass);

        final Factory factory = (Factory) t;
        factory.setCallbacks(new Callback[]{
                (MethodInterceptor) (Object obj, Method method, Object[] arguments, MethodProxy proxy) -> {
                    final String methodName = method.getName();
                    final Class<?>[] parameterTypes = method.getParameterTypes();

                    if (methodName.equals("__getObjectId")) {
                        return objectId;
                    }

                    final Response<MethodCallResult> response;

                    if (arguments.length > 0) {
                        final MethodArguments methodArguments = new MethodArguments();
                        for (int i = 0; i < arguments.length; i++) {
                            final Object argument = arguments[i];
                            if (RudderConfig.isPrimitive(argument)) {
                                methodArguments.addPrimitive(argument);
                            } else {
                                final String parameterObjectId = getObjectId(argument);
                                if (!(Util.isEmpty(parameterObjectId))) {
                                    methodArguments.addNonPrimitive(parameterObjectId);
                                } else {
                                    // looks like it's a local object, lets transfer a link to it
                                    final String id = objectStorage.put(argument);
                                    Class<?> aClass = argument.getClass();
                                    if (Modifier.isFinal(aClass.getModifiers())) {
                                        // we can't create proxy of final class object,
                                        // maybe parameter's class isn't final
                                        aClass = parameterTypes[i];
                                    }
                                    methodArguments.addLocalObject(id, aClass);
                                }
                            }
                        }

                        response = coordinatorClient.invoke(objectId, methodName, methodArguments).execute();
                    } else {
                        response = coordinatorClient.invoke(objectId, methodName).execute();
                    }

                    if (!response.isSuccessful()) {
                        final var exception = GsonUtil.gson.fromJson(response.errorBody().string(), RuntimeException.class);
                        throw new MethodCallFailedException("Failed to call method", exception);
                    }

                    final MethodCallResult callResult = response.body();
                    if (callResult.isVoid()) {
                        return null;
                    }

                    if (callResult.isPrimitive()) {
                        return callResult.getResult();
                    } else {
                        Class<?> aClass;
                        try {
                            aClass = Class.forName(callResult.getObjectClass());
                            if (Modifier.isFinal(aClass.getModifiers())) {
                                // class is final, we can't subclass it, try using method's return type instead
                                aClass = method.getReturnType();
                            }
                        } catch (ClassNotFoundException e) {
                            // this may be a proxy, use a method's return type then
                            aClass = method.getReturnType();
                        }
                        return createProxy(coordinatorClient, objectStorage, callResult.getObjectId(), aClass);
                    }
                }
        });
        return t;
    }

    private static String getObjectId(final Object object) {
        if (object instanceof __ObjectIdInterface) {
            return ((__ObjectIdInterface) object).__getObjectId();
        }
        return null;
    }

    public interface __ObjectIdInterface {
        String __getObjectId();
    }

}
