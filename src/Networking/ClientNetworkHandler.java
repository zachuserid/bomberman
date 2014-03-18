package Networking;

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
	private InetAddress address;
	private int port;

	/*
	 * Network status:
	 * 0 - Failure to register server IP
	 * 1 - Success status
	 */
	private int networkStatus;
	
	//Constructor
	
	//Accept a new return buffer to fill with <R> type data
    public ClientNetworkHandler(String ip, int port) {
    	super();
    	this.port = port;
    	this.networkStatus = 1;
		//Create an IP address object
    	try {
    		this.address = InetAddress.getByName(ip);
    	} catch (Exception e)
    	{
    		this.networkStatus = 0;
    	}
    }
    
    //Getters & Setters
    
    public int getNetworkStatus()
    {
    	return this.networkStatus;
    }

    //Methods
    
	@Override
    public DatagramSocket BindSocket() throws SocketException
    {
    	//System.out.println("Client: binding to socket, port: " + port);
    	return new DatagramSocket();
    }
    
    
	//Send the packet data to the server's network handler
	@Override
    protected void sendData(byte[] packet_data)
    {
			DatagramPacket sendPacket = new DatagramPacket(packet_data,
								packet_data.length, address, port);

			try 
			{
				//int highAck = Utils.byteArrayToInt(new byte[]{packet_data[1], packet_data[2], packet_data[3], packet_data[4]});
				//System.out.println("Sending: " + highAck);
				socket.send(sendPacket);
			} 
			catch(Exception e)
			{
				System.out.println("Failed to send data of length " + packet_data.length + ": " + e.getMessage());
			}
    }


}