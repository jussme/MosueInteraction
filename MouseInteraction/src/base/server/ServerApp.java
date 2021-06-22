package base.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static base.ClientSocketType.*;
import base.ClientSocketType;

public class ServerApp{
	private final Map<String, Connector> socketsByPassword = new HashMap<>();
	
	private class Mediator extends Thread{
		DataOutputStream controlls_ServerOutputStream;
		DataInputStream controlls_ServerInputStream;
		BufferedOutputStream screenData_ServerOutputStream;
		BufferedInputStream screenData_ServerInputStream;
		/*
		Mediator(OutputStream controlls_ServerOutputStream,
				InputStream controlls_ServerInputStream,
				OutputStream screenData_ServerOutputStream,
				InputStream screenData_ServerInputStream) throws IOException{
			System.out.println("Mediator");
			
			this.run();
			
			System.out.println("MediatorTransfered");
		}*/
		
		Mediator(DataOutputStream controlls_ServerOutputStream,
				DataInputStream controlls_ServerInputStream,
				BufferedOutputStream screenData_ServerOutputStream,
				BufferedInputStream screenData_ServerInputStream) {
			
			this.screenData_ServerOutputStream = screenData_ServerOutputStream;
			this.screenData_ServerInputStream = screenData_ServerInputStream;
			this.controlls_ServerOutputStream = controlls_ServerOutputStream;
			this.controlls_ServerInputStream = controlls_ServerInputStream;
			this.start();
				
			System.out.println("MediatorEndedConstr");
		}
		
		@Override
		public void run() {
			try {
				new Thread(() -> {
					try {
						screenData_ServerInputStream.transferTo(screenData_ServerOutputStream);System.out.println("end of transer");
						//while(true) {
						//	screenData_ServerOutputStream.write(screenData_ServerInputStream.read());
						//}
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}).start();
				controlls_ServerInputStream.transferTo(controlls_ServerOutputStream);
				
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	private class Connector {
		/*GraphicsInputSocket(0),
		GraphicsOutputSocket(1),
		InputSocket(2),
		OutputSocket(3),
		MetaPassiveSocket(4),
		MetaControllingSocket(5);*/
		Socket[] sockets = new Socket[6];
		DataInputStream controlls_ServerInputStream;
		DataOutputStream controlls_ServerOutputStream;
		BufferedInputStream screenData_ServerInputStream;
		BufferedOutputStream screenData_ServerOutputStream;
		InputStream metaDataInputStream;
		OutputStream metaDataOutputStream;
		
		Connector(Socket clientSocket, ClientSocketType clientSocketType) throws IOException{
			connectSocket(clientSocket, clientSocketType);
		}
		
		boolean connectSocket(Socket clientSocket, ClientSocketType clientSocketType) throws IOException{
			if(sockets[clientSocketType.getIntType()] != null) {
				sockets[clientSocketType.getIntType()] = clientSocket;
			}
			
			switch(clientSocketType) {
				case InputSocket:
					if(controlls_ServerOutputStream != null)
						return false;
					controlls_ServerOutputStream = new DataOutputStream(clientSocket.getOutputStream());
					break;
				case OutputSocket:
					if(controlls_ServerInputStream != null)
						return false;
					controlls_ServerInputStream = new DataInputStream(clientSocket.getInputStream());
					break;
				case GraphicsInputSocket:
					if(screenData_ServerOutputStream != null)
						return false;
					screenData_ServerOutputStream = new BufferedOutputStream(clientSocket.getOutputStream());
					break;
				case GraphicsOutputSocket:
					if(screenData_ServerInputStream != null)
						return false;
					screenData_ServerInputStream = new BufferedInputStream(clientSocket.getInputStream());
					break;
			}
			
			boolean nullPresent = false;
			for(Object o : sockets) {
				if(o == null) {
					nullPresent = true;
				}
			}
			
			if(!nullPresent) {
				/*new Mediator(
						new DataOutputStream(sockets[InputSocket.getIntType()].getOutputStream()),
						new DataInputStream(sockets[OutputSocket.getIntType()].getInputStream()),
						new BufferedOutputStream(sockets[GraphicsInputSocket.getIntType()].getOutputStream()),
						new BufferedInputStream(sockets[GraphicsOutputSocket.getIntType()].getInputStream()),
						new DataOutputStream(sockets[MetaControllingSocket.getIntType()].getOutputStream()),
						new DataInputStream(sockets[MetaPassiveSocket.getIntType()].getInputStream())
						);*/
			}
				
			return true;
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
				entry.getValue().connectSocket(socket, clientSocketType);
				found = true;
			}
		}
		
		if(!found) {
			socketsByPassword.put(password, new Connector(socket, clientSocketType));
		}
	}
}