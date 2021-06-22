package base.client.controlling;

import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ControllingClientWindow extends JFrame{
	private static final long serialVersionUID = 1L;
	private final ControllingClient controllingClient;
	private final DrawPane drawPane;
	
	private class DrawPane extends JPanel{
		private static final long serialVersionUID = 1L;
		BufferedImage currentFrame;
		DrawPane(){
			ControllingClientWindow.this.add(this);
			
			
			
			setVisible(true);
			
		}
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(currentFrame, 0, 0, null);
		}
		
		void drawImage(BufferedImage image){
			this.currentFrame = image;
			repaint();
		}
	}
	
	public ControllingClientWindow(ControllingClient controllingClient) {
		setSize(1600, 900);
		setLayout(new GridLayout());
		
		this.drawPane = new DrawPane();
		
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
		//var graphics = this.getGraphics();
		//graphics.drawImage(image, 0, 0, null);
		drawPane.drawImage(image);
	}
}
