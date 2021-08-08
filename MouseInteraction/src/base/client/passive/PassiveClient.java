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
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import base.client.Client;
import base.client.ClientSocketType;
import base.client.InputType;
import base.client.controlling.ControllingClient;

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
		DatagramSocket datagramSocket;
		DatagramPacket packet;
		byte[] payload;
		
		Robot inputExecutor;
		
		InputReceiver(DatagramSocket inputSocket) {
			try {
			  //inputSocket.setReceiveBufferSize(8);
			  
				this.datagramSocket = inputSocket;
				this.inputExecutor = new Robot();
				
	      this.payload = new byte[ControllingClient.InputSender.MAX_PAYLOAD_LENGTH];
	      this.packet = new DatagramPacket(payload, payload.length);
				
				this.setPriority(MAX_PRIORITY);
			}catch(AWTException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			this.start();
		}
		
		@Override
		public void run() {int counter = 0;
		  try {
        int x = 0, y = 0;
        InputType inputType;
        do {
          datagramSocket.receive(packet);
          inputType = InputType.valueOf(payload[0]);
          x = Byte.toUnsignedInt(payload[1]) + (Byte.toUnsignedInt(payload[2]) << 8);
          switch(inputType) {
            case MOUSE_MOVEMENT:
              y = Byte.toUnsignedInt(payload[3]) + (Byte.toUnsignedInt(payload[4]) << 8);
              inputExecutor.mouseMove(x, y);System.out.println("Counter: " + ++counter + ", " + x + ":" + y);
              //System.out.println(System.currentTimeMillis() + ", mouse movement: " + x + ";" + y);
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
