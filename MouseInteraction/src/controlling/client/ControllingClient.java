package controlling.client;

import java.io.IOException;
import java.net.Socket;

public class ControllingClient {
	
	
	public ControllingClient(String hostname, int remotePort) {
		launchClientSocketServicing(hostname, remotePort);
	}
	
	private void launchClientSocketServicing(String hostname, int remotePort) {
		try {
			var socket = new Socket(hostname, remotePort);
			
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
