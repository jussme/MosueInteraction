package base.server;

import javax.swing.JFrame;

public class ServerWindow extends JFrame{
	private static final long serialVersionUID = 1L;

	ServerWindow() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		setSize(200,200);
		
		setTitle("Server");
		
		setVisible(true);
	}
}
