package org.duanbn.salix.server.processor;

import org.springframework.context.ApplicationContext;

public abstract class AbstractProcessor implements IProcessor {

	protected ApplicationContext springCtx;

	public void setSpringCtx(ApplicationContext springCtx) {
		this.springCtx = springCtx;
	}

}
