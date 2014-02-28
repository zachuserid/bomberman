package Networking;

/**
 * @(#)BombermanClientNetworkHandler.java
 *
 *
 * @author
 * @version 1.00 2014/2/26
 */

import java.net.*;
import BombermanGame.*;

public class BombermanClientNetworkHandler extends ClientNetworkHandler<PlayerCommand, World> {

    //Data members
	
	//Constructors

    public BombermanClientNetworkHandler(int size, InetAddress ip, int port) {
    	super(new PlayerCommand[size], new PlayerCommand[size], new World[size], new World[size], ip, port);
    }

    //Methods
    
	@Override
	protected int parseReceive(World array[], int currIndex, byte[] data)
	{
		/*
		 * TODO: We know the dimensions of a world, and that
		 * we only want to deal with the last world update from
		 * the server. We will simply truncate the data for the
		 * last width * height bytes;
		 */
		
		int w  = (int)Math.sqrt(data.length);
		int h = w;
		char grid[][] = new char[w][h];
		
		array[currIndex++] = new World( grid );
		
		return currIndex;
	}
	

	@Override
	protected byte[] parseSend(PlayerCommand data)
	{
		//TODO: turn the playerCommand into a protocol string
		return new byte[10];
	}
	

	@Override
    public PlayerCommand getSendCopy(PlayerCommand orig)
    {
    	return orig.getCopy();
    }

	
	@Override
    public World getReceiveCopy(World orig)
    {
		return orig.getCopy();
    }
	
	//Main
	public static void main(String args[]){
		
		if ( args.length < 2 )
		{
			System.out.println("Usage: java BombermanClientNetworkHandler <ip address> <port>");
			return;
		}
		
		int port = Integer.parseInt(args[1]);
		
		InetAddress addr;
    	try {
    		addr = InetAddress.getByName( args[0] );
    	} catch (UnknownHostException e)
    	{
    		System.out.println("Unknown host exception.");
    		return;
    	}
    	
    	BombermanClientNetworkHandler client = new BombermanClientNetworkHandler(50000, addr, port);
    	
		if ( !client.Initialize() )
    	{
    		System.out.println("Failed to start client.");
    		return;
    	}
		
	}


}