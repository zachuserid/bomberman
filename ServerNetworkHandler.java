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

public class ServerNetworkHandler<S extends Sendable<S>, R> extends AbstractServerNetworkHandler<S, R> {

    //All stuff that server currently has relating to subscribers/network

    public ServerNetworkHandler(R[] ret, int port) {
    	super(ret, port);
    }

	/*
	 * Grab the data from the buffer and rotate for
	 * consumer, producer style buffering
	 */
	public R[] getData(){

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
		R playerUpdates[] = (R[])parseData(in_buf);

		return playerUpdates;

	}

    public void sendData(final S data){
    	//Access the outgoing buffer, lock it and send data
    	//to all recepients
    	Future<String> future;

		//launch a new thread to work in
		future = executor.submit(new Callable<String>(){
			//thread's run method
			public String call(){

				//Buffer data to covert to byte[] before sending it.
				S copy = data.getCopy();

				//data to be send over network
				byte packet_data[] = new byte[MAX_BUF];

				//Convert the copied S into byte[]
				packet_data = data.getBytes();

				//Iterate over all queued commands
				for (int i=0; i<packet_data.length; i+=packet_data.length){
					//Iterate over all subscribers
					for (Subscriber client: spectators){

						//Send this spectator the data
						DatagramPacket sendPacket = new DatagramPacket(packet_data, packet_data.length,
																client.getAddr(), client.getPort());
					}
				}

        		return null; //In case this has some use later
			} //End call()

		}); //End submit()ing anonymous object


    }

}