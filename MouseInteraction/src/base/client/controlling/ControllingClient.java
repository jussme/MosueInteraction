package base.client.controlling;

import java.awt.event.MouseEvent;
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
		
		BufferedImage receiveScreenShot() throws IOException{
			byte[] sizeArray = new byte[4];
			graphicsInputStream.read(sizeArray);
			int size = ByteBuffer.wrap(sizeArray).asIntBuffer().get();
			String diag = "";
			for(var bytee : ByteBuffer.wrap(sizeArray).array()) {
				diag = diag + "," + bytee;
			}
			System.out.println(size + ", bytes:\n" + diag);
			byte[] imageByteBuffer = new byte[size];
			graphicsInputStream.read(imageByteBuffer);
			
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageByteBuffer));
			System.out.println(img);
			return img;
			
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
		controllingClientWindow = new ControllingClientWindow(this);
		launchClientSocketServicing(hostname, remotePort, password);
	}
	
	private void launchClientSocketServicing(String hostname, int remotePort, String password) {
		try {
			Socket metaCommSocket = logSocketOn(hostname, remotePort, password, ClientSocketType.MetaControllingSocket);
			new MetaCommunicator(metaCommSocket);
			
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
