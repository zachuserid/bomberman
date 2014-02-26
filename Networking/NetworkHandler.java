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
//Sendable = copyable
public abstract class NetworkHandler<S implements Sendable, R implements Sendable> {
	
	protected boolean sA, rA;
	
	protected int sAIndex, sBIndex, rAIndex, rBIndex;

	protected S[] sendBufferA, sendBufferB;
	
	protected R[] receiveBufferA, receiveBufferB;
	
	protected Thread sendThread, recieveThread;
	
	protected DatagramSocket socket;
	
	protected boolean active;
	
	
	//returns the active buffer
	protected S[] getSendWrite()
	{
		if(this.sA) return this.sendBufferA;
		return this.sendBufferB;
	}
	
	protected S[] getSendRead()
	{
		if(this.sA) return this.sendBufferB;
		return this.sendBufferA;
	}
	
	protected S[] getReceiveWrite()
	{
		if(this.rA) return this.receiveBufferA;
		return this.receiveBufferB;
	}
	
	protected S[] getReceiveRead()
	{
		if(this.rA) return this.receiveBufferB;
		return this.receiveBufferA;
	}
	
	//returns the index for the active buffer
	protected int getSendWriteIndex()
	{
		if(this.sA) return this.sAIndex;
		return this.sBIndex;
	}
	
	protected int getSendReadIndex()
	{
		if(this.sA) return this.sBIndex;
		return this.sAIndex;
	}
	
	protected int getReceiveWriteIndex()
	{
		if(this.rA) return this.rAIndex;
		return this.rBIndex;
	}
	
	protected int getReceiveReadIndex()
	{
		if(this.rA) return this.rBIndex;
		return this.rAIndex;
	}

	//sets the index for the active buffer
	protected int setSendWriteIndex(int index)
	{
		if(this.sA) this.sAIndex = index;
		else this.sBIndex = index;
	}
	
	protected int setSendReadIndex(int index)
	{
		if(this.sA) this.sBIndex = index;
		else this.sAIndex = index;
	}
	
	protected int setReceiveWriteIndex(int index)
	{
		if(this.rA) this.rAIndex = index;
		else this.rBIndex = index;
	}
	
	protected int setReceiveReadIndex(int index)
	{
		if(this.rA) this.rBIndex = index;
		else this.rAIndex = index;	
	}
	

	//CTOR
    public NetworkHandler(S[] as, S[] bs, R[] ar, R[] br) {
    	
    	this.sAIndex = 0;
    	this.sBIndex = 0;
    	this.rAIndex = 0;
    	this.rBIndex = 0;

    	this.sendBufferA = as;
    	this.sendBufferB = bs;
    	
    	this.receiveBufferA = ar;
    	this.receiveBufferB = br;
    	
    	this.sendThread = new Thread(new Runnable(){
    		public void run(){
    			this.SenderThreadMethod();
    		}
    	});
    	this.recieveThread = new Thread(new Runnable(){
    		public void run(){
    			this.ReceiverThreadMethod();
    		}
    	});
    }

    
    //METHODS
    
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
    			this.rB = !this.rB;
    			this.getSendWrite.notify();
    		}
    	}
    }
    
    /*
     * takes control of the buffer that was being filled with object data
     * copies that buffer to the given buffer
     */
    public int getData(R[] buffer)
    {
    	this.swapReceiveBuffer();
    	
    	R[] b = this.getReceiveRead();
    	
    	int length;
    	
    	synchronized(b)
    	{
    		length = this.getReceiveReadIndex();
    		
    		for(int i = 0; i < length; i++)
    			buffer[i] = b[i].getCopy();
    		
    		this.setReceiveReadIndex(0);
    	}
    	
    	return length;
    }

    /*
     * copies the data to the writing buffer to send
     * swaps the buffer to now be the reading buffer
     */
    public void sendData(S[] buffer){
    	
    	S[] b = this.getSendWrite();
    	
    	synchronized(b)
    	{
    		int length = Math.min(data.length, b.length);
    		
    		for(int i = 0; i < length; i++)
    			b[i] = buffer[i].getCopy();
    		
    		
    	}
    	
    	this.swapSendBuffer();
    }
    
    protected abstract R parseReceive(byte[] data);
    
    protected abstract byte[] parseSend(S data);
    
    
    //initializes the threads and starts the server
    public void Initialize()
    {
    	this.BindSocket(this.socket);
    	
    	this.active = true;
    	
    	this.recieveThread.start();
    	this.sendThread.start();
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
    		
    		S[] b = this.getSendRead();
    		
	    	synchronized(b)
	    	{
	    		if(this.getSendReadIndex() != 0)
	    		{
		    		int length = this.getSendReadIndex();
		    		
		    		byte[] toSend = new byte[64000];
		    		int sendIndex = 0;
		    		
		    		for(int i = 0; i < length; i++)
		    		{
		    			byte[] data = this.parseSend(b[i]);
		    			for(int n = 0; n < data.length; n++)
		    				toSend[n + sendIndex] = data[n]; 
	    				sendIndex += data.length;
	    			}
		    		
		    		this.Send(toSend);
	    		
	    			this.setSendReadIndex(0);
	    		}
	    		
	    		b.wait();
    		}
    	}
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
    		
    		this.socket.receive(packet);
    		
    		R[] b = this.getReceiveWrite();
    		
    		synchronized(b)
    		{
    			int index = this.getReceiveWriteIndex();
    			
    			b[index] = this.parseReceive(packet.getData());
    			
    			this.setReceiveWriteIndex(++index);
    		}
    	}
    }
    
    //binds socket for either client or server
    protected abstract void BindSocket(DatagramSocket socket);
    
    //sends data for either a client or server
    protected abstract void Send(byte[] data);
    
    //stops the handler
    public void Stop()
    {
    	this.active = false;
    }
}