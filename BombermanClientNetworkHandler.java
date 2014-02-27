/**
 * @(#)BombermanClientNetworkHandler.java
 *
 *
 * @author
 * @version 1.00 2014/2/26
 */

import java.util.concurrent.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class BombermanClientNetworkHandler extends ClientNetworkHandler<PlayerUpdate, World> {

    //Data members

    public BombermanClientNetworkHandler(int size, InetAddress ip, int port) {
    	super(new PlayerUpdate[size], new PlayerUpdate[size], new World[size], new World[size], ip, port);
    }


	@Override
	protected World parseReceive(byte[] data)
	{
		//TODO: convert the byte array into a world
		int w  = (int)Math.sqrt(data.length);
		int h = w;
		return new World( w, h );
	}

	@Override
	protected byte[] parseSend(PlayerUpdate data)
	{
		//TODO: turn the playerCommand into a protocol string
		return new byte[10];
	}

	@Override
    public void Send(byte[] packet_data)
    {
			//Send this spectator the data
			DatagramPacket sendPacket = new DatagramPacket(packet_data,
								packet_data.length, address, port);
			try {
				socket.send(sendPacket);
			} catch(Exception e){}
    }

	@Override
    public PlayerUpdate getSendCopy(PlayerUpdate orig)
    {
    	return orig.getCopy();
    }

	@Override
    public World getReceiveCopy(World orig)
    {
    	//Should resemble the following..
		return orig.getCopy();
		/*
    	int wid = orig.getWidth();
    	int hei = orig.getHeight();

    	World w = new World(wid, hei);

    	for( int i=0; i<wid; i++ )
    	{
    		for ( int j=0; j< hei; j++ )
    		{
    			w.grid[i][j] = orig.getCharAt(i, j);
    		}
    	}

    	return w;
    	*/
    }

}