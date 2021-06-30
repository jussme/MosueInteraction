package base.client.controlling;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import base.client.Client;
import base.client.ClientSocketType;
import base.client.InputType;

import static base.client.InputType.*;

public class ControllingClient extends Client{
	private final ControllingClientWindow controllingClientWindow;
	private boolean screenSharing = true;
	private MediaReceiver mediaReceiver;
	private InputSender inputSender;
	
	private class MetaCommunicator extends Thread {
		
		MetaCommunicator(Socket metaCommSocket){
			
		}
	}
	
	private class MediaReceiver extends Thread{
		BufferedInputStream graphicsInputStream;
		
		MediaReceiver(Socket graphicsInputSocket){
			try {
				graphicsInputStream = new BufferedInputStream(graphicsInputSocket.getInputStream());
				
				this.start();
			}catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		MediaReceiver(BufferedInputStream graphicsInputStream){
			this.graphicsInputStream = graphicsInputStream;
			
			this.start();
		}
		
		BufferedImage receiveScreenShot() throws IOException{
			byte[] sizeArray = new byte[4];
			graphicsInputStream.read(sizeArray);
			
			int size = ByteBuffer.wrap(sizeArray).asIntBuffer().get();
			
			byte[] imageByteBuffer = new byte[size];
			graphicsInputStream.readNBytes(imageByteBuffer, 0, size);
			
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageByteBuffer));
		
			return img;
		}
		
		@Override
		public void run() {
			try {
				do {
					controllingClientWindow.drawImage(receiveScreenShot());
					Thread.sleep(TOTAL_REFRESH_DELAY);
				}while(screenSharing);
			}catch(IOException | InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		MediaReceiver cloneMediaReceiver() {
			return new MediaReceiver(this.graphicsInputStream);
		}
	}
	
	private class InputSender extends Thread {
		DataOutputStream dataOutputStream;
		final Point latestMouseCoords;
		int lastX = 10;
		int lastY = 10;
		
		InputSender(Socket outputSocket) throws IOException{
			this.dataOutputStream = new DataOutputStream(outputSocket.getOutputStream());
			this.latestMouseCoords = controllingClientWindow.getLatestMouseCoordsObject();
			
			//this.start();
		}
		
		@Override
		public void run() {
			try {
				do {
					if(lastX != latestMouseCoords.x || lastY != latestMouseCoords.y) {
						dataOutputStream.writeChar(latestMouseCoords.x);
						dataOutputStream.writeChar(latestMouseCoords.y);
						//dataOutputStream.flush();
						System.out.println(System.currentTimeMillis() + ", x = " + lastX + ", y = " + lastY);
						lastX = latestMouseCoords.x;
						lastY = latestMouseCoords.y;
					}
					
					Thread.sleep(TOTAL_REFRESH_DELAY);
				}while(true);
			}catch(InterruptedException | IOException ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		}
		
		void sendMouseMovement(int x, int y) {
			try {
				synchronized(dataOutputStream) {
					dataOutputStream.writeChar(MOUSE_MOVEMENT.getIntType());
					dataOutputStream.writeChar(x);
					dataOutputStream.writeChar(y);
					dataOutputStream.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		void sendClick(int code, InputType inputType) {
			try {
				synchronized(dataOutputStream) {
					dataOutputStream.writeChar(inputType.getIntType());
					dataOutputStream.writeChar(code);
					dataOutputStream.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public ControllingClient(String hostname, int remotePort, String password) {
		launchClientSocketServicing(hostname, remotePort, password);
		controllingClientWindow = new ControllingClientWindow(this);
	}
	
	private void launchClientSocketServicing(String hostname, int remotePort, String password) {
		try {
			Socket metaCommSocket = logSocketOn(hostname, remotePort, password, ClientSocketType.MetaControllingSocket);
			new MetaCommunicator(metaCommSocket);
			
			Socket graphicsInputSocket = logSocketOn(hostname, remotePort, password, ClientSocketType.GraphicsInputSocket);
			this.mediaReceiver = new MediaReceiver(graphicsInputSocket);
			
			Socket outputSocket = logSocketOn(hostname, remotePort, password, ClientSocketType.OutputSocket);
			this.inputSender = new InputSender(outputSocket);
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	void setScreenSharing(boolean screenSharing) {
		this.screenSharing = screenSharing;
		if(screenSharing && !mediaReceiver.isAlive()) {
			try {
				mediaReceiver.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
			mediaReceiver = mediaReceiver.cloneMediaReceiver();
		}
	}
	
	void sendMouseMovement(int x, int y) {
		inputSender.sendMouseMovement(x, y);
	}
	
	void sendClick(int code, InputType inputType) {
		inputSender.sendClick(code, inputType);
	}
}
