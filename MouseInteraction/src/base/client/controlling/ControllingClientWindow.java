package base.client.controlling;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import base.client.InputType;

public class ControllingClientWindow extends JFrame{
	private static final long serialVersionUID = 1L;
	private final DrawPane drawPane;
	private final ControllingClient controllingClient;
	
	private class DrawPane extends JPanel{
		static final long serialVersionUID = 1L;
		BufferedImage currentFrame;
		final Point latestMouseCoords = new Point(10, 10);
		
		DrawPane(){
			
			addKeyListener(new KeyListener() {

				@Override
				public void keyTyped(KeyEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void keyPressed(KeyEvent e) {
					controllingClient.sendClick(e.getExtendedKeyCode(), InputType.KEYBOARD_PRESS);
				}

				@Override
				public void keyReleased(KeyEvent e) {
					controllingClient.sendClick(e.getExtendedKeyCode(), InputType.KEYBOARD_RELEASE);
				}
				
			});
			
			addMouseListener(new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent e) {
					
				}

				@Override
				public void mousePressed(MouseEvent e) {
					controllingClient.sendClick(e.getButton(), InputType.MOUSE_PRESS);
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					controllingClient.sendClick(e.getButton(), InputType.MOUSE_RELEASE);
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
			
			addMouseMotionListener(new MouseMotionListener() {
				@Override
				public void mouseDragged(MouseEvent e) {
					
				}

				@Override
				public void mouseMoved(MouseEvent e) {
					latestMouseCoords.x = e.getX();
					latestMouseCoords.y = e.getY();
					controllingClient.sendMouseMovement(e.getX(), e.getY());
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
		
		Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		Dimension maxDimension = new Dimension(maxBounds.width, maxBounds.height);
		setMaximumSize(maxDimension);
		setSize(maxDimension);
		
		setLayout(new GridLayout());
		
		setTitle("Controlling client");
		
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
		this.setJMenuBar(jMenuBar);
	}
}
