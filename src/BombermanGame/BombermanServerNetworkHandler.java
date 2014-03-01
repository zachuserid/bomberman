package BombermanGame;

import java.util.regex.Pattern;
import java.net.*;
import java.util.regex.*;
import Networking.ServerNetworkHandler;

public class BombermanServerNetworkHandler extends ServerNetworkHandler<World, PlayerCommand[]> {

	//Members
	
	private int MAX_PLAYERS = 10;
	
	//Parsing expressions for Bomberman communication protocol
    private String joinRE = "\\|\\|\\|.*?\\|\\|\\|";
	private Pattern joinPat = Pattern.compile(joinRE);
	
	//Constructor
	
	public BombermanServerNetworkHandler(int port) {
		super(port);
	}
	
	//Methods
	
	void handleNewPlayer(String name, InetAddress ip, int port)
	{
    	if ( !canAddPlayer() ){
    		return;
    	}
    	
		this.addSubscriber(name, ip, port);
    }

    
	//If this class knows about the world, this should be in there...
    private boolean canAddPlayer()
    {
		//Add any failing conditions here for cases that
		// players cannot be added
		if ( this.getSubscribers().size() >= this.MAX_PLAYERS ){
			return false;
		}

		return true;
	}

 	/*
     * If any commands are player joins, add the player.
     * Otherwise, lock on the write buffer and buffer the packet
     */
    @Override
    public boolean preProcessPacket(DatagramPacket packet)
    {

		String buf = new String( packet.getData() );
    	//Handle any join requests
    	Matcher m = this.joinPat.matcher(buf);

    	while(m.find())
    	{
    		//send player details to handleNewPlayer()
    		String name = m.group().split(",")[1];
    		handleNewPlayer(name, packet.getAddress(), packet.getPort());
    	}

    	//Remove these join messages from the other commands
		buf = buf.replaceAll(this.joinRE, "");

		packet.setData( buf.getBytes() );

		return true;
		
    }

	@Override
	protected PlayerCommand[][] parseReceive(byte[] data)
	{
		/*
		 * Carlton, this is the string that we send:
		 * ':<time>,<id>,<PlayerCommandType>,<time>,<id>,<PlayerCommandType>,...'
		 * Where the ':' means start of a player's command
		 * and each command itself is made up of the following:
		 * <time>, which is the time of sending the update
		 * <id>, the order of this update compared to all requests
		 * <PlayerCommandType> is the type of movement or bomb drop (enum).
		 */		
		String protoStr = new String(data);
		
		String playerUpdateStrs[] = protoStr.split(":");
		
		//Give space for an array for each player. Note length - 1 since the split
		// will give empty String since it is the first character in the protocol
		PlayerCommand commands[][] = new PlayerCommand[playerUpdateStrs.length-1][];

		//For all players who sent update requests
    	for (int i=1; i<playerUpdateStrs.length; i++)
    	{
    		
    		String updates[] = playerUpdateStrs[i].split(",");
    		//For all updates that they sent
    		
    		/*
    		 * Instantiate this player's array. It has length
    		 * of the update array length/3, as that is the
    		 * number of updates in the protocol string from
    		 * this client. We can assume a multiple of 3 in the
    		 * string length so long as join games are properly
    		 * filtered in preProcessPacket()
    		 */
    		commands[i] = new PlayerCommand[updates.length/3];
    		
    		for (int j=0; j<updates.length; j+=3)
    		{
    			float time = Float.parseFloat(updates[j]);
    			int id = Integer.parseInt(updates[j+1]);
    			PlayerCommandType type = PlayerCommandType.values()[Integer.parseInt(updates[j+2])];
    			
    			commands[i][j] = new PlayerCommand(type, time, id);
    			
    		}
    	}
    	
		return commands;
	}


	@Override
	protected byte[] parseSend(World world)
	{
		return world.getBytes();
	}


	@Override
	protected World getSendCopy(World original)
	{
		return original.getCopy();
	}


	@Override
	protected PlayerCommand[] getReceiveCopy(PlayerCommand[] original) 
	{
		PlayerCommand cp[] = new PlayerCommand[original.length];
		
		for (int i=0; i<original.length; i++)
		{
			cp[i] = original[i].getCopy();
		}
		
		return cp;
	}

}
