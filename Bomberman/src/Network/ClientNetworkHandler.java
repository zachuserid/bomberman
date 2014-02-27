package Network;

/**
 * @(#)ClientNetworkHandler.java
 *
 *
 * @author
 * @version 1.00 2014/2/24
 */

import java.net.*;

public abstract class ClientNetworkHandler<S, R> extends NetworkHandler<S, R> {

	//Data members

	//The server's communication details
	InetAddress address;
	int port;

	
	//Methods
	
	//Accept a new return buffer to fill with <R> type data
    public ClientNetworkHandler(S[] s1, S[] s2, R[] r1, R[] r2, InetAddress ip, int port) {
    	super(s1, s2, r1, r2);
    	this.address = ip;
    	this.port = port;
    }


    @SuppressWarnings("resource")
	@Override
    public void BindSocket(DatagramSocket sock) throws SocketException
    {
    	System.out.println("Client: binding to socket, port: " + port);
    	sock = new DatagramSocket(port, address);
    }
    
    
	//Send the packet data to the server's network handler
	@Override
    protected void Send(byte[] packet_data)
    {
			DatagramPacket sendPacket = new DatagramPacket(packet_data,
								packet_data.length, address, port);
			try {
				System.out.println("Sending data: " + new String(packet_data) + " to " + port);
				socket.send(sendPacket);
			} catch(Exception e){
				System.out.println("Failed to send data of length " + packet_data.length);
			}
    }


}