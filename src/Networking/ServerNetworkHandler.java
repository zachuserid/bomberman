package Networking;

/**
 * @(#)AbstractServerNetworkHandler.java
 *
 *
 * @author
 * @version 1.00 2014/2/24
 */

import java.net.*;
import java.util.Collection;
import java.util.HashMap;

public abstract class ServerNetworkHandler<S, R> extends NetworkHandler<S, R> {

	//Data members

	protected int listen_port;
	
    /*
	 * The list of players or spectators subscribed to
	 * receive updates regarding our game state.
	 */
	HashMap<String,Subscriber> subscribers;

	//Constructor(s)

    public ServerNetworkHandler(int port) 
    {
    	super();
		listen_port = port;
		subscribers = new HashMap<String,Subscriber>();
    }

    
    //Getters
    
    public Collection<Subscriber> getSubscribers()
    {
    	return this.subscribers.values();
    }

    public void Send(String name, S data)
    {
    	this.sendData(name, this.parseSend(data));
    }
    
    public Subscriber getSubscriberByName(String name)
    {
    	return this.subscribers.get(name);
    }
    
    
	//Methods
    protected void addSubscriber(String name, InetAddress addr, int port)
    {
    	if ( name.trim().equals("") ) System.out.println("New spectator");
    	else System.out.println("New player: " + name);
    	
    	subscribers.put(name, new Subscriber(name, addr, port) );
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
    protected void sendData(byte[] packet_data)
    {

			Collection<Subscriber> subs = this.getSubscribers();
						
			//Iterate over all subscribers
			for (Subscriber client: subs) this.sendData(client.getName(), packet_data);
    }
	
    protected void sendData(String name, byte[] packet_data)
    {

    		Subscriber sub = this.subscribers.get(name);
    		if(sub == null) return;
    		
			//Send this spectator the data
			DatagramPacket sendPacket = new DatagramPacket(packet_data, packet_data.length,
															sub.getAddr(), sub.getPort());
				
			try {socket.send(sendPacket);} 
			catch(Exception e)
			{
				System.out.println("Failed to send data of length " + packet_data.length + ". " + e.getMessage());
			}
    }
}