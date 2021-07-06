package base.client.passive;

import javax.swing.JFrame;

public class PassiveClientWindow extends JFrame{
	private static final long serialVersionUID = 1L;

	PassiveClientWindow(){
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		setSize(300, 100);
		
		setTitle("Passive client");
		
		this.setVisible(true);
	}
}
