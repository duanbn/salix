package com.salix.server.beans;

import java.io.Serializable;
import java.util.List;

public class SalixServiceBean implements Serializable {

    private static final long serialVersionUID = 1L;

	private String serviceName;

	private List<String> methods;

	public SalixServiceBean(String serviceName, List<String> methods) {
		this.serviceName = serviceName;
		this.methods = methods;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public List<String> getMethods() {
		return methods;
	}

	public void setMethods(List<String> methods) {
		this.methods = methods;
	}

}
