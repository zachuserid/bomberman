package Networking;

/** 
* @(#)ServerNetworkHandler.java
 *
 *
 * @author
 * @version 1.00 2014/2/24
 */

import java.net.*;
import java.util.regex.*;
import BombermanGame.*;

public class BombermanServerNetworkHandler extends ServerNetworkHandler<World, PlayerCommand> {

    //Specific Bomberman server related data members

	//If we know about the world, we can get rid of this
	final static int MAX_PLAYERS = 10;

    //Parsing expressions for Bomberman communication protocol
    String joinRE = "\\|\\|\\|.*?\\|\\|\\|";
	Pattern joinPat = Pattern.compile(joinRE);


	//Constructors
	
    public BombermanServerNetworkHandler(int size, int port) {
    	super(new World[size], new World[size], new PlayerCommand[size], new PlayerCommand[size], port);
    }

	//Methods

	/*
	 * parseData() will handle individual player messages.
	 * It will gather the various command information
	 * and use it to create a new instance of a R
	 * from a factory method.
	 */
	@Override
    public PlayerCommand parseReceive(byte[] data)
    {
		//TODO: convert the byte array into a playerCommand object

    	return (PlayerCommand)null;
    }

	
    @Override
    public byte[] parseSend(World world)
    {
    	//TODO: convert world data into byte array
		return new byte[world.getGridWidth() * world.getGridHeight()];
    }

    
    @Override
    public World getSendCopy(World orig)
    {
    	//See this method in BombermanClientNetowkrHandler
    	// for implementation of World.getCopy()
    	return orig.getCopy();

    }

    
	@Override
    public PlayerCommand getReceiveCopy(PlayerCommand orig)
    {
    	return orig.getCopy();
    }

	
 	/*
     * If any commands are player joins, add the player.
     * Otherwise, lock on the write buffer and buffer the packet
     */
    @Override
    public void preProcessPacket(DatagramPacket packet)
    {

		String buf = new String( packet.getData() );
    	//Handle any join requests
    	Matcher m = joinPat.matcher(buf);

    	while(m.find())
    	{
    		//send player details to handleNewPlayer()
    		String name = m.group().split(",")[1];
    		handleNewPlayer(name, packet.getAddress(), packet.getPort());
    	}

    	//Remove these join messages from the other commands
		buf = buf.replaceAll(joinRE, "");

		packet.setData( buf.getBytes() );

    }

    
    void handleNewPlayer(String name, InetAddress ip, int port){
    	if ( !canAddPlayer() ){
    		return;
    	}
    	
		addSubscriber(ip, port);
    }

    
	//If this class knows about the world, this should be in there...
    private boolean canAddPlayer(){
		//Add any failing conditions here for cases that
		// players cannot be added
		if ( this.getSubscribers().size() >= MAX_PLAYERS ){
			return false;
		}

		return true;
	}

}