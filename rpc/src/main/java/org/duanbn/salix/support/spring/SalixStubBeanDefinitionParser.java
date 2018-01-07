package org.duanbn.salix.support.spring;

import org.apache.log4j.Logger;
import org.duanbn.salix.client.SalixFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class SalixStubBeanDefinitionParser extends SalixBeanDefinitionParser {

	public static final Logger LOG = Logger.getLogger(SalixStubBeanDefinitionParser.class);

	public BeanDefinition parse(Element element, ParserContext parserContext) {

		String serviceName = element.getAttributes().getNamedItem("name").getTextContent();
		String interfaceClass = element.getAttributes().getNamedItem("interface").getTextContent();
		String zkUrl = (String) getMeta("zkUrl");

		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setBeanClass(SalixFactoryBean.class);
		beanDefinition.setLazyInit(false);

		beanDefinition.getPropertyValues().addPropertyValue("serviceName", serviceName);
		beanDefinition.getPropertyValues().addPropertyValue("interfaceClass", interfaceClass);
		beanDefinition.getPropertyValues().addPropertyValue("proxyType", 2);
		beanDefinition.getPropertyValues().addPropertyValue("zkUrl", zkUrl);

		parserContext.getRegistry().registerBeanDefinition(serviceName, beanDefinition);

		return beanDefinition;
	}
}
