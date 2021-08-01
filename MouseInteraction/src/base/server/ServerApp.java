package base.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import base.client.ClientSocketType;

public class ServerApp{
	private final Map<String, Mediator> socketsByPassword = new HashMap<>();
	
	private class Mediator{
		Object[] sockets = new Object[6];
		
		Mediator(Socket socket, ClientSocketType clientSocketType) {
			sockets[clientSocketType.getIntType()] = socket;
		}
		
		boolean allSocketsBound() {
		  for(Object s : sockets) {
        if(s == null) {
          return false;
        }
      }
      
      return true;
		}
		
		boolean bindSocket(Socket clientSocket, ClientSocketType clientSocketType) {
			if(sockets[clientSocketType.getIntType()] == null) {
				sockets[clientSocketType.getIntType()] = clientSocket;
				/*
				try {
				  switch(clientSocketType) {
            case OutputSocket:
              clientSocket.setReceiveBufferSize(8);
              break;
            case InputSocket:
              clientSocket.setSendBufferSize(8);
              break;
            default:
              break;
				  }
				} catch (SocketException e) {
				  e.printStackTrace();
				  System.exit(1);
				}
				*/
				
				if(allSocketsBound()) {
					transferAllSockets();
				}
				
				return true;
			}else {
				return false;
			}
		}
		
		void transferAllSockets() {
			for(int it = 5; it > 2; --it) {
				final int constBuff = it;
				new Thread(() -> {
					try {
					  switch (ClientSocketType.valueOf(constBuff)) {
					    case OutputSocket:
					      DatagramSocket serverInUDP = (DatagramSocket) sockets[constBuff];
                DatagramSocket serverOutUDP = (DatagramSocket) sockets[ClientSocketType.getNOfTypes() - 1 - constBuff];
                byte[] buf = new byte[6];
					      DatagramPacket packet = new DatagramPacket(buf, buf.length);
                while(true) {
                  serverInUDP.receive(packet);
					        serverOutUDP.send(packet);
					      }
					    default:
					      Socket serverInTCP = (Socket) sockets[constBuff];
					      Socket serverOutTCP = (Socket) sockets[ClientSocketType.getNOfTypes() - 1 - constBuff];
					      serverInTCP.getInputStream().transferTo(serverOutTCP.getOutputStream());
					      break;
					  }
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}).start();
			}
		}
	}
	
	public ServerApp(int localPort) {
		new ServerWindow();
		launchTCPServerSocketServicing(localPort);
		launchUDPServerSocketServicing(localPort + 1);
	}
	
	private void launchTCPServerSocketServicing(int localPort) {
		try (final var serverSocket = new ServerSocket()){
			//serverSocket.setReceiveBufferSize(64000);
			serverSocket.bind(new InetSocketAddress(localPort));
			new Thread(() -> {
			  try {
			    while(true){
	          logTCPClientSocket(serverSocket.accept());
	        }
			  } catch (IOException e) {
			    e.printStackTrace();
			    System.exit(1);
			  }
			}).start();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void launchUDPServerSocketServicing(int localPort) {
	  try (final var serverUDPSocket = new DatagramSocket()) {
      serverUDPSocket.bind(new InetSocketAddress(localPort));
      new Thread(() -> {
        try {
          byte[] recvBuffer = new byte[508];
          DatagramPacket p = new DatagramPacket(recvBuffer, recvBuffer.length);
          while(true){
            serverUDPSocket.receive(p);
            logUDPClientSocket(p);
          }
        } catch (IOException e) {
          e.printStackTrace();
          System.exit(1);
        }
      }).start();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
	}
	
	private void logUDPClientSocket(DatagramPacket p) throws IOException {
	  
	  //TODO receive packet
	  
	  boolean found = false;
    for(var entry : socketsByPassword.entrySet()) {
      if(entry.getKey().equals(password)) {
        entry.getValue().bindSocket(socket, clientSocketType);
        found = true;
      }
    }
    
    if(!found) {
      socketsByPassword.put(password, new Mediator(socket, clientSocketType));
    }
	}
	
	private void logTCPClientSocket(Socket socket) throws IOException{
		var bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String password = bufferedReader.readLine().trim();
		ClientSocketType clientSocketType = ClientSocketType.valueOf(bufferedReader.read());
		
		boolean found = false;
		for(var entry : socketsByPassword.entrySet()) {
			if(entry.getKey().equals(password)) {
				entry.getValue().bindSocket(socket, clientSocketType);
				found = true;
			}
		}
		
		if(!found) {
			socketsByPassword.put(password, new Mediator(socket, clientSocketType));
		}
	}
}