package base.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
	protected DatagramSocket logUDPSocketOn(String hostname, int remotePort, String password, ClientSocketType clientSocketType) throws IOException {
	  Socket socket = logTCPSocketOn(hostname, remotePort, password, clientSocketType);
	  
	  var datagramSocket = new DatagramSocket();
	  
	  var outputStream = new BufferedOutputStream(socket.getOutputStream());
	  outputStream.write(datagramSocket.getLocalPort());
	  outputStream.write(datagramSocket.getLocalPort() >> 8);
	  outputStream.flush();
	  
	  var inputStream = new BufferedInputStream(socket.getInputStream());
	  int port = inputStream.read();
	  port += inputStream.read() << 8;
	  
	  datagramSocket.connect(new InetSocketAddress(hostname, port));
	  
	  return datagramSocket;
	}
	
	protected Socket logTCPSocketOn(String hostname, int remotePort, String password, ClientSocketType clientSocketType) throws IOException {
	  Socket socket = new Socket(hostname, remotePort);
    var outputStream = new BufferedOutputStream(socket.getOutputStream());
    outputStream.write(password.length());
    outputStream.write(password.getBytes(base.Main.ENCODING));
    outputStream.write(clientSocketType.getIntType());
    outputStream.flush();
    
    return socket;
  }
}
