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

import base.ClientSocketType;
import base.client.Client;

public class ControllingClient extends Client{
	private final ControllingClientWindow controllingClientWindow;
	private boolean screenSharing = true;
	private MediaReceiver mediaReceiver;
	
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
	}
	
	private class InputSender extends Thread {
		DataOutputStream dataOutputStream;
		final Point latestMouseCoords;
		int lastX = 10;
		int lastY = 10;
		
		InputSender(Socket outputSocket) throws IOException{
			this.dataOutputStream = new DataOutputStream(outputSocket.getOutputStream());
			this.latestMouseCoords = controllingClientWindow.getLatestMouseCoordsObject();
			
			this.start();
		}
		
		@Override
		public void run() {
			try {
				do {
					if(lastX != latestMouseCoords.x || lastY != latestMouseCoords.y) {
						dataOutputStream.writeChar(latestMouseCoords.x);
						dataOutputStream.writeChar(latestMouseCoords.y);
						lastX = latestMouseCoords.x;
						lastY = latestMouseCoords.y;
					}
					//System.out.println(System.currentTimeMillis());
					Thread.sleep(TOTAL_REFRESH_DELAY);
				}while(true);
			}catch(InterruptedException | IOException ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	public ControllingClient(String hostname, int remotePort, String password) {
		controllingClientWindow = new ControllingClientWindow(this);
		launchClientSocketServicing(hostname, remotePort, password);
	}
	
	private void launchClientSocketServicing(String hostname, int remotePort, String password) {
		try {
			Socket metaCommSocket = logSocketOn(hostname, remotePort, password, ClientSocketType.MetaControllingSocket);
			new MetaCommunicator(metaCommSocket);
			
			Socket graphicsInputSocket = logSocketOn(hostname, remotePort, password, ClientSocketType.GraphicsInputSocket);
			this.mediaReceiver = new MediaReceiver(graphicsInputSocket);
			
			Socket outputSocket = logSocketOn(hostname, remotePort, password, ClientSocketType.OutputSocket);
			new InputSender(outputSocket);
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	void setScreenSharing(boolean screenSharing) {
		this.screenSharing = screenSharing;
		if(screenSharing && !mediaReceiver.isAlive()) {
			mediaReceiver.start();
		}
	}
}
