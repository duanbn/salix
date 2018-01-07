package com.salix.demo;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Startup {

	public static void main(String[] args) throws IOException {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
		System.in.read();
	}

}
