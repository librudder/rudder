package com.github.rudder.client;

import com.github.rudder.shared.*;
import net.sf.cglib.proxy.*;
import okhttp3.OkHttpClient;
import org.objenesis.ObjenesisStd;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.lang.reflect.Method;
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

                    if (methodName.equals("__getObjectId")) {
                        return objectId;
                    }

                    final Response<MethodCallResult> response;

                    if (arguments.length > 0) {
                        final MethodArguments methodArguments = new MethodArguments();
                        for (final Object argument : arguments) {
                            if (Config.isPrimitive(argument)) {
                                methodArguments.addPrimitive(argument);
                            } else {
                                final String parameterObjectId = getObjectId(argument);
                                if (!(Util.isEmpty(parameterObjectId))) {
                                    methodArguments.addNonPrimitive(parameterObjectId);
                                } else {
                                    // looks like it's a local object, lets transfer a link to it
                                    final String id = objectStorage.put(argument);
                                    methodArguments.addLocalObject(id, argument);
                                }
                            }
                        }

                        response = coordinatorClient.invoke(objectId, methodName, methodArguments).execute();
                    } else {
                        response = coordinatorClient.invoke(objectId, methodName).execute();
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
