package com.github.rudder.shared;

public class MethodCallResult {

	private boolean isPrimitive;

	private Object result;

	private String objectId;

	private String objectClass;

	private boolean isVoid;

	public MethodCallResult(final boolean isVoid) {
		this.isVoid = isVoid;
		this.objectClass = null;
		this.objectId = null;
		this.isPrimitive = false;
	}

	public MethodCallResult(final Object result) {
		this.result = result;
		this.objectId = null;
		this.objectClass = null;
		this.isPrimitive = true;
	}

	public MethodCallResult(final String objectId, final String objectClass) {
		this.objectId = objectId;
		this.objectClass = objectClass;
		this.result = null;
		this.isPrimitive = false;
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
}
