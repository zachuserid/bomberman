package Networking;

/**
 * @(#)Subscriber.java
 *
 * Represents an instance of a player
 * or spectator from the server's point of view.
 * This will store our communication requirements
 * for the specific client.
 *
 * Any future details regarding a client,
 * that the server must maintain will be
 * maintained in this class.
 *
 * @author
 * @version 1.00 2014/2/23
 */

import java.net.*;

public class Subscriber {

	//the client's IP address
	private InetAddress address;
	//The client's port
	private int port;
	//The highest received acknowledgment
	private int ackCount;
	//The subscriber's name
	private String name;

    public Subscriber(String name, InetAddress addr, int prt) {
    	this.name = name;
    	this.address = addr;
    	this.port = prt;
    	this.ackCount = 0;
    }

    public InetAddress getAddr()
    {
    	return this.address;
    }

    public int getPort()
    {
    	return this.port;
    }
    
    public String getName()
    {
    	return this.name;
    }
    
    public int getAckCount(){
    	return this.ackCount;
    }
    
    public void setAckCount(int ack){
    	this.ackCount = ack;
    }

}