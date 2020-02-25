package com.github.rudder.shared;

import com.google.gson.*;

import java.lang.reflect.Type;

import static com.github.rudder.shared.MethodArguments.*;

public class GsonUtil {

	public static Gson gson;

	static {
		gson = new GsonBuilder()
				.registerTypeAdapter(MethodArgument.class, (JsonDeserializer<MethodArgument>) (jsonElement, type, jsonDeserializationContext) -> {
					final JsonObject asJsonObject = jsonElement.getAsJsonObject();
					final JsonElement value = asJsonObject.get("value");

					final JsonElement objectClassJsonElement = asJsonObject.get("objectClass");
					final String objectClass = objectClassJsonElement != null ? objectClassJsonElement.getAsString() : null;

					final JsonElement isPrimitiveJsonElement = asJsonObject.get("isPrimitive");
					final boolean isPrimitive = isPrimitiveJsonElement.getAsBoolean();
					final JsonElement objectIdJsonElement = asJsonObject.get("objectId");
					final String objectId = objectIdJsonElement != null ? objectIdJsonElement.getAsString() : null;

					final String asString = value != null ? value.getAsString() : null;
					final Object redValue;
					if (asString == null) {
						redValue = null;
					} else {
						try {
							redValue = gson.fromJson(asString, Class.forName(objectClass));
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
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
				.create();
	}

}
