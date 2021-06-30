package base.client;

public enum InputType {
	MOUSE_MOVEMENT(0),
	MOUSE_PRESS(1),
	KEYBOARD_PRESS(2),
	MOUSE_RELEASE(3),
	KEYBOARD_RELEASE(4),
	OTHER(3);
	
	private int intType;
	
	private InputType(int intType) {
		this.intType = intType;
	}
	
	public int getIntType() {
		return this.intType;
	}
	
	public static InputType valueOf(int intType) throws NumberFormatException{
		InputType[] types = InputType.values();
		for(var type : types)
			if(type.intType == intType)
				return type;
		
		return null;
	}
}
