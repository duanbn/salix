package com.salix.server;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Startup {

	public static final Logger LOG = Logger.getLogger(Startup.class);

	public static final String SYS_PROP_BASEDIR = "system.root.path";

	public static void main(String[] args) throws IOException {
		List<String> springContextFiles = new ArrayList<String>();

		String baseDir = System.getProperty(SYS_PROP_BASEDIR);
		String confDir = baseDir + "/conf";

		File[] xmlFiles = new File(confDir).listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				if (pathname.getName().endsWith(".xml")) {
					return true;
				}
				return false;
			}
		});
		for (File xmlFile : xmlFiles) {
			if (isSpringContextFile(xmlFile)) {
				springContextFiles.add(xmlFile.getName());
			}
		}

		LOG.info("find spring context file " + springContextFiles);

		ClassPathXmlApplicationContext springCtx = new ClassPathXmlApplicationContext(
				springContextFiles.toArray(new String[springContextFiles.size()]));
		springCtx.start();

		springCtx.registerShutdownHook();
	}

	/**
	 * 通过判断root节点来判断是否是spring配置文件.
	 * 
	 * @param file
	 * @return
	 */
	private static boolean isSpringContextFile(File file) {
		InputStream is = null;
		String xmlFileName = file.getName();
		Document xmlDoc = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(xmlFileName);
			xmlDoc = builder.parse(new InputSource(is));
		} catch (Exception e) {
			LOG.warn("read " + file.getName() + " failure " + e.getMessage());
			return false;
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				LOG.error(e);
			}
		}

		NodeList childs = xmlDoc.getChildNodes();
		if (childs == null || childs.getLength() == 0) {
			return false;
		}
		Node root = xmlDoc.getChildNodes().item(0);

		if (root.getNodeName().equals("beans")) {
			return true;
		}
		return false;
	}

}
