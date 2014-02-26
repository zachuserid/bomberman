/**
 * @(#)NetworkHandler.java
 *
 *
 * @author
 * @version 1.00 2014/2/24
 */

import java.util.concurrent.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

//Param <S>: type of send data
//Param <R>: type of receive data
public abstract class NetworkHandler<S, R> {

	//Maximum UDP payload size, rounding down
	public static final int MAX_BUF = 64000;

	//An executor for running working threads
	ExecutorService executor;

	/*
	//World buffer, passed in by world, sending to the clients
	//Locks for read, write
	private Semaphore worldWritable, worldReadable;
	//Indeces
	private int worldIn, worldOut;
	//buffer filled by world
	protected S worldBufferIn;
	//buffer send to clients
	protected S worldBufferOut;
	*/

	//Update buffer, parsed by the server, sent to world, filled by network
	//Locks for read/write
	protected Semaphore updateWritable, updateReadable;
	//indeces
	protected int uIn, uOut, retIndex;
	//buffer updated by network
	protected byte[] playerUpdateBufferIn;
	//buffer send to world
	protected byte[] playerUpdateBufferOut;

	//CTOR
    public NetworkHandler() {

    	//Initialize the buffers
    	playerUpdateBufferIn = new byte[MAX_BUF];
    	playerUpdateBufferOut = new byte[MAX_BUF];

    	//buffer indeces, all are empty.
    	uIn = 0;
    	uOut = 0;
    	retIndex = 0;

    	//worldWritable = new Semaphore(1);
    	//worldReadable = new Semaphore(1);
    	updateWritable = new Semaphore(1);
    	updateReadable = new Semaphore(1);

    	executor = Executors.newCachedThreadPool();
    }

    //METHODS
    public abstract R[] getData();

    public void sendData(S data){
    	
    	byte[] toSend = parseSend(data);
    	
    	
    	
    }
    
    protected abstract R parseReceive(byte[] data);
    
    protected abstract byte[] parseSend(S data);
    
    protected bufferData(DatagramPacket data){
    	String buf = new String(packet.getData());

		byte bytes[] = buf.getBytes();

		updateWritable.acquireUninterruptibly();
		updateReadable.acquireUninterruptibly();

		for (int j=0; j<bytes.length; j++){
			playerUpdateBufferIn[uIn++] = bytes[j];
		}

		updateReadable.release();
		updateWritable.release();

    }

    /*
    protected boolean swapWorldBuffer(){
    	//Swap working buffer for empty
    	worldWritable.aquireUnInteruptably();
    	worldReadable.aquireUnInteruptable();

    	byte temp[] = worldBufferIn;
    	worldBufferIn = worldBufferOut;
    	worldBufferOut = temp;

    }
    */
    
    //THIS METHOD WILL BE RUN IN A SEPERATE THREAD
    public void initCommunication(){
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

    //returns success of swap
    protected boolean swapUpdateBuffer(){
    	//Swap working buffer for empty
    	updateWritable.acquireUninterruptibly();
    	updateReadable.acquireUninterruptibly();

    	byte temp[] = playerUpdateBufferIn;
    	playerUpdateBufferIn = playerUpdateBufferOut;
    	playerUpdateBufferOut = temp;

    	//Clear the now producer buffer
		playerUpdateBufferOut = new byte[MAX_BUF];
		uOut = 0;

    	return true;
    }


}