package base.client.passive;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import javax.imageio.ImageIO;

import base.ClientSocketType;
import base.client.Client;

public class PassiveClient extends Client{
	private class MediaSender extends Thread{
		OutputStream outputStream;
		Robot screenCapturer;
		Rectangle screenRectangle;
		
		MediaSender(Socket graphicsSocket) {
			try {
				this.outputStream = graphicsSocket.getOutputStream();
				this.screenCapturer = new Robot();
				this.screenRectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
			}catch(IOException | HeadlessException | AWTException  e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			
			this.start();
		}
		
		@Override
		public void run() {
			do {
				try {
					do {
						sendScreenShot(getScreenShot());
						Thread.sleep(SCREEN_REFRESH_DELAY);
					}while(true);
				}catch(IOException | InterruptedException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}while(true);
		}
		
		void sendScreenShot(BufferedImage screenShot) throws IOException{
			ImageIO.write(screenShot, "JPEG", outputStream);
		}
		
		BufferedImage getScreenShot() {
			return screenCapturer.createScreenCapture(screenRectangle);
		}
	}
	
	private class InputReceiver extends Thread{
		DataInputStream inputStream;
		Robot inputExecutor;
		
		InputReceiver(Socket inputSocket) {
			try {
				this.inputStream = new DataInputStream(inputSocket.getInputStream());
				this.inputExecutor = new Robot();
			}catch(IOException | AWTException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			this.start();
		}
		
		@Override
		public void run() {
			do{
				try {
					moveMouseTo(inputStream.readChar(), inputStream.readChar());
				}catch(IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}while(true);
		}
		
		void moveMouseTo(int x, int y) {
			inputExecutor.mouseMove(x, y);
		}
	}
	
	public PassiveClient(String hostname, int remotePort, String password) {
		launchClientSocketServicing(hostname, remotePort, password);
	}
	
	private void launchClientSocketServicing(String hostname, int remotePort, String password) {
		try {
			Socket graphicsSocket = logSocketOn(hostname, remotePort, password, ClientSocketType.GraphicsOutputSocket);
			new MediaSender(graphicsSocket);
			
			Socket inputSocket = logSocketOn(hostname, remotePort, password, ClientSocketType.InputSocket);
			new InputReceiver(inputSocket);
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
