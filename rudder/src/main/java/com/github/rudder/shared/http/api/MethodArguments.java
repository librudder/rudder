package com.github.rudder.shared.http.api;

import java.util.ArrayList;
import java.util.List;

public class MethodArguments {

    private final List<MethodArgument> arguments = new ArrayList<>();

    public MethodArguments() {
    }

    public void addPrimitive(final Object object) {
        arguments.add(new MethodArgument(object, null, object.getClass().getName(), true));
    }

    public List<MethodArgument> getArguments() {
        return arguments;
    }

    public void addNonPrimitive(final String objectId) {
        arguments.add(new MethodArgument(null, objectId, null, false));
    }

    public void addLocalObject(final String objectId, final Object object) {
        arguments.add(new MethodArgument(null, objectId, object.getClass().getName(), false));
    }

    public static class MethodArgument {

        private Object value;

        private boolean isPrimitive;

        private String objectId;

        private String objectClass;

        public MethodArgument() {
        }

        public MethodArgument(final Object value,
                              final String objectId,
                              final String objectClass,
                              final boolean isPrimitive) {
            this.value = value;
            this.objectId = objectId;
            this.objectClass = objectClass;
            this.isPrimitive = isPrimitive;
        }

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(final String objectId) {
            this.objectId = objectId;
        }

        public boolean isPrimitive() {
            return isPrimitive;
        }

        public void setPrimitive(final boolean primitive) {
            isPrimitive = primitive;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(final Object value) {
            this.value = value;
        }

        public String getObjectClass() {
            return objectClass;
        }

        public void setObjectClass(final String objectClass) {
            this.objectClass = objectClass;
        }
    }

}
