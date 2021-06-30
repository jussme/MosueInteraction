package base.client;

public enum ClientSocketType{
	GraphicsInputSocket(0),
	InputSocket(1),
	MetaPassiveSocket(2),
	MetaControllingSocket(3),
	OutputSocket(4),
	GraphicsOutputSocket(5);
	
	private int intType;
	private static int nOfTypes = 6;
	
	public int getIntType() {
		return this.intType;
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
	
	private ClientSocketType(int intType){
		this.intType = intType;
	}
}