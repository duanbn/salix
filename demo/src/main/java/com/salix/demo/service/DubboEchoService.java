package com.salix.demo.service;

public interface DubboEchoService {

	public Object echo(Object value) throws Exception;

	public void discard();

	public void discard(String value);

	public void error() throws Exception;

	public void rterror();

}
