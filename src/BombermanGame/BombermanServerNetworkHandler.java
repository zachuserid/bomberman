package BombermanGame;

import java.util.ArrayList;
import java.net.*;
import Networking.DoubleBuffer;

import Networking.ServerNetworkHandler;
import Networking.Subscriber;

public class BombermanServerNetworkHandler extends ServerNetworkHandler<World, PlayerCommand[]> {

	//Members
	
	protected DoubleBuffer<PlayerCommand> doubleJoinBuffer;
	
	protected int maxPlayers;
	
	protected int currentPlayers;
	
	//Constructor
	
	public BombermanServerNetworkHandler(int port, int maxPlayers) {
		super(port);
		
		this.maxPlayers = maxPlayers;
		
		this.doubleJoinBuffer = new DoubleBuffer<PlayerCommand>();
	}
	
	//Methods
	
	public PlayerCommand[] getJoinRequests()
    {
    	ArrayList<PlayerCommand> joins = this.doubleJoinBuffer.readAll(true);
    	
    	PlayerCommand joinCommands[] = new PlayerCommand[joins.size()];
    	
    	joins.toArray(joinCommands);
    	
    	return joinCommands;
    }
	
	public void Send(String name, String data)
	{
		this.sendData(name, data.getBytes());
	}
	
	//After the server sorts out player position, character and jazz,
	// send the player specific data to the client in an ack
	//TODO: remember to get the server to call all these after receiving join requests
	public void ackJoinRequest(BombermanPlayer p, int width, int height)
	{
		byte ackPacket[] = new byte[11];
		ackPacket[0] = (byte)PlayerCommandType.valueOf("Join").ordinal(); //[0] = message type
		
		int ackC = this.getSubscriberByName(p.getName()).getAckCount();
		byte commandIdBytes[] = java.nio.ByteBuffer.allocate(4).putInt(ackC).array();
		for (int i=0; i<commandIdBytes.length; i++) 
			ackPacket[i+1] = commandIdBytes[i]; //[1,..,4] = highest command to acknowledge
		
		int playerNumber = PlayerCommandType.valueOf(p.getName()).ordinal();
		ackPacket[5] = (byte)playerNumber; //[5] = the player's number
		
		ackPacket[6] = (byte)p.getCharacter(); //[6] = the player's char representation
		
		ackPacket[7] = (byte)p.getX(); //[7] = x position
		ackPacket[8] = (byte)p.getY(); //[8] = y position
		
		ackPacket[9] = (byte)width; //[9] = world object width
		ackPacket[10] = (byte)height; //[10] = world object height
		
		this.sendData(p.getName(), ackPacket);
	}
	
	protected void handleNewSubscriber(InetAddress ip, int port, boolean playing, int ackC)
	{
		String playerName = ""; //TODO: Should add names to spectators also..
		
    	if ( playing && canAddPlayer() )
    	{
    		playerName = PlayerName.values()[this.currentPlayers++].toString();
    		
    		PlayerCommand c = new PlayerCommand(playerName, PlayerCommandType.Join, 0);
    		
    		//add the join command to the join buffer without swapping after
    		doubleJoinBuffer.write(c, false);
    	}
    	
		this.addSubscriber(playerName, ip, port);
    	
		//Added subscriber, ack their join
		Subscriber sub = this.getSubscriberByName(playerName);
    	if (sub != null)
    	{
    		//Update the count, acknowledging receving join request..
    		sub.setAckCount(ackC);
    	}    	
    }
    
	//If this class knows about the world, this should be in there...
    private boolean canAddPlayer()
    {
		//Add any failing conditions here for cases that
		// players cannot be added
		if ( this.currentPlayers == this.maxPlayers ){
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

    	byte raw_data[] = packet.getData();  
		String buf = new String( packet.getData() );
		
		int cNum = Utils.byteToInt(raw_data[1]);
		PlayerCommandType thisCommand = PlayerCommandType.values()[cNum];

		// Get the subscriber's name from the IP and port or packet.
		String theName = "";
		for (Subscriber s : this.getSubscribers())
		{
			// Compare this packet to our list of subscribers which have names
			if (s.getAddr().equals(packet.getAddress())
					&& s.getPort() == packet.getPort())
			{
				theName = s.getName();
				break;
			}
		}
		
		//TODO: Must be a better way to do casts from byte
    			
		//Note: ignoring byte[0] because it is the player delimeter (:)
		
    	//In here means new player or spectator request
		if (thisCommand == PlayerCommandType.Join)
    	{
			System.out.println("Pre processing a join!");
    		//Confirm not a resend after adding the player
    		if (theName.equals(""))
    		{
    			//Get the int for the command id to ack
    			byte bytesToInt[] = {raw_data[2], raw_data[3], raw_data[4], raw_data[5]};
    			int ackInt = java.nio.ByteBuffer.wrap(bytesToInt).getInt();
    			
	    		if ((new String(new byte[]{raw_data[6]})).equals("1"))
	    			handleNewSubscriber(packet.getAddress(), packet.getPort(), true, ackInt);
	    		else
	    			handleNewSubscriber(packet.getAddress(), packet.getPort(), false, ackInt);
    		}
	
    		return false;
    	}
		
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
		 * Protocol format:
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
		
		//maintain a list of all player names to ack after we build update requests
		String playerNames[] = new String[playerUpdateStrs.length-1];

		//For all players who sent update requests
    	for (int i=1; i<playerUpdateStrs.length; i++)
    	{
    		
    		//Remove the name and delimeters from the buffer, added by preProcessPacket()
    		String splitS[] = playerUpdateStrs[i].split(",");
    		playerNames[i-1] = splitS[0];

    		//Deal with the bytes now
    		byte updateBytes[] = splitS[1].getBytes();
    		
    		int updateType = (int)updateBytes[0];
    		
    		byte bytesToInt[] = {updateBytes[1], updateBytes[2], updateBytes[3], updateBytes[4]};
    		//The starting id
			int currentCommandId = java.nio.ByteBuffer.wrap(bytesToInt).getInt();
    		      		
    		/*
    		 * Instantiate this player's array with length 
    		 * appropriate to this player's update size
    		 */
    		if (updateType == PlayerCommandType.valueOf("Update").ordinal())
    		{
    			ArrayList<PlayerCommand> playerCommands = new ArrayList<PlayerCommand>();
    		
	    		for (int j=1; j<updateBytes.length; j++)
	    		{
	    			int id = currentCommandId++;
	    			String commandStr = PlayerCommandType.values()[(int)updateBytes[j]].toString();
	    			PlayerCommandType type = PlayerCommandType.valueOf(commandStr);
	    			    			
	    			//TODO: Ack everyone. Also, make sure joins are not getting here.
	    			
	    			//Unless the id is above our current
	    			// counter for this client, ignore this message,
	    			// it's a resend due to our ack not yet reaching client
	    			PlayerCommand toAdd = null;
	    			Subscriber thisSub = this.getSubscriberByName(playerNames[i-1]);
	    			if (thisSub != null)
	    			{
	    				//Acknowledge highest received id
	    				if (id > thisSub.getAckCount())
	    				{
	    					thisSub.setAckCount(id);
	    	    			toAdd = new PlayerCommand(playerNames[i-1], type, id);
	    				}
	    			}
	    			
	    			if (toAdd != null) playerCommands.add(toAdd); 
	    			
	    			//System.out.println("created command for player " + commands[i-1][commandsIndex-1].PlayerName);	
	    		}//End for
    		
	    		commands[i-1] = new PlayerCommand[playerCommands.size()];
	    		playerCommands.toArray(commands[i-1]);
	    		
	    		//before returning our parsed update requests, ack all
	        	ackPlayers(playerNames);
    		}
    	}
    	
		return commands;
	}
	
	protected void ackPlayers(String players[])
	{
		for (int k=0; k<players.length; k++)
    	{ 
    		//The ack packet will have a header of 7 (PlayerCommandType.Ack)
    		//The payload will be 4 bytes representing the highest command id int
    		int highAck = this.getSubscriberByName(players[k]).getAckCount();
    		
    		System.out.println("Server acking player " + players[k]
			          + " for highwatermark: " + highAck);
    		
    		byte toByte[] = java.nio.ByteBuffer.allocate(4).putInt(highAck).array();
    		byte toSend[] = new byte[toByte.length+1];
    		
    		toSend[0] = (byte)7; //header
    		
    		for (int u=1; u<toSend.length; u++){
    			toSend[u] = toByte[u-1];
    		}
    		
    		this.sendData(players[k], toSend);
    	}
	}

	@Override
	protected byte[] parseSend(World world)
	{
		byte worldBytes[] = world.getBytes();
		
		byte byteData[] = new byte[worldBytes.length+3];
		
		//comment about this
		byteData[0] = (byte)0; //header packet type
		byteData[1] = (byte)world.getGridWidth();
		byteData[2] = (byte)world.getGridHeight();
		
		for ( int i=0; i<worldBytes.length; i++ )
		{
			byteData[i+3] = worldBytes[i];
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
