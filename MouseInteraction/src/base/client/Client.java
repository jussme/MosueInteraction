package base.client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Client {
	public static final int TOTAL_REFRESH_DELAY = 16;
	private static final int PORT_LOWER_BOUND = 49152;
	
	public Socket logTCPSocketOn(String hostname, int remotePort, String password, ClientSocketType clientSocketType) throws IOException{
		Socket socket = new Socket(hostname, remotePort);
		var bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		bufferedWriter.write(password + "\n");
		bufferedWriter.flush();
		bufferedWriter.write(clientSocketType.getIntType());
		bufferedWriter.flush();
		
		return socket;
	}
	
	public DatagramSocket logUDPSocketOn(String hostname, int remotePort, String password, ClientSocketType clientSocketType) throws IOException{
    try (DatagramSocket dSocket = new DatagramSocket(findFreePort())) {
      //socketType and password in one packet
      byte[] passwordInBytes = password.getBytes("UTF-8");
      byte[] socketTypeAndPasswordByteArr = new byte[1 + passwordInBytes.length];
      socketTypeAndPasswordByteArr[0] = (byte) clientSocketType.getIntType();
      for (int it = 1; it < socketTypeAndPasswordByteArr.length; ++ it) {
        socketTypeAndPasswordByteArr[it] = passwordInBytes[it];
      }
      dSocket.send(
          new DatagramPacket(socketTypeAndPasswordByteArr,
              socketTypeAndPasswordByteArr.length,
              new InetSocketAddress(hostname, remotePort)
          )
      );
      
      return dSocket;
    } catch(IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    //to satisfy the compiler
    return null;
	}
	/*
	private byte[] toByteRepresentedIPAddress(String ip) {
	  byte[] byteIP = new byte[4];
	  
	  ip.split
	  
	  return byteIP;
	}
	*/
	private int findFreePort() throws IOException{
	  int currentPort = PORT_LOWER_BOUND;
	  while(currentPort <= 65535) {
	    try (ServerSocket serverSocket = new ServerSocket(currentPort)) {
	      if (serverSocket.isBound() && serverSocket.getLocalPort() == currentPort) {
	        return currentPort;
	      }
	    } catch (IOException e) {
	      ++currentPort;
	    }
	  }
	  
	  throw new IOException("No free port in the <49152, 65535> range");
	}
}
