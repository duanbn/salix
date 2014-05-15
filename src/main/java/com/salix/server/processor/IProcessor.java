package com.salix.server.processor;

import org.springframework.context.ApplicationContext;

import com.salix.core.message.Message;

public interface IProcessor {

	public Message process(Message in) throws Throwable;
	
	public void setSpringCtx(ApplicationContext springCtx);

}
