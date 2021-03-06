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
public abstract class NetworkHandler<S, R> {

	//Data members
	
	protected boolean sA, rA;

	protected int sAIndex, sBIndex, rAIndex, rBIndex;

	protected S[] sendBufferA, sendBufferB;

	protected R[] receiveBufferA, receiveBufferB;

	protected Thread sendThread, recieveThread;

	protected DatagramSocket socket;

	protected boolean active;

	//Getters & Setters
	
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

	protected R[] getReceiveWrite()
	{
		if(this.rA) return this.receiveBufferA;
		return this.receiveBufferB;
	}

	protected R[] getReceiveRead()
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
	protected void setSendWriteIndex(int index)
	{
		if(this.sA) this.sAIndex = index;
		else this.sBIndex = index;
	}

	protected void setSendReadIndex(int index)
	{
		if(this.sA) this.sBIndex = index;
		else this.sAIndex = index;
	}

	protected void setReceiveWriteIndex(int index)
	{
		if(this.rA) this.rAIndex = index;
		else this.rBIndex = index;
	}

	protected void setReceiveReadIndex(int index)
	{
		if(this.rA) this.rBIndex = index;
		else this.rAIndex = index;
	}


	//Constructors
	
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
    			SenderThreadMethod();
    		}
    	});
    	this.recieveThread = new Thread(new Runnable(){
    		public void run(){
    			ReceiverThreadMethod();
    		}
    	});
    }


    //Methods
    
    //initializes the threads and starts the server
    public boolean Initialize()
    {
    	try
    	{
    		this.BindSocket(this.socket);
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
    			buffer[i] = getReceiveCopy(b[i]);

    		this.setReceiveReadIndex(0);
        	System.out.println("getData: " + this.setReceiveReadIndex(0) + " returning length: " + length);
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
    		int length = Math.min(buffer.length, b.length);

    		for(int i = 0; i < length; i++)
    			b[i] = getSendCopy(buffer[i]);
    		System.out.println("sendData()");
    	}

    	this.swapSendBuffer();
    }

    
    //swaps the buffers used for recieving and processing data
    protected void swapReceiveBuffer()
    {
    	synchronized(this.getReceiveWrite())
    	{
    		synchronized(this.getReceiveRead())
    		{
    			System.out.println("Swapping receive");
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
    			System.out.println("Swapping send");
    			this.sA = !this.sA;
    			this.getSendWrite().notify();
    		}
    	}
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

	    		try { b.wait(); } catch(Exception e){}
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

			try
			{
				this.socket.receive(packet);
			} catch (Exception e) {}

    		R[] b = this.getReceiveWrite();

    		synchronized(b)
    		{
    			int index = this.getReceiveWriteIndex();

				this.preProcessPacket(packet);

				if ( index < b.length )
				{
					System.out.println("Received a packet: " + b[index]);
					
					b[index] = this.parseReceive( packet.getData() );

					this.setReceiveWriteIndex(++index);
				} else { System.out.println("Receive buffer overflow"); }
    			
    		}
    	}
    }

    
    //stops the handler
    public void Stop()
    {
    	System.out.println("Closing socket");
    	
    	this.active = false;
    	
    	socket.close();
    }
    

    //Abstracts and overrides

    //return a R(eceive) type object from parsing raw packet data
    protected abstract R parseReceive(byte[] data);

    //return raw packet data from an S(end) type object
    protected abstract byte[] parseSend(S data);

    //binds socket for either client or server
    protected abstract void BindSocket(DatagramSocket socket) throws SocketException;

    //sends data for either a client or server
    protected abstract void Send(byte[] data);

    //a copy method to be implemented at lowest level
    public abstract S getSendCopy(S original);

	//a copy of the receive type object given
    public abstract R getReceiveCopy(R original);

    //method to perform any logic which requires the datagram packet
    protected void preProcessPacket(DatagramPacket packet){ /*default, no implementation */ }
}