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
import java.net.DatagramSocket;
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
					//Thread.sleep(TOTAL_REFRESH_DELAY);
				}while(true);
			}catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		int counter = 0;
		void sendScreenShot(BufferedImage screenShot) throws IOException{
			long time0 = System.currentTimeMillis();
			var byteArrayOutputStream = new ByteArrayOutputStream();
			ImageIO.write(screenShot, "png", byteArrayOutputStream);
			byteArrayOutputStream.flush();
			
			outputStream.write(ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array());
			
			outputStream.write(byteArrayOutputStream.toByteArray());
			outputStream.flush();
			++counter;
			System.out.println(System.currentTimeMillis() + ", count: " + counter + ", write: " + (System.currentTimeMillis() - time0));
		}
		
		BufferedImage getScreenShot() {
			return screenCapturer.createScreenCapture(screenRectangle);
		}
	}
	
	private class InputReceiver extends Thread{
		DataInputStream inputStream;
		Robot inputExecutor;
		
		InputReceiver(DatagramSocket inputSocket) {
			try {
			  inputSocket.setReceiveBufferSize(8);
			  
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
					x = inputStream.readChar();
					switch(inputType) {
						case MOUSE_MOVEMENT:
							y = inputStream.readChar();
							inputExecutor.mouseMove(x, y);
							System.out.println(System.currentTimeMillis() + ", mouse movement");
							break;
						case MOUSE_PRESS:
							inputExecutor.mousePress(InputEvent.getMaskForButton(x));
							break;
						case MOUSE_RELEASE:
							inputExecutor.mouseRelease(InputEvent.getMaskForButton(x));
							break;
						case KEYBOARD_PRESS:
							inputExecutor.keyPress(x);
							break;
						case KEYBOARD_RELEASE:
							inputExecutor.keyRelease(x);
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
			Socket metaCommSocket = logTCPSocketOn(hostname, remotePort, password, ClientSocketType.MetaPassiveSocket);
			new MetaCommunicator(metaCommSocket);
			
			Socket graphicsSocket = logTCPSocketOn(hostname, remotePort, password, ClientSocketType.GraphicsOutputSocket);
			new MediaSender(graphicsSocket);
			
			DatagramSocket inputSocket = logUDPSocketOn(hostname, remotePort, password, ClientSocketType.InputSocket);
			new InputReceiver(inputSocket);
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
