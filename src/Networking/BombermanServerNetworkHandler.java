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
    public int parseReceive(PlayerCommand[] array, int currIndex, byte[] data)
    {
		/*
		 * TODO: The receive() could potentially obtain
		 * many client update requests as one packet.
		 * 
		 * We must delimit the data into various packets based on
		 * some protocol. It may be as simple as prepending the number
		 * of bytes for the packet as the first 3 chars in the byte array
		 * before the client sends it, which can be parseInt() and we would 
		 * then be able to separate the Objects and append the new ones to the array.
		 */
		
		/*
		 * I'm thinking the protocol string can look something like this: 
		 * ":|||join, <name>|||:<timestamp>,<name>||<command>|<details|...>||<command2>|<details|...>"
		 * ex: :|||join, zach|||:1231241,zach||move|5|7||move|4|7:1235213,alex||move|7|7||move|7|8
		 * always start player update with ':'
		 * new player sends |||join, name|||
		 * others send ||command|x|y||...
		 */
		
		//split the array based on above, or using a delimeter
		// (see NetworkHandlerTest.parseReceive() for simple example).
		
		//for (int i=0; i<split.legth; i++)
		//{
			//below params will come from parsing this individual player update protocol str
		//	PlayerCommand pc = new PlayerCommand(null, 0.02f, 1);
		//	array[currIndex++] = pc;
		//}
		
		
    	return currIndex;
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
    
    
    //Main
  	public static void main(String args[]){
  		
  		if ( args.length < 1 )
  		{
  			System.out.println("Usage: java BombermanServerNetworkHandler <port>");
  			return;
  		}
  		
  		int port = Integer.parseInt(args[0]);
      	
      	BombermanServerNetworkHandler server = new BombermanServerNetworkHandler(50000, port);
      	
  		if ( !server.Initialize() )
      	{
      		System.out.println("Failed to start server.");
      		return;
      	}
  		
  	}

}