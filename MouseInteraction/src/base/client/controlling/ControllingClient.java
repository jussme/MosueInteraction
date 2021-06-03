package base.client.controlling;

import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import javax.imageio.ImageIO;

import base.ClientSocketType;
import base.client.Client;

public class ControllingClient extends Client{
	private final ControllingClientWindow controllingClientWindow;
	private InputSender inputSender;
	
	private class MediaReceiver extends Thread{
		InputStream graphicsInputStream;
		
		MediaReceiver(Socket graphicsInputSocket){
			try {
				graphicsInputStream = graphicsInputSocket.getInputStream();
			}catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		BufferedImage receiveScreenShot() throws IOException{
			return ImageIO.read(graphicsInputStream);
		}
		
		@Override
		public void run() {
			do {
				try {
					controllingClientWindow.drawImage(receiveScreenShot());
					Thread.sleep(SCREEN_REFRESH_DELAY);
				}catch(IOException | InterruptedException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}while(true);
		}
	}
	
	private class InputSender extends Thread {
		DataOutputStream dataOutputStream;
		
		InputSender(Socket outputSocket) throws IOException{
			this.dataOutputStream = new DataOutputStream(outputSocket.getOutputStream());
		}
		
		void receiveMouseEvent(MouseEvent e) {
			try {
				dataOutputStream.writeChar(e.getX());
				dataOutputStream.writeChar(e.getY());		
			}catch(IOException ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	public ControllingClient(String hostname, int remotePort, String password) {
		launchClientSocketServicing(hostname, remotePort, password);
		controllingClientWindow = new ControllingClientWindow(this);
	}
	
	private void launchClientSocketServicing(String hostname, int remotePort, String password) {
		try {
			Socket graphicsInputSocket = logSocketOn(hostname, remotePort, password, ClientSocketType.GraphicsInputSocket);
			new MediaReceiver(graphicsInputSocket);
			
			Socket outputSocket = logSocketOn(hostname, remotePort, password, ClientSocketType.OutputSocket);
			this.inputSender = new InputSender(outputSocket);
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void receiveMouseEvent(MouseEvent e) {
		inputSender.receiveMouseEvent(e);
	}
}
