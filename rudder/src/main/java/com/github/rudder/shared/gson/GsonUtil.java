package com.github.rudder.shared.gson;

import com.github.rudder.shared.http.api.MethodCallResult;
import com.google.gson.*;

import static com.github.rudder.shared.http.api.MethodArguments.MethodArgument;

public class GsonUtil {

    public static Gson gson;

    static {
        gson = new GsonBuilder()
                .registerTypeAdapter(MethodArgument.class, (JsonDeserializer<MethodArgument>) (jsonElement, type, jsonDeserializationContext) -> {
                    final JsonObject asJsonObject = jsonElement.getAsJsonObject();

                    final JsonElement objectClassJsonElement = asJsonObject.get("objectClass");
                    final String objectClass = objectClassJsonElement != null ? objectClassJsonElement.getAsString() : null;

                    final JsonElement isPrimitiveJsonElement = asJsonObject.get("isPrimitive");
                    final boolean isPrimitive = isPrimitiveJsonElement.getAsBoolean();
                    final JsonElement objectIdJsonElement = asJsonObject.get("objectId");
                    final String objectId = objectIdJsonElement != null ? objectIdJsonElement.getAsString() : null;

                    final JsonElement value = asJsonObject.get("value");
                    final Object redValue;
                    if (value == null) {
                        redValue = null;
                    } else {
                        try {
                            redValue = gson.fromJson(value, Class.forName(objectClass));
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    final MethodArgument methodArgument = new MethodArgument();
                    methodArgument.setValue(redValue);
                    methodArgument.setObjectClass(objectClass);
                    methodArgument.setObjectId(objectId);
                    methodArgument.setPrimitive(isPrimitive);
                    return methodArgument;
                })
                .registerTypeAdapter(MethodCallResult.class, (JsonDeserializer<MethodCallResult>) (jsonElement, type, jsonDeserializationContext) -> {
                    final JsonObject asJsonObject = jsonElement.getAsJsonObject();

                    final JsonElement objectClassJsonElement = asJsonObject.get("objectClass");
                    final String objectClass = objectClassJsonElement != null ? objectClassJsonElement.getAsString() : null;

                    final JsonElement isPrimitiveJsonElement = asJsonObject.get("isPrimitive");
                    final boolean isPrimitive = isPrimitiveJsonElement.getAsBoolean();
                    final JsonElement objectIdJsonElement = asJsonObject.get("objectId");
                    final String objectId = objectIdJsonElement != null ? objectIdJsonElement.getAsString() : null;

                    final JsonElement value = asJsonObject.get("result");

                    final Object redValue;
                    if (value == null) {
                        redValue = null;
                    } else {
                        try {
                            redValue = gson.fromJson(value, Class.forName(objectClass));
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    final boolean isVoid = asJsonObject.get("isVoid").getAsBoolean();

                    final MethodCallResult methodCallResult = new MethodCallResult();
                    methodCallResult.setResult(redValue);
                    methodCallResult.setObjectClass(objectClass);
                    methodCallResult.setObjectId(objectId);
                    methodCallResult.setPrimitive(isPrimitive);
                    methodCallResult.setVoid(isVoid);
                    return methodCallResult;
                })
                .create();
    }

}
