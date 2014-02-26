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
import java.util.regex.*;

public abstract class AbstractServerNetworkHandler<S, R> extends NetworkHandler<S, R> {

	//Server specific members
	int listen_port;
	//The servers socket struct
	DatagramSocket socket;
	//number of players in a game
	private int MAX_PLAYERS = 4;
	/*
	 * The list of players or spectators subscribed to
	 * receive updates regarding our game state.
	 */
	ArrayList<Subscriber> spectators;
	//Socket on which to accept
	DatagramSocket sockfd;

	String joinRE = "\\|\\|\\|.*?\\|\\|\\|";
	Pattern joinPat = Pattern.compile(joinRE);

	
	/*
	 * Take in an empty R array to work with
	 */
    public AbstractServerNetworkHandler(int port) {
		listen_port = port;
    }

    void handleNewPlayer(String name, InetAddress ip, int port){
    	if ( !canAddPlayer() ){
    		return;
    	}

		spectators.add( (new Subscriber(ip, port)) );

    }


    /*
     * If any commands are player joins, add the player.
     * Otherwise, lock on the write buffer and buffer the packet
     */
    void bufferData(DatagramPacket packet){

    	//Handle any join requests
    	Matcher m = joinPat.matcher(buf);
    	while(m.find()){
    		//send player details to handleNewPlayer()
    		String name = m.group().split(",")[1];
    		handleNewPlayer(name, packet.getAddress(), packet.getPort());
    	}

    	//Remove these join messages from the other commands
		buf = buf.replaceAll(joinRE, "");


    }


    private boolean canAddPlayer(){
		//Add any failing conditions here for cases that
		// players cannot be added
		if ( spectators.size() >= MAX_PLAYERS ){
			return false;
		}

		return true;
	}


}