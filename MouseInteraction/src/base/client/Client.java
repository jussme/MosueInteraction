package base.client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Client {
	public static final int TOTAL_REFRESH_DELAY = 16;
	
	public Socket logSocketOn(String hostname, int remotePort, String password, ClientSocketType clientSocketType) throws IOException{
		var socket = new Socket(hostname, remotePort);
		var bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		bufferedWriter.write(password + "\n");
		bufferedWriter.flush();
		bufferedWriter.write(clientSocketType.getIntType());
		bufferedWriter.flush();
		
		return socket;
	}
}
