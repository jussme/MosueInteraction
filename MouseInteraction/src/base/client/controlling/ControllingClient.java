package base.client.controlling;

import static base.client.InputType.MOUSE_MOVEMENT;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import base.client.Client;
import base.client.ClientSocketType;
import base.client.InputType;

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
				graphicsInputSocket.setReceiveBufferSize(64000);
				
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
		int counter = 0;
		BufferedImage receiveScreenShot() throws IOException{
			byte[] sizeArray = new byte[4];
			graphicsInputStream.read(sizeArray);
			long time0 = System.currentTimeMillis();
			int size = ByteBuffer.wrap(sizeArray).asIntBuffer().get();
			
			byte[] imageByteBuffer = new byte[size];
			graphicsInputStream.readNBytes(imageByteBuffer, 0, size);
			
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageByteBuffer));
			++counter;
			System.out.println(System.currentTimeMillis() + ", count: " + counter + ", read: " + (System.currentTimeMillis() - time0));
			return img;
		}
		
		void sleepUntillWindowInstantiated() {
		  try{
        while(controllingClientWindow == null) {
          Thread.sleep(5);
        };
      }catch(InterruptedException e) {
        e.printStackTrace();
        System.exit(1);
      }
		}
		
		@Override
		public void run() {
			LinkedList<Long> framesList = new LinkedList<>();
			for(int it = 0; it < 30; ++it) {
				framesList.add(0L);
			}
			
			sleepUntillWindowInstantiated();
			
			try {
				do {
					controllingClientWindow.drawImage(receiveScreenShot());
					controllingClientWindow.showFPS(
						(int)(
								(
									1000f / (
										System.currentTimeMillis() - framesList.pollLast()
									)
								) * 30
							)
						);
					framesList.offerFirst(System.currentTimeMillis());
				}while(screenSharing);
			}catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		MediaReceiver cloneMediaReceiver() {
			return new MediaReceiver(this.graphicsInputStream);
		}
	}
	
	private class InputSender {
		DataOutputStream dataOutputStream;
		
		InputSender(Socket outputSocket) throws IOException{
		  outputSocket.setSendBufferSize(8);
		  
			this.dataOutputStream = new DataOutputStream(outputSocket.getOutputStream());
		}
		
		void sendMouseMovement(int x, int y) {
			try {
				synchronized(dataOutputStream) {
					dataOutputStream.writeChar(MOUSE_MOVEMENT.getIntType());//TODO < 2 bytes
					dataOutputStream.writeChar(x);
					dataOutputStream.writeChar(y);
					dataOutputStream.flush();
					System.out.println(System.currentTimeMillis() + ", mouse movement");
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
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
			Socket metaCommSocket = logTCPSocketOn(hostname, remotePort, password, ClientSocketType.MetaControllingSocket);
			new MetaCommunicator(metaCommSocket);
			
			Socket graphicsInputSocket = logTCPSocketOn(hostname, remotePort, password, ClientSocketType.GraphicsInputSocket);
			this.mediaReceiver = new MediaReceiver(graphicsInputSocket);
			
			Socket outputSocket = logTCPSocketOn(hostname, remotePort, password, ClientSocketType.OutputSocket);
			this.inputSender = new InputSender(outputSocket);
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	void setScreenSharing(boolean screenSharing) {
		this.screenSharing = screenSharing;
		if(screenSharing) {
			if(mediaReceiver.isAlive()) {
				try {
					mediaReceiver.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.exit(1);
				}
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
