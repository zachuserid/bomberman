/**
 * @(#)AbstractServerNetworkHandler.java
 *
 *
 * @author
 * @version 1.00 2014/2/24
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ServerNetworkHandler<S, R> extends NetworkHandler<S, R> {

	//Data members

	protected int listen_port;

	//Constructor(s)
	/*
	 * Take in an empty R array to work with
	 */
    public ServerNetworkHandler(S[] as, S[] bs, R[] ar, R[] br, int port) {
    	super(as, bs, ar, br);
		listen_port = port;
    }


	//Methods

	//binds socket for either client or server
    @Override
    protected void BindSocket(DatagramSocket socket) throws SocketException
    {
		socket = new DatagramSocket(this.listen_port);
    }


}