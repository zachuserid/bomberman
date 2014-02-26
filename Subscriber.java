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

import java.io.*;
import java.net.*;

public class Subscriber {

	//the client's IP address
	private InetAddress address;
	//The client's port
	private int port;

    public Subscriber(InetAddress addr, int prt) {
    	address = addr;
    	port = prt;
    }

    public InetAddress getAddr(){
    	return address;
    }

    public int getPort(){
    	return port;
    }

}