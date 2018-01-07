package org.duanbn.salix.server.processor;

import org.duanbn.salix.core.message.Message;
import org.springframework.context.ApplicationContext;

public interface IProcessor {

	public Message process(Message in) throws Throwable;
	
	public void setSpringCtx(ApplicationContext springCtx);

}
