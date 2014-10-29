package com.salix.support.spring;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.xml.BeanDefinitionParser;

public abstract class SalixBeanDefinitionParser implements BeanDefinitionParser {

	private static final Map<String, Object> metaCache = new HashMap<String, Object>();

	protected static void setMeta(String key, Object value) {
		metaCache.put(key, value);
	}

	protected static Object getMeta(String key) {
		return metaCache.get(key);
	}

	// protected String getZookeeperElement(Element element) {
	// NodeList nodeList = element.getParentNode().getChildNodes();
	// for (int i = 0; i < nodeList.getLength(); i++) {
	// Node ele = nodeList.item(i);
	// if (ele.getNodeName().equals("salix:zookeeper")) {
	// return ((Element) ele).getAttribute("address");
	// }
	// }
	//
	// throw new RuntimeException("找不到配置中心的zookeeper地址");
	// }

}
