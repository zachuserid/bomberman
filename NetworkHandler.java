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

	protected
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
	protected worldSem;
	protected S worldBuffer;
	
	//Update buffer, parsed by the server, sent to world, filled by network
	//Locks for read/write
	protected Semaphore updateWritable, updateReadable;
	//indeces
	protected int uIn, uOut;
	//buffer updated by network
	protected byte[] playerUpdateBufferIn;
	//buffer send to world
	protected byte[] playerUpdateBufferOut;
	
	//CTOR
    public NetworkHandler() {
    	uIn = 0;
    	uOut = 0;
    	
    	//worldWritable = new Semaphore(1);
    	//worldReadable = new Semaphore(1);
    	worldSem = new Semaphore(1);
    	updateWritable = new Semaphore(1);
    	updateReadable = new Semaphore(1);
    }
    
    //METHODS
    abstract R[] getData();

    abstract void sendData(S data);
    
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
    
    //returns success of swap
    protected boolean swapUpdateBuffer(){
    	//Swap working buffer for empty
    	updateWritable.acquireUninterruptibly();
    	updateReadable.acquireUninterruptibly();
    	
    	byte temp[] = playerUpdateBufferIn;
    	playerUpdateBufferIn = playerUpdateBufferOut;
    	playerUpdateBufferOut = temp;
		
    	return true;
    }


}