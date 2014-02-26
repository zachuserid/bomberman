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
    public AbstractServerNetworkHandler(R[] ret, int port) {
    	super(ret);
		listen_port = port;
    }

    void handleNewPlayer(String name, InetAddress ip, int port){
    	if ( !canAddPlayer() ){
    		return;
    	}

		spectators.add( (new Subscriber(ip, port)) );

    }

	/*
	 * parseData() will handle individual player messages.
	 * It will gather the various command information
	 * and use it to create a new instance of a R
	 * from a factory method.
	 */
    PlayerUpdate[] parseData(byte[] data){
		//TODO: Figure out a workaround for implementing
		// this method with generic types.. can't use factory methods
		// on a static type.

    	return (PlayerUpdate[])null;
    }

    /*
     * If any commands are player joins, add the player.
     * Otherwise, lock on the write buffer and buffer the packet
     */
    void bufferData(DatagramPacket packet){

    	String buf = new String(packet.getData());

    	//Handle any join requests
    	Matcher m = joinPat.matcher(buf);
    	while(m.find()){
    		//send player details to handleNewPlayer()
    		String name = m.group().split(",")[1];
    		handleNewPlayer(name, packet.getAddress(), packet.getPort());
    	}

    	//Remove these join messages from the other commands
		buf = buf.replaceAll(joinRE, "");

		byte bytes[] = buf.getBytes();

		updateWritable.acquireUninterruptibly();

		for (int j=0; j<bytes.length; j++){
			playerUpdateBufferIn[uIn++] = bytes[j];
		}

		updateWritable.release();


    }

	//THIS METHOD WILL BE RUN IN A SEPERATE THREAD
    public void initServer(){
		try {
			socket = new DatagramSocket(listen_port);
		} catch (SocketException e){
			System.out.println("A socket error occured: " + e.getMessage());
			return;
		} catch (Exception e){
			System.out.println("An unexpected error occured: " + e.getMessage());
			return;
		}

		byte[] receiveData = new byte[MAX_BUF];

		//Our infinite loop of accepting and handling packets
		while(true){
			//Construct a packet to be filled by network layer OS(Java VM) call
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try {
				socket.receive(receivePacket);
			} catch (IOException e){
				//TODO: log a packet read error
			}
			/*
			 * TODO: parse the received packet and determine command
			 * and call appropriate handling method.
			 *
			 * We will send all packets from the network to bufferData()
			 * IMPORTANT: We only want to send it one packet at a time,
			 * as this will represent one players commands. The network layer
			 * may do this for us.
			 *
			 */

			 bufferData(receivePacket);


		}
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