package com.salix.support.spring;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.salix.server.Bootstrap;

public class SalixApplicationBeanDefinitionParser implements BeanDefinitionParser {

	public static final Logger LOG = Logger.getLogger(SalixClientBeanDefinitionParser.class);

	public BeanDefinition parse(Element element, ParserContext parserContext) {

		String name = element.getAttribute("name");
		int port = Integer.parseInt(element.getAttribute("port"));
		String zkHost = element.getAttribute("zkHost");

		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setBeanClass(Bootstrap.class);
		beanDefinition.setLazyInit(false);
		beanDefinition.getPropertyValues().addPropertyValue("name", name);
		beanDefinition.getPropertyValues().addPropertyValue("port", port);
		beanDefinition.getPropertyValues().addPropertyValue("zkHost", zkHost);
		beanDefinition.setInitMethodName("startup");

		parserContext.getRegistry().registerBeanDefinition(name, beanDefinition);

		return beanDefinition;
	}
}
