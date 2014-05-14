package com.salix.core.message;

public class ShutdownMessage extends Message {

	private boolean shutdown = true;

	public boolean isShutdown() {
		return shutdown;
	}

	public void setShutdown(boolean shutdown) {
		this.shutdown = shutdown;
	}

}
