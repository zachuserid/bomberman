package Networking;

/**
 * @(#)NetworkHandler.java
 *
 *
 * @author
 * @version 1.00 2014/2/24
 */

import java.net.*;
import java.util.ArrayDeque;
import java.util.ArrayList;

//S: type of send data
//R: type of receive data
public abstract class NetworkHandler<S, R> {

	protected boolean sA, rA;

	protected int sAIndex, sBIndex, rAIndex, rBIndex;

	protected ArrayDeque<S> sendBufferA, sendBufferB;

	protected ArrayDeque<R> receiveBufferA, receiveBufferB;

	protected Thread sendThread, recieveThread;

	protected DatagramSocket socket;

	protected boolean active;


	//returns the active buffer for a given task
	protected ArrayDeque<S> getSendWrite()
	{
		if(this.sA) return this.sendBufferA;
		return this.sendBufferB;
	}

	protected ArrayDeque<S> getSendRead()
	{
		if(this.sA) return this.sendBufferB;
		return this.sendBufferA;
	}

	protected ArrayDeque<R> getReceiveWrite()
	{
		if(this.rA) return this.receiveBufferA;
		return this.receiveBufferB;
	}

	protected ArrayDeque<R> getReceiveRead()
	{
		if(this.rA) return this.receiveBufferB;
		return this.receiveBufferA;
	}


    public NetworkHandler() 
    {
    	this.sendBufferA = new ArrayDeque<S>();
    	this.sendBufferB = new ArrayDeque<S>();

    	this.receiveBufferA = new ArrayDeque<R>();
    	this.receiveBufferB = new ArrayDeque<R>();

    	this.sendThread = new Thread(new Runnable(){
    		public void run(){
    			SenderThreadMethod();
    		}
    	});
    	this.recieveThread = new Thread(new Runnable(){
    		public void run(){
    			ReceiverThreadMethod();
    		}
    	});
    }

    
    //initializes the threads and starts the server
    public boolean Initialize()
    {
    	try
    	{
    		this.socket = this.BindSocket();
    	} catch (SocketException e)
    	{
    		System.out.println("Could not bind socket. " + e.getMessage());
    		return false;
    	}

    	this.active = true;

    	this.recieveThread.start();
    	this.sendThread.start();
    	
    	return true;
    }

    
    //stops the handler
    public void Stop()
    {
    	this.active = false;
    	
    	if ( this.socket != null ) this.socket.close();
    	
    	ArrayDeque<S> d = this.getSendRead();
    	
    	synchronized(d)
    	{
    		d.notify();
    	}
    }
    
    /*
     * takes control of the buffer that was being filled with object data
     * copies the data and returns it
     */
    public ArrayList<R> getData()
    {
    	this.swapReceiveBuffer();
    	
    	ArrayDeque<R> b = this.getReceiveRead();

    	ArrayList<R> list = new ArrayList<R>(b.size());
    	    	
    	synchronized(b)
    	{
    		while(!b.isEmpty()) list.add(this.getReceiveCopy(b.pop()));
    	}
    	
    	return list;
    }

    /*
     * copies the data to the writing buffer to send
     * swaps the buffer to now be the reading buffer
     */
    public void sendData(ArrayList<S> data)
    {
    	ArrayDeque<S> b = this.getSendWrite();

    	synchronized(b)
    	{
    		for(S d : data) b.push(this.getSendCopy(d));
    		
    		System.out.println("send write buffer has " + b.size());
    	}
    	
    	this.swapSendBuffer();
    }


    /*
     * gets the reading buffer
     * sees if there is something in it
     * if there is, process it then send it
     * then wait for the signal that the reading buffer is being swapped
     * repeat
     */
    protected void SenderThreadMethod()
    {
    	while(this.active)
    	{
    		ArrayDeque<S> b = this.getSendRead();

	    	synchronized(b)
	    	{
	    		if(!b.isEmpty())
	    		{
	    			System.out.println("Sending " + b.size() + " items");
	    			
	    			//change this for slightly more efficient way eventually
	    			byte[] sendData = new byte[16000];
	    			int index = 0;
	    			
	    			while(!b.isEmpty()) 
	    			{
	    				byte[] d = this.parseSend(b.pop());
	    				
	    				for(int i = 0; i < d.length; i++) sendData[index++] = d[i];
	    			}
	    			
	    			System.out.println("Sending string: " + new String( sendData ) );
		    		
		    		//Send data over network
		    		this.Send(sendData);
	    		}
	    		
	    		//if still active, wait on this buffer to be signaled
	    		if(this.active) try { b.wait(); } catch(Exception e){ System.out.println("Exception in sender wait"); }
    		}
    	}
    	
    	System.out.println("Sender thread complete");
    }

    /*
     * waits for a new packet
     * grabs the writing buffer
     * converts the packet to an object
     * writes the object to the buffer
     * increases the count of objects in buffer
     */
    protected void ReceiverThreadMethod()
    {
    	while(this.active)
    	{
    		DatagramPacket packet = new DatagramPacket(new byte[64000], 64000);

			try
			{

				this.socket.receive(packet);
				
				System.out.println("NOTE: Received " + new String ( packet.getData() ));
				
			} catch (Exception e) 
			{ 
				System.out.println("Socket exception: " + e.getMessage()); 
				packet = null;
			}
			
    		//Allow handling of datagram packet before we strip the data
			//if it returns true, we should do continue to process it normally
			if(packet != null && this.preProcessPacket(packet))
			{
				//Obtain the consumer buffer for received data
        		ArrayDeque<R> b = this.getReceiveWrite();
        		
	    		synchronized(b)
	    		{
	    			R[] data = this.parseReceive(packet.getData());
	    			for(int i = 0; i < data.length; i++)
	    			{
	    				if ( data[i] != null )
	    					b.push(data[i]);
	    			}
	    			
	    			System.out.println(data.length + " element(s) added to receive buffer");
				}
    		}
    	}
    	
    	System.out.println("Receiver thread complete");
    }

    //swaps the buffers used for recieving and processing data
    protected void swapReceiveBuffer()
    {
    	synchronized(this.getReceiveWrite())
    	{
    		synchronized(this.getReceiveRead())
    		{
    			this.rA = !this.rA;
    		}
    	}
    }

    //swaps the buffers used for processing and sending data
    protected void swapSendBuffer()
    {
    	synchronized(this.getSendRead())
    	{
    		synchronized(this.getSendWrite())
    		{
    			this.sA = !this.sA;
    			this.getSendWrite().notify();
    		}
    	}
    }
    
    //method to perform any logic which requires the datagram packet, if returns true, packet processed normally
    protected boolean preProcessPacket(DatagramPacket packet){ return true; }

    //return a R(eceive) type object from parsing raw packet data
    protected abstract R[] parseReceive(byte[] data);

    //return raw packet data from an S(end) type object
    protected abstract byte[] parseSend(S data);

    //binds socket for either client or server
    protected abstract DatagramSocket BindSocket() throws SocketException;

    //sends data for either a client or server
    protected abstract void Send(byte[] data);
    
    //a copy method to be implemented at lowest level
    protected abstract S getSendCopy(S original);

	//a copy of the receive type object given
    protected abstract R getReceiveCopy(R original);
}