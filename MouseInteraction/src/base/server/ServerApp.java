package base.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import base.client.ClientSocketType;

public class ServerApp{
  protected static final int PORT_LOWER_BOUND = 49152;
  protected static final int PORT_UPPER_BOUND = 65535;
	
  private final Map<Credentials, Mediator> socketsByPassword = new HashMap<>();
	
	public ServerApp(int localPort) {
		new ServerWindow();
		launchServerSocketServicing(localPort);
	}
	
	private void launchServerSocketServicing(int localPort) {
	  new Thread(() -> {
      try {
        @SuppressWarnings("resource")
        var serverSocket = new ServerSocket();
        //serverSocket.setReceiveBufferSize(64000);
        serverSocket.bind(new InetSocketAddress(localPort));
        while(true){
          logClientSocket(serverSocket.accept());
        }
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(1);
      }
    }).start();
	}
	
//should be in its own file? dont know if it's even necessary, readCredentials() could be pasted into logClientSocket
	private static class Credentials {
    String password;
    ClientSocketType clientSocketType;
    int udpSocketRemotePort;
    
    Credentials(String password, ClientSocketType clientSocketType) {
      this.password = password;
      this.clientSocketType = clientSocketType;
    }
    
    Credentials(String password, ClientSocketType clientSocketType, int udpSocketRemotePort) {
      this.password = password;
      this.clientSocketType = clientSocketType;
      this.udpSocketRemotePort = udpSocketRemotePort;
    }
    static Credentials readCredentials(Socket socket) throws IOException{
      var inputStream = new BufferedInputStream(socket.getInputStream());
      byte[] buff = new byte[inputStream.read()];
      inputStream.read(buff);
      
      String password = new String(buff, base.Main.ENCODING);
      
      ClientSocketType clientSocketType = ClientSocketType.valueOf(inputStream.read());
      
      if(clientSocketType.getCorrespondingClass() == DatagramSocket.class) {
        int readPort = inputStream.read() + (inputStream.read() << 8);System.out.println("Remote listening port number by bufferedReader.read() = " + readPort);
        return new Credentials(password, clientSocketType, readPort);
      }
      
      return new Credentials(password, clientSocketType);
    }
    
    boolean clientCredentialsMatch(Credentials credentials) {
      return this.password.equals(credentials.password);
    }
  }
	
	private void logClientSocket(Socket socket) throws IOException {
	  Credentials credentials = Credentials.readCredentials(socket);
	  
    Object loggedSocket = socket;
    switch (credentials.clientSocketType) {
      //UDP sockets need to be sent a new port number of the receiving DatagramSocket
      case InputSocket:
      case OutputSocket:
        loggedSocket = new DatagramSocket(); DatagramSocket ds = (DatagramSocket) loggedSocket;
        ds.connect(new InetSocketAddress(socket.getInetAddress(), credentials.udpSocketRemotePort));
        
        notifyClientOfServerUDPPort(socket, ds.getLocalPort());
      default:
        Mediator foundMediator;
        if((foundMediator = findCorrespondingMediator(credentials)) != null) {
          foundMediator.bindSocket(loggedSocket, credentials.clientSocketType);
        } else {
          //new client
          socketsByPassword.put(credentials, new Mediator(loggedSocket, credentials.clientSocketType));
        }
        break;
    }
	}
	
	private Mediator findCorrespondingMediator(Credentials credentials) {
	  for(var entry : socketsByPassword.entrySet()) {
      if(entry.getKey().clientCredentialsMatch(credentials)) {
        return entry.getValue();
      }
    }
	  
	  return null;
	}
	
	private void notifyClientOfServerUDPPort(Socket socket, int port) throws IOException{
	  OutputStream outputStream = socket.getOutputStream();
	  outputStream.write(port);
	  outputStream.write((port >> 8));
	  //new DataOutputStream(socket.getOutputStream()).writeChar(port);
	}
}