package BombermanGame;

import java.util.regex.Pattern;
import java.net.*;
import java.util.regex.*;
import Networking.ServerNetworkHandler;
import Networking.Subscriber;

public class BombermanServerNetworkHandler extends ServerNetworkHandler<World, PlayerCommand[]> {

	//Members
	
	private final int MAX_PLAYERS = PlayerName.values().length;
	//An index for the PlayerNames to use
	private int namesUsed = 0;
	
	//Parsing expressions for Bomberman communication protocol
    private String joinRE = "\\|\\|\\|.*?\\|\\|\\|";
	private Pattern joinPat = Pattern.compile(joinRE);
	
	//Constructor
	
	public BombermanServerNetworkHandler(int port) {
		super(port);
	}
	
	//Methods
	
	void handleNewPlayer(String name, InetAddress ip, int port, boolean playing)
	{
    	if ( playing && canAddPlayer() )
    	{
    		String playerName = PlayerName.values()[this.namesUsed++].toString();
    		//Could add logic to use playerName, only if name is taken..
    		name = playerName;
    	}
    	
		this.addSubscriber(name, ip, port);
    }

    
	//If this class knows about the world, this should be in there...
    private boolean canAddPlayer()
    {
		//Add any failing conditions here for cases that
		// players cannot be added
		if ( this.namesUsed == this.MAX_PLAYERS ){
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
    		System.out.println("FOUND THE PATTERN TO ADD NEW PLAYER");
    		//send player details to handleNewPlayer()
    		String request = m.group().split(",")[0]; //join or watch
    		String name = m.group().split(",")[1].trim().toLowerCase();
    		if ( request.equals("|||join") ) 
    			handleNewPlayer(name, packet.getAddress(), packet.getPort(), true);
    		else if ( request.equals("|||watch") )	
    			handleNewPlayer(name, packet.getAddress(), packet.getPort(), false);
    		
    		//Add the join PlayerCommand to the front of the string
    		buf = buf.replace(":",  ":0,0,"+PlayerCommandType.Join.toString());
    		
    	}

    	//Remove these join messages byte buffer
		buf = buf.replaceAll(this.joinRE, "");
		
		//Get the subscriber's name from the IP and port or packet.
		String theName = "";
		for (Subscriber s: this.getSubscribers())
		{
			//Compare this packet to our list of subscribers which have names
			if ( s.getAddr().equals(packet.getAddress()) && s.getPort() == packet.getPort() ){
				theName = s.getName();
				break;
			}
		}
		
		System.out.println("replacing string " + buf);
		//append this data to the bytes that the client sent.
		buf = buf.replace(":", ":"+theName+",");
		
		System.out.println("We are receiving a message from '" + theName + "', updating payload to " + buf);

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
		
		System.out.println("Parsing string: "+protoStr);
		
		String playerUpdateStrs[] = protoStr.split(":");

		//Give space for an array for each player. Note length - 1 since the split
		// will give empty String since it is the first character in the protocol
		PlayerCommand commands[][] = new PlayerCommand[playerUpdateStrs.length-1][];

		//For all players who sent update requests
    	for (int i=1; i<playerUpdateStrs.length; i++)
    	{
    		
    		//check that entire message was not stripped (new player)
    		if ( playerUpdateStrs[i].trim().equals(",") ){
    			System.out.println("CONTINUING");
    			commands[i-1] = null;
    			continue;
    		}
    		
    		System.out.println("player update at " + i + ": '" + playerUpdateStrs[i].trim() + "'");
    		    		
    		String updates[] = playerUpdateStrs[i].split(",");
    		
    		//Get the name from the string, as it was appended by preProcessPacket()
    		String playerName = updates[0];
    		      		
    		/*
    		 * Instantiate this player's array. It has length
    		 * of the update array length/3, as that is the
    		 * number of updates in the protocol string from
    		 * this client, after removing the name that
    		 * we added in preProcessPacket(). 
    		 * We can assume a multiple of 3 in the
    		 * string length so long as join games are properly
    		 * filtered in preProcessPacket()
    		 */
    		commands[i-1] = new PlayerCommand[ (updates.length-1) /3];
    		int commandsIndex=0;
    		System.out.println("length: " + updates.length + ", commands.length: " + commands[i-1].length);
    		
    		for (int j=1; j<updates.length-1; j+=3)
    		{
    			System.out.println("updates starting at " + j + " are " + updates[j] + ", " + updates[j+1] +", " + updates[j+2] + ". Player name: " + playerName);
    			float time = Float.parseFloat(updates[j]);
    			int id = Integer.parseInt(updates[j+1]);
    			PlayerCommandType type = PlayerCommandType.valueOf(updates[j+2].trim());
    			
    			commands[i-1][commandsIndex++] = new PlayerCommand(playerName, type, time, id);
    			
    			System.out.println("created command for player " + commands[i-1][commandsIndex-1].PlayerName);
    			
    		}
    	}
    	
		return commands;
	}


	@Override
	protected byte[] parseSend(World world)
	{
		byte worldBytes[] = world.getBytes();
		
		byte byteData[] = new byte[worldBytes.length+2];
		
		byteData[0] = (byte)world.getGridWidth();
		byteData[1] = (byte)world.getGridHeight();
		
		for ( int i=0; i<worldBytes.length; i++ )
		{
			byteData[i+2] = worldBytes[i];
		}
		
		return byteData;
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
