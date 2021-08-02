package base.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

import base.client.ClientSocketType;

class Mediator{
  Object[] sockets = new Object[6];
  
  Mediator(Object socket, ClientSocketType clientSocketType) {
    sockets[clientSocketType.getIntType()] = socket;
  }
  
  private boolean allSocketsBound() {
    for(Object s : sockets) {
      if(s == null) {
        return false;
      }
    }
    
    return true;
  }
  
  boolean bindSocket(Object clientSocket, ClientSocketType clientSocketType) {
    if(sockets[clientSocketType.getIntType()] == null 
        && clientSocket.getClass() == clientSocketType.getCorrespondingClass())
    {
      sockets[clientSocketType.getIntType()] = clientSocket;

      if(allSocketsBound()) {
        transferAllSockets();
      }
      
      return true;
    }else {
      return false;
    }
  }

  private void transferAllSockets() {
    for(int it = 5; it > 2; --it) {
      final int constBuff = it;
      new Thread(() -> {
        try {
          switch (ClientSocketType.valueOf(constBuff)) {
            case OutputSocket:
              DatagramSocket serverInUDP = (DatagramSocket) sockets[constBuff];
              DatagramSocket serverOutUDP = (DatagramSocket) sockets[ClientSocketType.getNOfTypes() - 1 - constBuff];
              byte[] buf = new byte[6];
              DatagramPacket packet = new DatagramPacket(buf, buf.length);
              while(true) {
                serverInUDP.receive(packet);
                serverOutUDP.send(packet);
              }
            default:
              Socket serverInTCP = (Socket) sockets[constBuff];
              Socket serverOutTCP = (Socket) sockets[ClientSocketType.getNOfTypes() - 1 - constBuff];
              serverInTCP.getInputStream().transferTo(serverOutTCP.getOutputStream());
              break;
          }
        } catch (IOException e) {
          e.printStackTrace();
          System.exit(1);
        }
      }).start();
    }
  }
}