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
	//array of returnable objects
	protected R[] returnArr;

	//CTOR
    public NetworkHandler(R[] ret) {

    	//Initialize the buffers
    	playerUpdateBufferIn = new byte[MAX_BUF];
    	playerUpdateBufferOut = new byte[MAX_BUF];

		returnArr = ret;
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

    	//Clear the now producer buffer
		playerUpdateBufferOut = new byte[MAX_BUF];
		uOut = 0;

    	return true;
    }


}