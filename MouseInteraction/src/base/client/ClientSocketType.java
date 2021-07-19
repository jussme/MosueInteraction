package base.client;

import java.net.DatagramSocket;
import java.net.Socket;

public enum ClientSocketType{
	GraphicsInputSocket(0, Socket.class),
	InputSocket(1, DatagramSocket.class),
	MetaPassiveSocket(2, Socket.class),
	MetaControllingSocket(3, Socket.class),
	OutputSocket(4, DatagramSocket.class),
	GraphicsOutputSocket(5, Socket.class);
	
	private int intType;
	private Class<?> cl;
	private static int nOfTypes = 6;
	
	public int getIntType() {
		return this.intType;
	}
	
	public Class<?> getCorrespondingClass() {
	  return this.cl;
	}
	
	public static int getNOfTypes() {
		return nOfTypes;
	}
	
	public static ClientSocketType valueOf(int intType) throws NumberFormatException{
		ClientSocketType[] types = ClientSocketType.values();
		for(var type : types)
			if(type.intType == intType)
				return type;
		
		return null;
	}
	
	private ClientSocketType(int intType, Class<?> cl){
		this.intType = intType;
		this.cl = cl;
	}
}