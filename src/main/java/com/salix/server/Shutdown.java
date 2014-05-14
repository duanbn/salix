package com.salix.server;

import java.io.IOException;

import com.salix.client.connection.SingleConnection;
import com.salix.core.message.ShutdownMessage;

public class Shutdown {

	private SingleConnection conn;

	public Shutdown(String host, int port) throws IOException {
		this.conn = new SingleConnection(host, port);
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("usage:java Shutdown [port]");
			System.exit(-1);
		}

		Shutdown sd = new Shutdown("localhost", Integer.parseInt(args[0]));
		sd.doShutdown();

	}

	public void doShutdown() throws IOException {
		ShutdownMessage message = new ShutdownMessage();
		this.conn.send(message);
	}

}
