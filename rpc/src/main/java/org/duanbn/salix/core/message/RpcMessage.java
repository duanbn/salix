package org.duanbn.salix.core.message;

import java.util.Arrays;

public class RpcMessage extends Message {

	private static final long serialVersionUID = 1L;

	private String serviceName;

	private String methodName;

	private Class<?>[] paramTypes;
	private Object[] args;

	public RpcMessage() {
	}

	public RpcMessage(String serviceName, Object[] args) {
		this.serviceName = serviceName;
		this.args = args;
	}

	public String getMethodInfo() {
		StringBuilder info = new StringBuilder(methodName);
		for (Class<?> paramType : paramTypes) {
			info.append(paramType.getName());
		}
		return info.toString();
	}

	@Override
	public String toString() {
		return "RpcMessage [serviceName=" + serviceName + ", methodName=" + methodName + ", paramTypes="
				+ Arrays.toString(paramTypes) + ", args=" + Arrays.toString(args) + "]";
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Class<?>[] getParamTypes() {
		return paramTypes;
	}

	public void setParamTypes(Class<?>[] paramTypes) {
		this.paramTypes = paramTypes;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object... args) {
		this.args = args;
	}

}
