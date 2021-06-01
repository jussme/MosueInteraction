package server;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerApp {

	public ServerApp(int localPort) {
		launchServerSocketServicing(localPort);
	}
	
	private void launchServerSocketServicing(int localPort) {
		try {
			var serverSocket = new ServerSocket(localPort);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}