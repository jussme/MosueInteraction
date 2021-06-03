package base;

public enum ClientSocketType{
	GraphicsInputSocket(0),
	GraphicsOutputSocket(1),
	InputSocket(2),
	OutputSocket(3);
	
	int intType;
	
	public int getIntType() {
		return this.intType;
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