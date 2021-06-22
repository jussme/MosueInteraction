package base.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import base.ClientSocketType;

public class ServerApp{
	private final Map<String, Mediator> socketsByPassword = new HashMap<>();
	
	private class Mediator{
		Socket[] sockets = new Socket[6];
		
		public Mediator(Socket socket, ClientSocketType clientSocketType) {
			sockets[clientSocketType.getIntType()] = socket;
		}
		
		public boolean bindSocket(Socket clientSocket, ClientSocketType clientSocketType) {
			if(sockets[clientSocketType.getIntType()] == null) {
				sockets[clientSocketType.getIntType()] = clientSocket;
				
				boolean nullIsPresent = false;
				for(Socket s : sockets) {
					if(s == null) {
						nullIsPresent = true;
					}
				}
				
				if(!nullIsPresent) {
					bindAllSockets();
				}
				
				return true;
			}else {
				return false;
			}
		}
		
		public void bindAllSockets() {
			for(int it = 5; it > 2; --it) {
				int constBuff = it;
				new Thread(() -> {
					try {
						sockets[constBuff].getInputStream().transferTo(sockets[ClientSocketType.getNOfTypes() - 1 - constBuff].getOutputStream());
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
		launchServerSocketServicing(localPort);
	}
	
	private void launchServerSocketServicing(int localPort) {
		try {
			@SuppressWarnings("resource")
			var serverSocket = new ServerSocket(localPort);
			Socket buff;
			while(true){
				buff = serverSocket.accept();
				logClientSocket(buff);System.out.println("Logged " + buff.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void logClientSocket(Socket socket) throws IOException{
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