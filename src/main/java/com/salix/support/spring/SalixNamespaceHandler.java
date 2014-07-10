package com.salix.support.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class SalixNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("client", new SalixBeanDefinitionParser());
	}

}
