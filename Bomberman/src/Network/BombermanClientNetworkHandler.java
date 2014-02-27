package Network;

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
	protected World parseReceive(byte[] data)
	{
		//TODO: convert the byte array into a world
		
		int w  = (int)Math.sqrt(data.length);
		int h = w;
		char grid[][] = new char[w][h];
		
		return new World( grid );
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


}