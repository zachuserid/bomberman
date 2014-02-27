package Network;

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

    public ServerNetworkHandler(S[] as, S[] bs, R[] ar, R[] br, int port) {
    	super(as, bs, ar, br);
		listen_port = port;
    }

    
    //Getters
    
    public ArrayList<Subscriber> getSubscribers()
    {
    	return this.subscribers;
    }

    
	//Methods

    protected void addSubscriber(InetAddress addr, int port)
    {
    	subscribers.add( new Subscriber(addr, port) );
    }
    
    
	//binds socket for either client or server
    @SuppressWarnings("resource")
	@Override
    protected void BindSocket(DatagramSocket socket) throws SocketException
    {
		socket = new DatagramSocket(this.listen_port);
		System.out.println("Server: binding to port " + this.listen_port);
    }
    
    
    //Send data to all registered subscribers
	@Override
    protected void Send(byte[] packet_data)
    {

			ArrayList<Subscriber> spectators = this.getSubscribers();
			//Iterate over all subscribers
			for (Subscriber client: spectators){

				//Send this spectator the data
				DatagramPacket sendPacket = new DatagramPacket(packet_data, packet_data.length,
																client.getAddr(), client.getPort());
				try {
					System.out.println("Sending data: " + new String(packet_data) + " to " + client.getPort());
					socket.send(sendPacket);
				} catch(Exception e){
					System.out.println("Failed to send data of length " + packet_data.length);
				}
			}
    }


}