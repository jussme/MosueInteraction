package base.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
	
  private final Map<String, Mediator> socketsByPassword = new HashMap<>();
	
	public ServerApp(int localPort) {
		new ServerWindow();
		launchServerSocketServicing(localPort);
	}
	
	private void launchServerSocketServicing(int localPort) {
		try (final var serverSocket = new ServerSocket()){
			//serverSocket.setReceiveBufferSize(64000);
			serverSocket.bind(new InetSocketAddress(localPort));
			launchClientSocketServicing(serverSocket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void launchClientSocketServicing(ServerSocket serverSocket) {
	  new Thread(() -> {
      try {
        while(true){
          logClientSocket(serverSocket.accept());
        }
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(1);
      }
    }).start();
	}
	
	private void logClientSocket(Socket socket) throws IOException {
	  var bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    String password = bufferedReader.readLine();
    ClientSocketType clientSocketType = ClientSocketType.valueOf(bufferedReader.read());
    
    Object loggedSocket = socket;
    switch (clientSocketType) {
      //UDP sockets need to be sent a new port number of the receiving DatagramSocket
      case InputSocket:
      case OutputSocket:
        int foundFreePort = findFreePort();
        new DataOutputStream(socket.getOutputStream()).writeChar(foundFreePort);
        loggedSocket = new DatagramSocket(foundFreePort);
      default:
        boolean found = false;//TODO refactoring methods
        for(var entry : socketsByPassword.entrySet()) {
          if(entry.getKey().equals(password)) {
            entry.getValue().bindSocket(loggedSocket, clientSocketType);
            /*entry.getValue().bindSocket(datagramSocketBeingLoggedOn? datagramSocket : socket, clientSocketType);
              fun but the compiler has to know the method called,
              cant have an alternative between bindSocket(DatagramSocket s) and bindSocket(Socket s)
            */ 
            found = true;
          }
        }
        
        if(!found) {
          socketsByPassword.put(password, new Mediator(loggedSocket, clientSocketType));
        }
        break;
    }
	}
	
	private int findFreePort() throws IOException{
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