package base.client.controlling;

import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

public class ControllingClientWindow extends JFrame{
	private static final long serialVersionUID = 1L;
	private final DrawPane drawPane;
	private final ControllingClient controllingClient;
	
	private class DrawPane extends JPanel{
		static final long serialVersionUID = 1L;
		BufferedImage currentFrame;
		final Point latestMouseCoords = new Point(10, 10);
		
		DrawPane(){
			
			addMouseMotionListener(new MouseMotionListener() {
				@Override
				public void mouseDragged(MouseEvent e) {
					
				}

				@Override
				public void mouseMoved(MouseEvent e) {
					latestMouseCoords.x = e.getX();
					latestMouseCoords.y = e.getY();
				}
			});
			
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
		this.controllingClient = controllingClient;
		
		setSize(1600, 900);
		setLayout(new GridLayout());
		
		this.setupTopMenus();
		
		this.drawPane = new DrawPane();
		add(drawPane);
		
		setVisible(true);
	}
	
	void drawImage(BufferedImage image){
		drawPane.drawImage(image);
	}
	
	Point getLatestMouseCoordsObject() {
		return this.drawPane.latestMouseCoords;
	}
	
	void setupTopMenus() {
		JMenuBar jMenuBar = new JMenuBar();
		
		JCheckBoxMenuItem screenSharingBox = new JCheckBoxMenuItem("Screen sharing");
		screenSharingBox.setState(true);
		screenSharingBox.addActionListener(event -> {
			boolean screenSharing = ((JCheckBoxMenuItem) event.getSource()).getState();
			controllingClient.setScreenSharing(screenSharing);
		});
		
		
		jMenuBar.add(screenSharingBox);
		//TODO
		this.add(jMenuBar);
	}
}
