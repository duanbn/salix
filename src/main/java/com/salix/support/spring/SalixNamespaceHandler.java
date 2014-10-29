package com.salix.support.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class SalixNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		registerBeanDefinitionParser("zookeeper", new SalixZookeeperBeanDefinitionParser());

		registerBeanDefinitionParser("stub", new SalixStubBeanDefinitionParser());

		registerBeanDefinitionParser("application", new SalixApplicationBeanDefinitionParser());
	}

}
