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

public abstract class AbstractServerNetworkHandler<S, R> extends NetworkHandler<S, R> {

	//server specific members
	int port;
	//Size of buffer receive
	public static final int MAX_BUF = 5000;
	//Maximum UDP payload size, rounding down
	public static final int MAX_PAYLOAD_LEN = 64000;
	//number of players in a game
	private int MAX_PLAYERS = 4;
	/*
	 * The list of players or spectators subscribed to
	 * receive updates regarding our game state.
	 */
	ArrayList<Subscriber> spectators;
	//Socket on which to accept
	DatagramSocket sockfd;
	//An executor for running working threads
	ExecutorService executor;
	 
	
    public AbstractServerNetworkHandler(int p) {
		port = p;
		executor = Executors.newCachedThreadPool();
    }

    void handleNewPlayer(){
    }

    R parseData(){
    	return (R)null;
    }

}