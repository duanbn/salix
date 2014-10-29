package com.salix.support.spring;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class SalixZookeeperBeanDefinitionParser extends SalixBeanDefinitionParser {

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		String zkUrl = element.getAttribute("address");

		setMeta("zkUrl", zkUrl);

		RootBeanDefinition beanDefinition = new RootBeanDefinition();

		return beanDefinition;
	}

}
