package com.github.rudder.shared.http;

import com.github.rudder.client.Runner;
import com.github.rudder.shared.*;
import com.github.rudder.shared.gson.GsonUtil;
import com.github.rudder.shared.http.api.MethodArguments;
import com.github.rudder.shared.http.api.MethodCallResult;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class InvocationController implements HttpApp.HandlerDefinition {

    private final ObjectStorage objects;
    private InvocationClient client;

    public InvocationController(final ObjectStorage objects, final InvocationClient client) {
        this.objects = objects;
        this.client = client;
    }

    public InvocationController(final ObjectStorage objects) {
        this.objects = objects;
    }

    @NotNull
    public MethodCallResult invokeMethod(final String name,
                                         final Object obj,
                                         final Object[] args,
                                         final ObjectStorage objects) throws IllegalAccessException, InvocationTargetException {
        Class[] argTypes = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);

        final Method declaredMethod = Util.findMethod(obj.getClass(), name, argTypes);

        final boolean isVoid = Void.TYPE.equals(declaredMethod.getReturnType());

        final Object result = declaredMethod.invoke(obj, args);

        if (isVoid) {
            final MethodCallResult methodCallResult = new MethodCallResult();
            methodCallResult.setVoid(true);
            return methodCallResult;
        }

        MethodCallResult res;
        if (RudderConfig.isPrimitive(result)) {
            res = new MethodCallResult();
            res.setObjectClass(result.getClass().getName());
            res.setResult(result);
            res.setPrimitive(true);
        } else {
            final String objectId = objects.put(result);

            res = new MethodCallResult();
            res.setObjectClass(result.getClass().getName());
            res.setObjectId(objectId);
            res.setPrimitive(false);
        }
        return res;
    }

    @NotNull
    public Object[] getArguments(final String body, final ObjectStorage objects) {
        final Object[] args;
        final MethodArguments methodArguments = GsonUtil.gson.fromJson(body, MethodArguments.class);
        args = methodArguments.getArguments().stream().map(methodArgument -> {
            if (methodArgument.isPrimitive()) {
                return methodArgument.getValue();
            }

            final String objectId = methodArgument.getObjectId();
            final String objectClass = methodArgument.getObjectClass();
            if (!Util.isEmpty(objectClass)) {
                try {
                    return Runner.createProxy(client, objects, objectId, Class.forName(objectClass));
                } catch (ClassNotFoundException e) {
                    // todo: rethrow maybe (need rethink, for sure)
                    e.printStackTrace();
                    return null;
                }
            }

            return objects.get(objectId);
        }).toArray();
        return args;
    }

    public void setClient(final InvocationClient client) {
        this.client = client;
    }

    @Override
    public String path() {
        return "/invoke";
    }

    @Override
    public Handler handler() {
        return context -> {
            try {
                final String name = context.req.getParameter("methodName");
                final String calleeObjectId = context.req.getParameter("objectId");

                final Object obj = objects.get(calleeObjectId);

                final String body = context.body();

                Object[] args = new Object[0];

                if (!Util.isEmpty(body)) {
                    args = getArguments(body, objects);
                }

                MethodCallResult res = invokeMethod(name, obj, args, objects);
                context.result(GsonUtil.gson.toJson(res));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        };
    }
}
