package base.client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Client {
	protected static final int PORT_LOWER_BOUND = 49152;
	protected static final int PORT_UPPER_BOUND = 65535;
  
	protected Socket logTCPSocketOn(String hostname, int remotePort, String password, ClientSocketType clientSocketType) throws IOException{
		Socket socket = new Socket(hostname, remotePort);
		var bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		bufferedWriter.write(password + "\n");
		bufferedWriter.flush();
		bufferedWriter.write(clientSocketType.getIntType());
		bufferedWriter.flush();
		
		return socket;
	}
	
	protected InetSocketAddress logUDPSocketOn(String hostname, int remotePort, String password, ClientSocketType clientSocketType) throws IOException{
    try (DatagramSocket dSocket = new DatagramSocket(findFreePort())) {
      byte[] payload = packSocketTypeAndPassword(clientSocketType, password);
      dSocket.send(
          new DatagramPacket(
              payload,
              payload.length,
              new InetSocketAddress(hostname, remotePort)
          )
      );
      
      return dffd;
    } catch(IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    //to satisfy the compiler
    return null;
	}
	
	protected byte[] packSocketTypeAndPassword(ClientSocketType clientSocketType, String password) throws UnsupportedEncodingException {
	//socketType and password in one packet
    byte[] passwordInBytes = password.getBytes("UTF-8");
    byte[] socketTypeAndPasswordByteArr = new byte[1 + passwordInBytes.length];
    socketTypeAndPasswordByteArr[0] = (byte) clientSocketType.getIntType();
    for (int it = 1; it < socketTypeAndPasswordByteArr.length; ++ it) {
      socketTypeAndPasswordByteArr[it] = passwordInBytes[it];
    }
    
    return socketTypeAndPasswordByteArr;
	}
	
	protected int findFreePort() throws IOException{
	  int currentPort = PORT_LOWER_BOUND;
	  while(currentPort <= PORT_UPPER_BOUND) {
	    try (ServerSocket serverSocket = new ServerSocket(currentPort)) {
	      if (serverSocket.isBound() && serverSocket.getLocalPort() == currentPort) {
	        return currentPort;
	      }
	    } catch (IOException e) {
	      ++currentPort;
	    }
	  }
	  
	  throw new IOException("No free port in the <" + PORT_LOWER_BOUND + ", " + PORT_UPPER_BOUND + "> range");
	}
}
