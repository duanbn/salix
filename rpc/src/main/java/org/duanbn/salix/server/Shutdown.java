package org.duanbn.salix.server;

import java.io.IOException;

import org.duanbn.salix.client.connection.SingleConnection;
import org.duanbn.salix.core.message.ShutdownMessage;

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

		Shutdown sd = new Shutdown("127.0.0.1", Integer.parseInt(args[0]));
		sd.doShutdown();
	}

	public void doShutdown() throws IOException {
		ShutdownMessage message = new ShutdownMessage();
		this.conn.send(message);
	}

}
