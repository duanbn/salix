package org.duanbn.salix.core.message;

import java.io.Serializable;

public class Message implements Serializable {

	protected Object body;

	public void setBody(Object body) {
		this.body = body;
	}

	public Object getBody() {
		return this.body;
	}
}
