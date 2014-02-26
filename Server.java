/**
 * @(#)Server.java
 *
 * A server class to accept connections
 * and start up communication threads
 * as required.
 *
 * @author
 * @version 1.00 2014/2/22
 */

import java.util.concurrent.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server extends Thread {

	//Size of buffer
	public static final int MAX_BUF = 5000;
	//Maximum payload size, rounding down
	public static final int MAX_PAYLOAD_LEN = 64000;
	/*
	 * The number of bytes in one transmission unit
	 * dictated by our communication protocol
	 *
	 * Each transmission unit represents the change of
	 * two blocks. this can be a movement, deploying a bomb,
	 * etc..
	 *
	 * byte representation:
	 * 1: first x value
	 * 2: first y value
	 * 3: the character symbol for the new square at this coord
	 * 4: second x value
	 * 5: second y value
	 * 6: the character symbol for the new square at this coord
	 */
	 public static final int PROTO_PACKET_LEN = 6;
	 //the starting health for a player, may come from somewhere else later
	 private int START_HEALTH = 10;
	 //number of players in a game
	 private int MAX_PLAYERS = 4;

	//The 2D game representation
	World board;
	/*
	 * The list of players or spectators subscribed to
	 * receive updates regarding our game state.
	 */
	ArrayList<Subscriber> spectators;
	//Socket on which to accept
	DatagramSocket sockfd;
	//Buffers for input controls
	byte buffer1[];
	byte buffer2[];
	//buffer pointer to one of the above
	byte working_buffer[];
	//working buffer identifier
	int current_buffer;
	//An executor for running working threads
	ExecutorService executor;
	//The server's listening port
	int listen_port;
	char highestPlayerChar;


    public Server(World b, int port) {
		board = b;
		listen_port = port;
		//Should be a 2D array representing the map
		buffer1 = new byte[MAX_BUF];
		buffer2 = new byte[MAX_BUF];
		working_buffer = buffer1;
		current_buffer = 1;
		executor = Executors.newCachedThreadPool();
		highestPlayerChar = 'a';
    }

	/*
	 * accept packets,
	 * if new player request, handle adding them to the list
	 * if a movement or other command, handle board update
  	 */
	public void run(){

		DatagramSocket socket;
		try {
			socket = new DatagramSocket(listen_port);
		} catch (SocketException e){
			System.out.println("A socket error occured: " + e.getMessage());
			return;
		} catch (Exception e){
			System.out.println("An unexpected error occured: " + e.getMessage());
			return;
		}

		byte[] receiveData = new byte[MAX_PAYLOAD_LEN];

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
			 * TODO: undo the received packet and determine command
			 * and call appropriate handling method.
			 *
			 * Need to create method for assigning start coordinates for new player.
			 *
			 * Movement commands require an initial position. We will need
			 * to somehow relate movement messages to a player in order to
			 * update the Subscriber's player instance and create the two
			 * box changes in the buffer. The player can send us his current location
			 * or characterID. The characterID would require us to notify the client
			 * of their ID after they request to join... Just some first thoughts.
			 *
			 */
		}
	}

	/*
	 * Add the player to the game
	 * return true if succesful,
	 * false otherwise
	 */
	private boolean handleNewPlayer(DatagramPacket packet, int startX, int startY){

		if ( canAddPlayer() ){
			//Create a player, and a subscriber
			InetAddress IPAddress = packet.getAddress();
			int port = packet.getPort();

			Player p = new Player(startX, startY, START_HEALTH, (char)(highestPlayerChar+1), true);

			spectators.add( (new Subscriber(IPAddress, port)) );

			return true;
		}

		return false;

	}

	//the x, y coordinate of origin, and command to action.
	private void processIncomingCommand(int x, int y, String command){
		/*
		 * TODO: This is where we will need to
		 * convert the x, y, COMMAND into the working byte array
		 */

		//syncrhonize on working buffer while appending to it
		synchronized(working_buffer){
			//working_buffer[ working_buffer.length ] = command;
		}//End synchronized
	}

	/*
	 * Create a new thread to work in, using
	 * the executor. swap the working buffer with
	 * the empty one. Iterate through the Viewers/Players,
	 * sending the ouput and removing it once it is done.
	 */
	private void distributeOutput(World w){

		Future<String> future;

		//launch a new thread to work in
		future = executor.submit(new Callable<String>(){
			//thread's run method
			public String call(){

				byte temp_buf[];

				//Syncrhonize on the working buffer during swap
				synchronized(working_buffer){
					//Hold on to the filled buffer
					temp_buf = working_buffer;

					//Swap working buffer for empty
					if ( current_buffer == 1 ){
						working_buffer = buffer2;
						current_buffer = 2;
					} else if ( current_buffer == 2 ){
						working_buffer = buffer1;
						current_buffer = 1;
					}

				}//End syncrhonized

				//Iterate over all queued commands
				for (int i=0; i<temp_buf.length; i+=PROTO_PACKET_LEN){
					//Iterate over all subscribers
					for (Subscriber client: spectators){
						//The packet to send
						byte sendData[] = new byte[PROTO_PACKET_LEN];

						//fill out the packet with bytes fom i to (i + PROTO_LEN) - 1
						for (int j=0; j<PROTO_PACKET_LEN; j++){
							sendData[j] = temp_buf[i+j];
						}

						//Send this spectator the data
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
																client.getAddr(), client.getPort());
					}
				}

        		return null;
			}//End call()

		});//End submit()ing anonymous class

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
