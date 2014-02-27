/**
 * @(#)ClientNetworkHandler.java
 *
 *
 * @author
 * @version 1.00 2014/2/24
 */

import java.util.concurrent.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public abstract class ClientNetworkHandler<S, R> extends NetworkHandler<S, R> {

	//Data members

	InetAddress address;
	int port;

	//Accept a new return buffer to fill with <R> type data
    public ClientNetworkHandler(S[] s1, S[] s2, R[] r1, R[] r2, InetAddress ip, int port) {
    	super(s1, s2, r1, r2);
    	this.address = ip;
    	this.port = port;
    }


    @Override
    public void BindSocket(DatagramSocket sock) throws SocketException
    {
    	sock = new DatagramSocket(port, address);
    }


}