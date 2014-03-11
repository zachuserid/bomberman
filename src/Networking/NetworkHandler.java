package Networking;

/**
 * @(#)NetworkHandler.java
 *
 *
 * @author
 * @version 1.00 2014/2/24
 */

import java.net.*;
import java.util.ArrayList;

//S: type of send data
//R: type of receive data
public abstract class NetworkHandler<S, R> {

	protected int sAIndex, sBIndex, rAIndex, rBIndex;

	protected Thread sendThread, recieveThread;

	protected DatagramSocket socket;

	protected boolean active;
	
	protected DoubleBuffer<S> doubleSendBuffer;
	
	protected DoubleBuffer<R> doubleReceiveBuffer;


    public NetworkHandler() 
    {

    	this.doubleSendBuffer = new DoubleBuffer<S>();
    	this.doubleReceiveBuffer = new DoubleBuffer<R>();
    	
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
    	
    	synchronized(this.doubleSendBuffer)
    	{
    		this.doubleSendBuffer.notify();
    	}

    }
    
    /*
     * empties the receive buffer and returns
     * all of its contents
     */
    public ArrayList<R> getData()
    {	
    	return this.doubleReceiveBuffer.readAll(true);
    }

    /*
     * copies the data to the writing buffer to send
     * swaps the buffer to now be the reading buffer
     */
    public void Send(S data)
    {
    	//System.out.println("Send(S data)");
    	
    	this.doubleSendBuffer.write(this.getSendCopy(data), true);
    	
    	synchronized(this.doubleSendBuffer)
    	{
    		this.doubleSendBuffer.notify();
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
    		//System.out.println("reading all from sendBuffer");
			ArrayList<S> b = doubleSendBuffer.readAll(false);
			
			if (!b.isEmpty())
			{
				//System.out.println("SenderThreadMethod b is not empty");
				// System.out.println("Sending " + b.size() + " items");

				// change this for slightly more efficient way eventually
				byte[] sendData = new byte[16000];
				int index = 0;

				for (int i = 0; i < b.size(); i++) 
				{
					byte[] d = this.parseSend(b.get(i));

					for (int j = 0; j < d.length; j++)
						sendData[index++] = d[j];
				}

				// Send data over network
				//System.out.println("SenderThreadMethod sendData()");
				this.sendData(sendData);
			}

	    	//if still active, wait on send buffer to be signaled
			synchronized(this.doubleSendBuffer)
			{
				if(this.active)
				{
					try { this.doubleSendBuffer.wait(); } 
					catch(Exception e){ System.out.println("Exception in sender wait"); }
				}
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
				
				//System.out.println("NOTE: Received " + new String ( packet.getData() ));
				
			} catch (Exception e) 
			{ 
				System.out.println("Socket exception: " + e.getMessage()); 
				packet = null;
			}
			
    		//Allow handling of datagram packet before we strip the data
			//if it returns true, we should do continue to process it normally
			if (packet != null && this.preProcessPacket(packet)) {
				// Write to the receive buffer
				R[] data = this.parseReceive(packet.getData());
				for (int i = 0; i < data.length; i++) {
					if (data[i] != null)
						this.doubleReceiveBuffer.write(data[i], false);
				}

				 //System.out.println(data.length +" element(s) added to receive buffer");
			}
    	}
    	
    	System.out.println("Receiver thread complete");
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
    protected abstract void sendData(byte[] data);
    
    //a copy method to be implemented at lowest level
    protected abstract S getSendCopy(S original);

	//a copy of the receive type object given
    protected abstract R getReceiveCopy(R original);
}