/**
 * @(#)ServerNetworkHandler.java
 *
 *
 * @author
 * @version 1.00 2014/2/24
 */

import java.util.concurrent.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ServerNetworkHandler extends AbstractServerNetworkHandler<World, PlayerUpdate> {

    //All stuff that server currently has relating to subscribers/network

    public ServerNetworkHandler(int port) {
    	super(port);
    }

	/*
	 * Grab the data from the buffer and rotate for
	 * consumer, producer style buffering
	 */
	public PlayerUpdate[] getData(){

		//Acquire the write lock...
		updateWritable.acquireUninterruptibly();

		//Keep a pointer reference to the input buffer before rotating
		byte in_buf_ptr[] = playerUpdateBufferIn;

		//Copy of the buffer from the now consumable buffer
		byte in_buf[] = new byte[in_buf_ptr.length];

		//Get a copy of the current input buffer
		for (int i=0; i<in_buf.length; i++){
			in_buf[i] = in_buf_ptr[i];
		}

		//Release the lock, allowing access for swap()
		updateWritable.release();

		//Rotate
		swapUpdateBuffer();

		//Get the buffer and clear the working one.
		PlayerUpdate playerUpdates[] = parseData(in_buf);

		return playerUpdates;

	}

	/*
	 * parseData() will handle individual player messages.
	 * It will gather the various command information
	 * and use it to create a new instance of a R
	 * from a factory method.
	 */
	@Override
    PlayerUpdate parseReceive(byte[] data){
		//TODO: Figure out a workaround for implementing
		// this method with generic types.. can't use factory methods
		// on a static type.

    	return (PlayerUpdate[])null;
    }
	
	@Override
    public void sendData(final World data){
    	//Access the outgoing buffer, lock it and send data
    	//to all recepients
    	Future<String> future;

		//launch a new thread to work in
		future = executor.submit(new Callable<String>(){
			//thread's run method
			public String call(){

				//data to be send over network
				byte packet_data[] = worldToBytes(data);

				//Iterate over all subscribers
				for (Subscriber client: spectators){

					//Send this spectator the data
					DatagramPacket sendPacket = new DatagramPacket(packet_data, packet_data.length,
																client.getAddr(), client.getPort());
				}
			
        		return null; //In case this has some use later
			} //End call()

		}); //End submit()ing anonymous object


    }
    
    private byte[] worldToBytes(World w){
		
		int wid = w.getWidth();
		int hei = w.getHeight();
		byte theBytes[] = new byte[(wid * hei) + 1];
		
		for (int i=0; i<wid; i++){
			for (int j=0; j<hei; j++){
				
				theBytes[ (i*wid) + j ] = (byte)w.getCharAt(i, j);
				
			}
		}
		
		return theBytes;
		
	}

}