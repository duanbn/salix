package com.salix.support.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import com.salix.client.SalixFactoryBean;
import com.salix.client.connection.ConnectionPool;

public class SalixNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("cp", new SalixBeanDefinitionParser(ConnectionPool.class));
		registerBeanDefinitionParser("service", new SalixBeanDefinitionParser(SalixFactoryBean.class));
	}

}
