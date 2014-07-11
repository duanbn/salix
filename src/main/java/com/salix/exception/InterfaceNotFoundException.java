package com.salix.exception;

public class InterfaceNotFoundException extends RuntimeException {

	public InterfaceNotFoundException(String interfaceName) {
		super(interfaceName + " class not found");
	}

}
