package base.client;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
	protected DatagramSocket logUDPSocketOn(String hostname, int remotePort, String password, ClientSocketType clientSocketType) throws IOException {
	  Socket socket = logTCPSocketOn(hostname, remotePort, password, clientSocketType);
	  var dataInputStream = new DataInputStream(socket.getInputStream());
	  int port = dataInputStream.readChar();
	  
	  var datagramSocket = new DatagramSocket();
	  datagramSocket.connect(new InetSocketAddress(hostname, port));
	  
	  return datagramSocket;
	}
	
	protected Socket logTCPSocketOn(String hostname, int remotePort, String password, ClientSocketType clientSocketType) throws IOException {
	  Socket socket = new Socket(hostname, remotePort);
    var bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    bufferedWriter.write(password + "\n");
    bufferedWriter.write(clientSocketType.getIntType());
    bufferedWriter.flush();
    
    return socket;
  }
}
