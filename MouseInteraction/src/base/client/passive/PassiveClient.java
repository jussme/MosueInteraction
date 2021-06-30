package base.client.passive;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import base.client.Client;
import base.client.ClientSocketType;
import base.client.InputType;

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
					Thread.sleep(TOTAL_REFRESH_DELAY);
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
			
			outputStream.write(byteArrayOutputStream.toByteArray());
			outputStream.flush();
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
				
				this.setPriority(MAX_PRIORITY);
			}catch(IOException | AWTException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			this.start();
		}
		
		@Override
		public void run() {
			try {
				int x = 0, y = 0;
				InputType inputType;
				do {
					inputType = InputType.valueOf(inputStream.readChar());
					switch(inputType) {
						case MOUSE_MOVEMENT:
							x = inputStream.readChar();
							y = inputStream.readChar();
							inputExecutor.mouseMove(x, y);
							System.out.println(System.currentTimeMillis() + ", x = " + x + ", y = " + y);
							break;
						case MOUSE_PRESS:
							int button = InputEvent.getMaskForButton(inputStream.readChar());
							inputExecutor.mousePress(button);
							System.out.println("button" + button);
							break;
						case MOUSE_RELEASE:
							inputExecutor.mouseRelease(InputEvent.getMaskForButton(inputStream.readChar()));
							break;
						case KEYBOARD_PRESS:
							inputExecutor.keyPress(inputStream.readChar());
							break;
						case KEYBOARD_RELEASE:
							inputExecutor.keyRelease(inputStream.readChar());
							break;
						default:
							throw new IllegalArgumentException();
					}
				}while(true);
			}catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
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
