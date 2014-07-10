package com.salix.support.spring;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.salix.core.util.StringUtil;

public class SalixBeanDefinitionParser implements BeanDefinitionParser {

	public static final Logger LOG = Logger.getLogger(SalixBeanDefinitionParser.class);

	private Class<?> beanClass;

	public SalixBeanDefinitionParser(Class<?> beanClass) {
		this.beanClass = beanClass;
	}

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setBeanClass(beanClass);
		beanDefinition.setLazyInit(false);

		String id = element.getAttribute("id");
		if (StringUtil.isBlank(id)) {
			throw new RuntimeException("id配置错误, id=" + id);
		}
		String interfaceClass = element.getAttribute("interface");
		if (StringUtil.isBlank(interfaceClass)) {
			throw new RuntimeException("interface配置错误, interface=" + interfaceClass);
		}
		String serviceName = element.getAttribute("name");
		if (StringUtil.isBlank(serviceName)) {
			throw new RuntimeException("service name配置错误, name=" + serviceName);
		}
		// get remote host and port
		Element parent = (Element) element.getParentNode();
		if (parent == null) {
			throw new RuntimeException("配置错误, 找不到cp标签");
		}
		String host = parent.getAttribute("host");
		int port = Integer.parseInt(parent.getAttribute("port"));

		if (LOG.isDebugEnabled()) {
			LOG.debug("id=" + id + ", serviceName=" + serviceName + ", interface=" + interfaceClass + ", host=" + host
					+ ", port=" + port);
		}

		beanDefinition.getPropertyValues().addPropertyValue("id", id);
		beanDefinition.getPropertyValues().addPropertyValue("serviceName", serviceName);
		beanDefinition.getPropertyValues().addPropertyValue("interfaceClass", interfaceClass);
		beanDefinition.getPropertyValues().addPropertyValue("host", host);
		beanDefinition.getPropertyValues().addPropertyValue("port", port);

		parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);

		return beanDefinition;
	}
}
