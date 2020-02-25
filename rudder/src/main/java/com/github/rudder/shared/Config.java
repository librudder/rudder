package com.github.rudder.shared;

import java.util.ArrayList;
import java.util.List;

public class Config {

	private static List<Class<?>> PRIMITIVES = new ArrayList<>();

	static {
		PRIMITIVES.add(CharSequence.class);
		PRIMITIVES.add(Number.class);
		PRIMITIVES.add(Boolean.class);
	}

	public static boolean isPrimitive(final Object object) {
		for (Class<?> primitiveClass : PRIMITIVES) {
			if (primitiveClass.isInstance(object)) {
				return true;
			}
		}
		return false;
	}

}
