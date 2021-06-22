package base.client.passive;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import base.ClientSocketType;
import base.client.Client;

public class PassiveClient extends Client{
	final PassiveClientWindow window = new PassiveClientWindow();
	
	private class MediaSender extends Thread{
		BufferedOutputStream outputStream;
		Robot screenCapturer;
		Rectangle screenRectangle;
		
		MediaSender(Socket graphicsSocket) {
			try {
				this.outputStream = new BufferedOutputStream(graphicsSocket.getOutputStream());
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
			try {
				do {
					sendScreenShot(getScreenShot());
					Thread.sleep(SCREEN_REFRESH_DELAY);
				}while(true);
			}catch(IOException | InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		void sendScreenShot(BufferedImage screenShot) throws IOException{
			var byteArrayOutputStream = new ByteArrayOutputStream();
			ImageIO.write(screenShot, "jpg", byteArrayOutputStream);
			byteArrayOutputStream.flush();
			outputStream.write(ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array());
			//size sent
			//for(var arrByte : byteArrayOutputStream.toByteArray())
			//	System.out.print(arrByte + ",");
			String diag = "";
			for(var bytee : ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array())
				diag = diag + "," + bytee;
			
			System.out.println("size: " + byteArrayOutputStream.size() + ", bytes:\n" + diag);
			outputStream.write(byteArrayOutputStream.toByteArray());
			outputStream.flush();
			//image sent
			System.out.println("image sent");
		}
		
		BufferedImage getScreenShot() {
			return window.drawImage(screenCapturer.createScreenCapture(screenRectangle));
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
	
	private class MetaCommunicator extends Thread {
		
		MetaCommunicator(Socket metaCommSocket){
			
		}
	}
	
	public PassiveClient(String hostname, int remotePort, String password) {
		launchClientSocketServicing(hostname, remotePort, password);
	}
	
	private void launchClientSocketServicing(String hostname, int remotePort, String password) {
		try {
			Socket metaCommSocket = logSocketOn(hostname, remotePort, password, ClientSocketType.MetaPassiveSocket);
			new MetaCommunicator(metaCommSocket);
			
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
