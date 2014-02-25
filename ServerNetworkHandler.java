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

public class ServerNetworkHandler<S extends Newable, R> extends AbstractServerNetworkHandler<S, R> {

    //All stuff that server currently has relating to subscribers/network

    public ServerNetworkHandler(int port) {
    	super(port);
    }

	public R[] getData(){
		return (R[])null;
	}

    public void sendData(S data){
    	//Access the outgoing buffer, lock it and send data 
    	//to all recepients
    	Future<String> future;

		//launch a new thread to work in
		future = executor.submit(new Callable<String>(){
			//thread's run method
			public String call(){
				
				//Buffer data before sending it
				worldSem.acquireUninterruptably();
				Newable copy = data.getCopy();
				worldSem.release();
				

				//data to be send over network
				byte packet_data[] = new byte[MAX_BUF];
				
				//Convert the copied S into byte[]

				//Iterate over all queued commands
				for (int i=0; i<packet_data.length; i+=packet_data.length){
					//Iterate over all subscribers
					for (Subscriber client: spectators){

						//Send this spectator the data
						DatagramPacket sendPacket = new DatagramPacket(packet_data, packet_data.length,
																client.getAddr(), client.getPort());
					}
				}

        		return null;
			}//End call()

		});//End submit()ing anonymous object


    }

}