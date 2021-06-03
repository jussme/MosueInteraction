package base.client.controlling;

import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public class ControllingClientWindow extends JFrame{
	private static final long serialVersionUID = 1L;
	private final ControllingClient controllingClient;
	//private final DrawPane drawPane;
	/*
	private class DrawPane extends JPanel{
		void drawImage(BufferedImage image){
			var graphics = this.getGraphics();
			graphics.drawImage(image, 0, 0, null);
		}
	}
	*/
	public ControllingClientWindow(ControllingClient controllingClient) {
		setSize(1600, 900);
		setLayout(new GridLayout());
		
		//this.drawPane = new DrawPane();
		//add(drawPane);
		
		this.controllingClient = controllingClient;
		
		addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {
				
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				controllingClient.receiveMouseEvent(e);
			}
		});
		
		setVisible(true);
	}
	
	void drawImage(BufferedImage image){
		var graphics = this.getGraphics();
		graphics.drawImage(image, 0, 0, null);
	}
}
