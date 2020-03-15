package com.github.rudder.shared;

public class MethodCallResult {

    private boolean isPrimitive;

    private Object result;

    private String objectId;

    private String objectClass;

    private boolean isVoid;

    public MethodCallResult() {
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }

    public void setPrimitive(final boolean primitive) {
        isPrimitive = primitive;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(final Object result) {
        this.result = result;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(final String objectId) {
        this.objectId = objectId;
    }

    public String getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(final String objectClass) {
        this.objectClass = objectClass;
    }

    public boolean isVoid() {
        return isVoid;
    }

    public void setVoid(final boolean aVoid) {
        isVoid = aVoid;
    }
}
