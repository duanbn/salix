package com.salix.support.spring;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.salix.client.SalixFactoryBean;
import com.salix.core.util.StringUtil;

public class SalixClientBeanDefinitionParser implements BeanDefinitionParser {

	public static final Logger LOG = Logger.getLogger(SalixClientBeanDefinitionParser.class);

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		String id = element.getAttribute("id");
		if (StringUtil.isBlank(id)) {
			throw new RuntimeException("id配置错误, id=" + id);
		}
		// get remote host and port
		String host = element.getAttribute("host");
		int port = Integer.parseInt(element.getAttribute("port"));

		NodeList services = element.getChildNodes();
		RootBeanDefinition beanDefinition = null;
		for (int i = 0; i < services.getLength(); i++) {
			Node service = services.item(i);
			if (!service.getNodeName().equals("salix:service"))
				continue;

			beanDefinition = new RootBeanDefinition();
			beanDefinition.setBeanClass(SalixFactoryBean.class);
			beanDefinition.setLazyInit(false);
			String serviceName = service.getAttributes().getNamedItem("name").getTextContent();
			String interfaceClass = service.getAttributes().getNamedItem("interface").getTextContent();
			String serviceId = service.getAttributes().getNamedItem("id").getTextContent();
			beanDefinition.getPropertyValues().addPropertyValue("id", serviceId);
			beanDefinition.getPropertyValues().addPropertyValue("serviceName", serviceName);
			beanDefinition.getPropertyValues().addPropertyValue("interfaceClass", interfaceClass);
			beanDefinition.getPropertyValues().addPropertyValue("host", host);
			beanDefinition.getPropertyValues().addPropertyValue("port", port);

			parserContext.getRegistry().registerBeanDefinition(serviceId, beanDefinition);
		}

		return beanDefinition;
	}
}
