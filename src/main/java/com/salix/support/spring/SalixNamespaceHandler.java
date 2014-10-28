package com.salix.support.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class SalixNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		registerBeanDefinitionParser("client", new SalixClientBeanDefinitionParser());
        registerBeanDefinitionParser("application", new SalixApplicationBeanDefinitionParser());
	}

}
