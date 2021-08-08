package base;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import base.client.controlling.ControllingClient;
import base.client.passive.PassiveClient;
import base.server.ServerApp;

public class Main {
	private static final String VALID_IP_ADDRESS_REGEX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]).){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
	private static final String VALID_HOSTNAME_REGEX = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]).)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9-]*[A-Za-z0-9])$";
	private static final String VALID_PORT_NUMBER_REGEX = "^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$";
	public static final String ENCODING = "UTF-8";
	
	public static void main(String[] args) {
	  //new TestClass(args[0]);
	  
		if(args.length != 1 && args.length != 0) {
			notifyOfFormatError();
		}else {
			try {
				if(args.length == 1)
					new ServerApp(Integer.parseInt(args[0]));
				else
					if(args.length == 0) {
						switch(chooseMode()) {
							case PassiveClient:
								new PassiveClient(
										getHostnameOrIPInput(),
										getPortNumberInput(),
										getPasswordInput());
								break;
							case ControllingClient:
								new ControllingClient(
										getHostnameOrIPInput(),
										getPortNumberInput(),
										getPasswordInput());
								break;
							case Server:
								new ServerApp(getPortNumberInput());
								break;
						}
					}
			}catch(NumberFormatException e) {
				notifyOfFormatError();
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	private static class TestClass{
	  BufferedInputStream graphicsInputStream;
	  BufferedOutputStream outputStream;
	  Robot screenCapturer;
	  Rectangle screenRectangle;
	  
	  long time0 = System.currentTimeMillis();
	  
	  boolean sent = false;
	  ByteArrayOutputStream byteArrayOutputStream;
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
	    System.out.println(this.time0 - (this.time0 = System.currentTimeMillis()) +
	        ", count: " + counter + ", read: " + (System.currentTimeMillis() - time0));
	    return img;
	  }
	  
	  
	  void sendScreenShot(BufferedImage screenShot) throws IOException{
	    long time0 = System.currentTimeMillis();
	    if (true) {
	      byteArrayOutputStream = new ByteArrayOutputStream();
	      ImageIO.write(screenShot, "PNG", byteArrayOutputStream);
	      //++counter;
	      //System.out.println(System.currentTimeMillis() + ", count: " + counter + ", encode: " + (System.currentTimeMillis() - time0));
	      byteArrayOutputStream.flush();
	      //sent = true;
	    }
	    ++counter;
	    outputStream.write(ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array());
	    
	    outputStream.write(byteArrayOutputStream.toByteArray());
	    outputStream.flush();
	    System.out.println(this.time0 - (this.time0 = System.currentTimeMillis()) + ", count: " + counter +
	        ", encode: " + (System.currentTimeMillis() - time0));
	    
	  }
	  
	  TestClass(String hostname) {
	    try {
	      if(hostname.equals("server")) {
	        var ss = new ServerSocket(50000);
	        Socket s = ss.accept();
	        graphicsInputStream = new BufferedInputStream(s.getInputStream());
	        var jframe = new JFrame();
	        jframe.setSize(1600,900);
	        jframe.setVisible(true);
	        var g = jframe.getGraphics();
	        
	        LinkedList<Long> framesList = new LinkedList<>();
	        for(int it = 0; it < 30; ++it) {
	          framesList.add(0L);
	        }
	        g.setFont(g.getFont().deriveFont(g.getFont().getSize() * 8f));
	        while(true) {
	          g.drawImage(receiveScreenShot(), 0, 0, null);
	          
	          g.drawString(
	              ""+(int)(
	                  (
	                    1000f / (
	                      System.currentTimeMillis() - framesList.pollLast()
	                    )
	                  ) * 30
	                )
	              ,100,100);
	          framesList.offerFirst(System.currentTimeMillis());
	        }
	      }else {
	        var s = new Socket(hostname, 50000);
	        outputStream = new BufferedOutputStream(s.getOutputStream());
	        this.screenCapturer = new Robot();
	        this.screenRectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
	        BufferedImage bi;
	        long time0;
	        while (true) {
	          time0 = System.currentTimeMillis();
	          bi = screenCapturer.createScreenCapture(screenRectangle);
	          System.out.println("Capture time: " + (System.currentTimeMillis() - time0));
	          sendScreenShot(bi);
	        }
	      }
	    } catch (IOException | AWTException e) {
	      e.printStackTrace();
	      System.exit(1);
	    }
	  }
	}
	
	enum Mode{
		PassiveClient,
		ControllingClient,
		Server;
	}
	private static Mode chooseMode() {
		return (Mode)JOptionPane.showInputDialog(null,
				"Choose operation mode",
				"Mode options",
				JOptionPane.QUESTION_MESSAGE,
				null,
				Mode.values(),
				Mode.values()[0]);
	}
	
	//TODO DRY
	private static int getPortNumberInput() {
		try{
			String portNumberStr = JOptionPane.showInputDialog(null, "Server port:", "28010");
			if(!portNumberStr.matches(VALID_PORT_NUMBER_REGEX))
				throw new NumberFormatException("Invalid port number format");
			return Integer.parseInt(portNumberStr);
		}catch(NumberFormatException e) {
			notifyOfFormatError();
			System.exit(1);
			return 0;
		}
		
	}
	
	private static String getHostnameOrIPInput() {
		String hostname = JOptionPane.showInputDialog(null, "Server hostname or IP: ", "localhost");
		if(!hostname.matches(VALID_HOSTNAME_REGEX) && !hostname.matches(VALID_IP_ADDRESS_REGEX)) {
			notifyOfFormatError();
			System.exit(1);
		}
		return hostname;
	}
	
	private static String getPasswordInput() {
		String password = JOptionPane.showInputDialog(null, "Password: ");
		if(password.isEmpty() || password.matches("\s") || password.length() > 25) {
			notifyOfFormatError("Password either too long(limited to max. 25 characters), or contains whitespaces");
			System.exit(1);
		}
		return password;
	}
	
	private static void notifyOfFormatError() {
	  notifyOfFormatError(null);
	}
	
	private static void notifyOfFormatError(String message) {
		JOptionPane.showMessageDialog(null, "Input format error. " + message, "Error", JOptionPane.ERROR_MESSAGE);
	}
}
