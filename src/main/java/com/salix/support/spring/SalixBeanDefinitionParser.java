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

}
