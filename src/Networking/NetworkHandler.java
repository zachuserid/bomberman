package Networking;

/**
 * @(#)NetworkHandler.java
 *
 *
 * @author
 * @version 1.00 2014/2/24
 */

import java.net.*;

//Param <S>: type of send data
//Param <R>: type of receive data
//Sendable = copyable
public abstract class NetworkHandler<S, R> {

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
    			SenderThreadMethod();
    		}
    	});
    	this.recieveThread = new Thread(new Runnable(){
    		public void run(){
    			ReceiverThreadMethod();
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
    			this.sA = !this.sA;
    			this.getSendWrite().notify();
    		}
    	}
    }

    /*
     * takes control of the buffer that was being filled with object data
     * copies that buffer to the given buffer
     */
    public int getData(R[] buffer)
    {
    	//this.swapReceiveBuffer();

    	R[] b = this.getReceiveRead();

    	int length;

    	synchronized(b)
    	{
    		length = this.getReceiveReadIndex();

    		System.out.println("~~~length of receive buffer: "+ length + " write index: " + this.getReceiveWriteIndex());

    		for(int i = 0; i < length; i++)
    		{
    			buffer[i] = getReceiveCopy(b[i]);
    		}
    		//Empty the read buffer
    		System.out.println("~~~~setting receiveRead to 0");
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
    		int length = Math.min(buffer.length, b.length);

    		for(int i = 0; i < length; i++)
    		{
    			//@DEBUG
    			if ( buffer[i] == null )
    			{
    				System.out.println("Trying to getSendCopy(null)");
    			}
    			b[i] = getSendCopy(buffer[i]);
    			
    			//@DEBUG
    			if ( b[i] == null )
    			{
    				System.out.println("WARNING: null got into send buffer at index " + i);
    			}

    		}
    		
    		this.setSendWriteIndex(length);

    	}
    	
    	this.swapSendBuffer();
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
	    			System.out.println("Server about to send");
	    			//Obtain the consumer buffer for send objects
		    		int length = this.getSendReadIndex();

		    		//Byte array raw packet data
		    		byte[] toSend = new byte[64000];
		    		int sendIndex = 0;

		    		//For the number of objects to send
		    		for(int i = 0; i < length; i++)
		    		{
		    			//Obtain the byte representation of this one object
		    			byte[] data = this.parseSend(b[i]);
		    			//Add its bytes to the outgoing raw packet
		    			for(int n = 0; n < data.length; n++)
		    			{
		    				if ( n + sendIndex < toSend.length )
		    					toSend[n + sendIndex] = data[n];
		    			}
	    				sendIndex += data.length;
	    			}
		    		
		    		System.out.println("IMPORTANT: SenderThread sending " + new String( toSend ) );
		    		
		    		//Send data over network
		    		this.Send(toSend);

		    		//Reset the consumer buffer to be produced to (over top of the data)
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
    		System.out.println("current receive write: " + getReceiveWriteIndex()
    				           + ". current send read: " + getSendReadIndex());
    		DatagramPacket packet = new DatagramPacket(new byte[64000], 64000);

			try
			{
				this.socket.receive(packet);
				
				System.out.println("IMPORTANT: RECEIVE THREAD: " + new String ( packet.getData() ));
			} catch (Exception e) {
				System.out.println("Receive error: " + e.getMessage());
			}
			//System.out.println("Receive thread outside of receive");
			//Obtain the consumer buffer for received data
    		R[] b = this.getReceiveWrite();

    		synchronized(b)
    		{
    			int index = this.getReceiveWriteIndex();
    			System.out.println("About to log some receives starting at " + index);

    			//Allow handling of datagram packet before we strip the data
				this.preProcessPacket(packet);

				//System.out.println("Writing to receive array at index " + index);
				if ( index < b.length )
				{
					//Convert bytes to receive type object and add it to the buffer
					int newIndex = this.parseReceive( b, index, packet.getData() );
					System.out.println("got new write index of " + newIndex + " after parsing");
					//Increment the producer buffer index
	    			this.setReceiveWriteIndex(newIndex);

				} else { System.out.println("Receive buffer overflow"); }
			
    		}
    	}
    }

    //stops the handler
    public void Stop()
    {
    	this.active = false;
    	
    	if ( this.socket != null )
    	{
    		this.socket.close();
    	}
    }

    //Abstracts and overrides

    //return a R(eceive) type object from parsing raw packet data
    protected abstract int parseReceive(R[] array, int currIndex, byte[] data);

    //return raw packet data from an S(end) type object
    protected abstract byte[] parseSend(S data);

    //binds socket for either client or server
    protected abstract DatagramSocket BindSocket() throws SocketException;

    //sends data for either a client or server
    protected abstract void Send(byte[] data);

    //a copy method to be implemented at lowest level
    public abstract S getSendCopy(S original);

	//a copy of the receive type object given
    public abstract R getReceiveCopy(R original);

    //method to perform any logic which requires the datagram packet
    protected void preProcessPacket(DatagramPacket packet){ /*default, no implementation */ }
}