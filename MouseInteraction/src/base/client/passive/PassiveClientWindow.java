package base.client.passive;

import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public class PassiveClientWindow extends JFrame{
	private static final long serialVersionUID = 1L;

	PassiveClientWindow(){
		setSize(1600, 900);
		
		
		this.setVisible(true);
	}
	
	public BufferedImage drawImage(BufferedImage image) {
		this.getGraphics().drawImage(image, 0, 0, null);
		return image;
	}
}
