package base.server;

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

import base.ClientSocketType;

public class ServerApp{
	private final Map<String, Connector> socketsByPassword = new HashMap<>();
	
	private class Mediator extends Thread{
		
		Mediator(OutputStream controlls_ServerOutputStream,
				InputStream controlls_ServerInputStream,
				OutputStream screenData_ServerOutputStream,
				InputStream screenData_ServerInputStream) throws IOException{
			controlls_ServerInputStream.transferTo(controlls_ServerOutputStream);
			screenData_ServerInputStream.transferTo(screenData_ServerOutputStream);
		}
	}
	
	private class Connector {
		InputStream controlls_ServerInputStream;
		OutputStream controlls_ServerOutputStream;
		DataInputStream screenData_ServerInputStream;
		DataOutputStream screenData_ServerOutputStream;
		
		Connector(Socket clientSocket, ClientSocketType clientSocketType) throws IOException{
			bindSocket(clientSocket, clientSocketType);
		}
		
		boolean bindSocket(Socket clientSocket, ClientSocketType clientSocketType) throws IOException{
			switch(clientSocketType) {
				case GraphicsInputSocket:
					if(controlls_ServerOutputStream != null)
						return false;
					controlls_ServerOutputStream = clientSocket.getOutputStream();
					break;
				case GraphicsOutputSocket:
					if(controlls_ServerInputStream != null)
						return false;
					controlls_ServerInputStream = clientSocket.getInputStream();
					break;
				case InputSocket:
					if(screenData_ServerOutputStream != null)
						return false;
					screenData_ServerOutputStream = new DataOutputStream(clientSocket.getOutputStream());
					break;
				case OutputSocket:
					if(screenData_ServerInputStream != null)
						return false;
					screenData_ServerInputStream = new DataInputStream(clientSocket.getInputStream());
					break;
			}
			
			if(controlls_ServerOutputStream != null &&
				controlls_ServerInputStream != null &&
				screenData_ServerInputStream != null &&
				screenData_ServerOutputStream != null)
				new Mediator(controlls_ServerOutputStream,
						controlls_ServerInputStream,
						screenData_ServerOutputStream,
						screenData_ServerInputStream);
			
			return true;
		}
	}
	
	public ServerApp(int localPort) {
		launchServerSocketServicing(localPort);
	}
	
	private void launchServerSocketServicing(int localPort) {
		try {
			var serverSocket = new ServerSocket(localPort);
			Socket buff;
			while(true){
				buff = serverSocket.accept();
				logClientSocket(buff);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void logClientSocket(Socket socket) throws IOException{
		var bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String password = bufferedReader.readLine();
		ClientSocketType clientSocketType = ClientSocketType.valueOf(Integer.parseInt(bufferedReader.readLine()));
		
		boolean found = false;
		for(var entry : socketsByPassword.entrySet()) {
			if(entry.getKey() == password) {
				entry.getValue().bindSocket(socket, clientSocketType);
				found = true;
			}
		}
		
		if(!found) {
			socketsByPassword.put(password, new Connector(socket, clientSocketType));
		}
	}
}