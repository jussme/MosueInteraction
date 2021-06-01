package base;

import javax.swing.JOptionPane;

import controlling.client.ControllingClient;
import passive.client.PassiveClient;
import server.ServerApp;

public class Main {
	private static final String VALID_IP_ADDRESS_REGEX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]).){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";

	private static final String VALID_HOSTNAME_REGEX = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]).)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9-]*[A-Za-z0-9])$";
	
	
	public static void main(String[] args) {
		if(args.length != 3 && args.length != 1 && args.length != 0) {
			notifyOfFormatError();
		}else {
			try {
				if(args.length == 1)
					launchServer(Integer.parseInt(args[0]));
				else
					if(args.length == 3)
						launchClient(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
					else
						if(args.length == 0)
							chooseMode();
			}catch(NumberFormatException e) {
				notifyOfFormatError();
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	
	
	private static void launchServer(int localPortNumber) {
		new ServerApp(localPortNumber);
	}
	
	private static void launchClient(String hostname, int remotePortNumber, int clientMode) {
		if(clientMode == 1)
			new ControllingClient(hostname, remotePortNumber);
		else
			new PassiveClient(hostname, remotePortNumber);
	}
	
	enum Mode{
		PassiveClient,
		ControllingClient,
		Server;
	}
	private static void chooseMode() {
		String hostname = "";
		int portNumber = -1;
		
		var choosenClientMode = (Mode)JOptionPane.showInputDialog(null,
				"Choose operation mode",
				"Mode options",
				JOptionPane.QUESTION_MESSAGE,
				null,
				Mode.values(),
				Mode.values()[0]);
		
		try{
			portNumber = Integer.parseInt(JOptionPane.showInputDialog(null, "Server port:", "28010"));
		}catch(NumberFormatException e) {
			notifyOfFormatError();
			e.printStackTrace();
			System.exit(1);
		}
		if(choosenClientMode == Mode.ControllingClient || choosenClientMode == Mode.PassiveClient) {
			hostname = JOptionPane.showInputDialog(null, "Server hostname or IP:");
			if(!hostname.matches(VALID_HOSTNAME_REGEX) && !hostname.matches(VALID_IP_ADDRESS_REGEX)) {
				notifyOfFormatError();
				System.exit(1);
			}
			if(choosenClientMode == Mode.ControllingClient)
				new ControllingClient(hostname, portNumber);
			else
				new PassiveClient(hostname, portNumber);
		}else {
			new ServerApp(portNumber);
		}
	}
	
	private static void notifyOfFormatError() {
		JOptionPane.showMessageDialog(null, "Format error", "Error", JOptionPane.ERROR_MESSAGE);
	}
}
