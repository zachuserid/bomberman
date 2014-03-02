package Networking;

/**
 * @(#)AbstractServerNetworkHandler.java
 *
 *
 * @author
 * @version 1.00 2014/2/24
 */

import java.net.*;
import java.util.ArrayList;

public abstract class ServerNetworkHandler<S, R> extends NetworkHandler<S, R> {

	//Data members

	protected int listen_port;
	
    /*
	 * The list of players or spectators subscribed to
	 * receive updates regarding our game state.
	 */
	ArrayList<Subscriber> subscribers;

	//Constructor(s)

    public ServerNetworkHandler(int port) 
    {
    	super();
		listen_port = port;
		subscribers = new ArrayList<Subscriber>();
    }

    
    //Getters
    
    public ArrayList<Subscriber> getSubscribers()
    {
    	return this.subscribers;
    }

    
	//Methods

    protected void addSubscriber(String name, InetAddress addr, int port)
    {
    	System.out.println("New player: " + name);
    	subscribers.add( new Subscriber(name, addr, port) );
    }
    
    
	//binds socket for either client or servers
	@Override
    protected DatagramSocket BindSocket() throws SocketException
    {
    	//System.out.println("Server: binding to port " + this.listen_port);
		return new DatagramSocket(this.listen_port);
    }
    
    
    //Send data to all registered subscribers
	@Override
    protected void Send(byte[] packet_data)
    {

			ArrayList<Subscriber> spectators = this.getSubscribers();
			
			System.out.println("++length of subscribers: " + spectators.size());
			
			//Iterate over all subscribers
			for (Subscriber client: spectators){
				System.out.println("The server is sending to spectator with port " + client.getPort());


				//Send this spectator the data
				DatagramPacket sendPacket = new DatagramPacket(packet_data, packet_data.length,
																client.getAddr(), client.getPort());
				
				try {
					
					System.out.println("Sending data: '" + new String(packet_data) + "' to " + client.getPort());
					socket.send(sendPacket);
					
				} catch(Exception e){
					
					System.out.println("Failed to send data of length " + packet_data.length + ". " + e.getMessage());
					
				}
			}
    }


}