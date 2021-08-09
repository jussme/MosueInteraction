package base;

import javax.swing.JOptionPane;

import base.client.controlling.ControllingClient;
import base.client.passive.PassiveClient;
import base.server.ServerApp;

public class Main {
	private static final String VALID_IP_ADDRESS_REGEX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]).){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
	private static final String VALID_HOSTNAME_REGEX = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]).)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9-]*[A-Za-z0-9])$";
	private static final String VALID_PORT_NUMBER_REGEX = "^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$";
	public static final String ENCODING = "UTF-8";
	
	public static void main(String[] args) {
	  //new TestClass(args[0]);
	  
		if(args.length != 1 && args.length != 0) {
			notifyOfFormatError();
		}else {
			try {
				if(args.length == 1)
					new ServerApp(Integer.parseInt(args[0]));
				else
					if(args.length == 0) {
						switch(chooseMode()) {
							case PassiveClient:
								new PassiveClient(
										getHostnameOrIPInput(),
										getPortNumberInput(),
										getPasswordInput());
								break;
							case ControllingClient:
								new ControllingClient(
										getHostnameOrIPInput(),
										getPortNumberInput(),
										getPasswordInput());
								break;
							case Server:
								new ServerApp(getPortNumberInput());
								break;
						}
					}
			}catch(NumberFormatException e) {
				notifyOfFormatError();
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	enum Mode{
		PassiveClient,
		ControllingClient,
		Server;
	}
	
	private static Mode chooseMode() {
		return (Mode)JOptionPane.showInputDialog(null,
				"Choose operation mode",
				"Mode options",
				JOptionPane.QUESTION_MESSAGE,
				null,
				Mode.values(),
				Mode.values()[0]);
	}
	
	//TODO DRY
	private static int getPortNumberInput() {
		try{
			String portNumberStr = JOptionPane.showInputDialog(null, "Server port:", "28010");
			if(!portNumberStr.matches(VALID_PORT_NUMBER_REGEX))
				throw new NumberFormatException("Invalid port number format");
			return Integer.parseInt(portNumberStr);
		}catch(NumberFormatException e) {
			notifyOfFormatError();
			System.exit(1);
			return 0;
		}
		
	}
	
	private static String getHostnameOrIPInput() {
		String hostname = JOptionPane.showInputDialog(null, "Server hostname or IP: ", "localhost");
		if(!hostname.matches(VALID_HOSTNAME_REGEX) && !hostname.matches(VALID_IP_ADDRESS_REGEX)) {
			notifyOfFormatError();
			System.exit(1);
		}
		return hostname;
	}
	
	private static String getPasswordInput() {
		String password = JOptionPane.showInputDialog(null, "Password: ");
		if(password.isEmpty() || password.matches("\s") || password.length() > 25) {
			notifyOfFormatError("Password either too long(limited to max. 25 characters), or contains whitespaces");
			System.exit(1);
		}
		return password;
	}
	
	private static void notifyOfFormatError() {
	  notifyOfFormatError(null);
	}
	
	private static void notifyOfFormatError(String message) {
		JOptionPane.showMessageDialog(null, "Input format error. " + message, "Error", JOptionPane.ERROR_MESSAGE);
	}
}
