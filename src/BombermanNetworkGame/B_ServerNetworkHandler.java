package BombermanNetworkGame;

import java.util.ArrayList;
import java.net.*;
import BombermanGame.B_Player;
import BombermanGame.Bomb;
import BombermanGame.PlayerCommand;
import BombermanGame.PlayerCommandType;
import BombermanGame.PlayerName;
import BombermanGame.Utils;
import Networking.DoubleBuffer;
import Networking.ServerNetworkHandler;
import Networking.Subscriber;

public class B_ServerNetworkHandler extends ServerNetworkHandler<B_NetworkPacket, PlayerCommand[]> {

	//Members

	protected DoubleBuffer<PlayerCommand> doubleJoinBuffer;

	protected int maxPlayers;

	protected int currentPlayers;
	
	protected int spectators;
	
	protected int gridWidth;
	
	protected int gridHeight;

	//Constructor

	public B_ServerNetworkHandler(int port, int maxPlayers, int width, int height) {
		super(port);

		this.maxPlayers = maxPlayers;
		
		this.spectators = 0;
		
		this.gridWidth = width;
		this.gridHeight = height;

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
	private void ackNewClient(String name, int xPos, int yPos, int width, int height, boolean playing)
	{
		byte ackPacket[] = new byte[10];
		ackPacket[0] = Utils.intToByte(PlayerCommandType.valueOf("Join").ordinal()); //[0] = message type

		int ackC = this.getSubscriberByName(name).getAckCount();

		byte commandIdBytes[] = Utils.intToStrByteArr(ackC);

		for (int i=0; i<commandIdBytes.length; i++) 
			ackPacket[i+1] = commandIdBytes[i]; //[1,..,4] = highest command to acknowledge

		int playerNumber;
		if (playing)
			playerNumber = PlayerName.valueOf(name).ordinal();
		else
			playerNumber = 0;

		ackPacket[5] = Utils.intToByte(playerNumber); //[5] = the player's number

		ackPacket[6] = Utils.intToByte(xPos); //[6] = x position
		ackPacket[7] = Utils.intToByte(yPos); //[7] = y position

		ackPacket[8] = Utils.intToByte(width); //[8] = world object width
		ackPacket[9] = Utils.intToByte(height); //[9] = world object height

		//System.out.println("Acking join request for player " + p.getName() + " with data: " + new String(ackPacket));

		this.sendData(name, ackPacket);
	}
	
	public void ackSpectatorRequest(String name, int width, int height)
	{
		this.ackNewClient(name, 0, 0, width, height, false);
	}
	
	public void ackJoinRequest(B_Player p, int width, int height)
	{
		this.ackNewClient(p.getName(), p.getX(), p.getY(), width, height, true);
	}

	protected void handleNewSubscriber(InetAddress ip, int port, boolean playing, int ackC)
	{
		String playerName = "spectator"+this.spectators++;

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
    	
    	if (!playing)
    		this.ackSpectatorRequest(playerName, this.gridWidth, this.gridHeight);
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
		
		//Note: ignoring byte[0] because it is the player delimeter (:)

    	//In here means new player or spectator request
		if (thisCommand == PlayerCommandType.Join)
    	{
			//System.out.println("Pre processing a join!");
    		//Confirm not a resend after adding the player
    		if (theName.equals(""))
    		{
    			//Get the int for the command id to ack
    			byte bytesToInt[] = {raw_data[2], raw_data[3], raw_data[4], raw_data[5]};
    			int ackInt = Utils.byteArrToStrInt(bytesToInt);
    			
	    		if ((new String(new byte[]{raw_data[6]})).equals("1"))
	    			handleNewSubscriber(packet.getAddress(), packet.getPort(), true, ackInt);
	    		else
	    			handleNewSubscriber(packet.getAddress(), packet.getPort(), false, ackInt);
    		}

    		return false;
    	}

		//append this data to the bytes that the client sent.
		buf = buf.replace(":", ":"+theName+",");

		//System.out.println("We are receiving a message from '" + theName + "', updating payload to " + buf);

		packet.setData( buf.getBytes() );

		return true;

    }

	@Override
	protected PlayerCommand[][] parseReceive(byte[] data)
	{
		//byte data received as string
		String protoStr = new String(data);

		//split to obtain commands per player
		String playerUpdateStrs[] = protoStr.split(":");

		//Give space for an array for each player. Note length - 1 since the split
		// will give empty String since it is the first character in the protocol
		PlayerCommand commands[][] = new PlayerCommand[playerUpdateStrs.length-1][];

		//maintain a list of all player names to ack after we build PlayeComands
		String playerNames[] = new String[playerUpdateStrs.length-1];

		//For all players who sent update requests
    	for (int i=1; i<playerUpdateStrs.length; i++)
    	{
    		//Remove the name and delimeters from the buffer, added by preProcessPacket()
    		String splitS[] = playerUpdateStrs[i].split(",");
    		
    		playerNames[i-1] = splitS[0];

    		//Deal with the bytes now
    		byte updateBytes[] = splitS[1].trim().getBytes();
    		
    		//this method knows that the byte represents a character of an int
    		int updateType = Utils.byteToInt(updateBytes[0]);
    		
    		//convert the 4 bytes storing a 4 character integer into the command id
    		byte bytesToInt[] = {updateBytes[1], updateBytes[2], updateBytes[3], updateBytes[4]};
			int currentCommandId = Utils.byteArrToStrInt(bytesToInt);
			    		
    		//Instantiate inner array with length based on this player's update size
    		if (updateType == PlayerCommandType.valueOf("Update").ordinal())
    		{
    			//System.out.println("Server: This client's update byte str: " + new String(updateBytes));
    			ArrayList<PlayerCommand> playerCommands = new ArrayList<PlayerCommand>();
    		
	    		for (int j=5; j<updateBytes.length; j++)
	    		{
	    			int id = currentCommandId++;

	    			//this method assumes the byte arr represents a character of the int
	    			int messageType = Utils.byteArrToStrInt(new byte[]{ updateBytes[j] });

	    			if (messageType >= PlayerCommandType.values().length) 
	    				return new PlayerCommand[][] { { null } };

	    			String commandStr = PlayerCommandType.values()[messageType].toString();

	    			PlayerCommandType type = PlayerCommandType.valueOf(commandStr);

	    			PlayerCommand toAdd = null;
	    			
	    			//update highest received packet for each player
	    			Subscriber thisSub = this.getSubscriberByName(playerNames[i-1]);
	    			if (thisSub != null)
	    			{
	    				if (id > thisSub.getAckCount())
	    				{
	    					thisSub.setAckCount(id);
	    	    			toAdd = new PlayerCommand(playerNames[i-1], type, id);
	    				}
	    			}

	    			if (toAdd != null) playerCommands.add(toAdd); 

	    		}//End for
    		
	    		commands[i-1] = new PlayerCommand[playerCommands.size()];
	    		playerCommands.toArray(commands[i-1]);

	    		//ack all players with highest command id received
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
    		
    		byte toByte[] = Utils.intToStrByteArr(highAck);
    		
    		byte toSend[] = new byte[toByte.length+1];
    		
    		toSend[0] = Utils.intToByte(7); //header
    		
    		for (int u=1; u<toSend.length; u++){
    			toSend[u] = toByte[u-1];
    		}
    		
    		this.sendData(players[k], toSend);
    	}
	}

	@Override
	protected byte[] parseSend(B_NetworkPacket data)
	{
		//number of bytes for one player's data
		int player_bytes = 7;
		int num_players = 4;
		
		byte worldBytes[] = data.getWorld().getBytes();

		ArrayList<Bomb> bombs = data.getWorld().getBombs();
		int numBombs = bombs.size();
		
		//breakdown of payload size:
		//num bytes in world [][] + 4*5+1 forall player information + numBombs*4+1 for bombs info
		byte byteData[] = new byte[worldBytes.length+(num_players*player_bytes)+1+(numBombs*4)+1];

		//[0] = command type
		byteData[0] = Utils.intToByte(PlayerCommandType.Update.ordinal()); //header packet type

		B_Player players[] = data.getPlayers();
		//[1..5] [6..10] [11..15] [16..20] is the player[i]s stats:
		//[xPos, yPox, killCount, Powerup, isAlive]
		for (int i=0; i<this.maxPlayers*player_bytes; i+=player_bytes)
		{
			byteData[i+1] = Utils.intToByte(players[i/player_bytes].getX());
			byteData[i+2] = Utils.intToByte(players[i/player_bytes].getY());
			byteData[i+3] = Utils.intToByte(players[i/player_bytes].getKillCount());
			byteData[i+4] = Utils.intToByte(players[i/player_bytes].getPowerup().ordinal());
			if (players[i/player_bytes].isAlive())
				byteData[i+5] = Utils.intToByte(1);
			else
				byteData[i+5] = Utils.intToByte(0);
			
			String bombC = Utils.intToPaddedStr(players[i/player_bytes].getBombCount(), 2);
			byteData[i+6] = bombC.getBytes()[0];
			byteData[i+7] = bombC.getBytes()[1];
			
			//debug
			//System.out.println("Sending player ("+players[i/player_bytes].getX()+","+players[i/player_bytes].getY()+") " +
			//		           i+" killcount: "+players[i/player_bytes].getKillCount()
			//		           +" powerup: "+players[i/player_bytes].getPowerup().ordinal()
			//		           +" alive: " + players[i/player_bytes].isAlive() 
			//		           +" bombs: " + players[i/player_bytes].getBombCount());
		}

		//[18..n] = world grid array
		for ( int i=0; i<worldBytes.length; i++ )
		{
			byteData[i+(num_players*player_bytes)+1] = worldBytes[i];
		}
		
		//number of bombs
		int start = worldBytes.length + (num_players*player_bytes)+1;

		//Place all bomb information in the packet
		byteData[start++] = Utils.intToByte(numBombs);
		System.out.println("num bombs: " + numBombs);
		for (int i=0; i<numBombs; i++)
		{
			Bomb bomb = bombs.get(i);
			int playerNum = PlayerName.valueOf(bomb.getName()).ordinal();
			byteData[start++] = Utils.intToByte(playerNum);
			int bX = bomb.getX();
			byteData[start++] = Utils.intToByte(bX);
			int bY = bomb.getY();
			byteData[start++] = Utils.intToByte(bY);
			int range = bomb.getRange();
			byteData[start++] = Utils.intToByte(range);
			float time = bomb.getTime();
			//5 char string representing the float to send
			String fTime = Utils.floatToPaddedStr(time, 5);
			for (int j=0; j<5; j++)
				byteData[start++] = fTime.getBytes()[j];
		}

		return byteData;
	}


	@Override
	protected B_NetworkPacket getSendCopy(B_NetworkPacket original)
	{
		return original.getCopy();
	}


	@Override
	protected PlayerCommand[] getReceiveCopy(PlayerCommand[] original) 
	{
		ArrayList<PlayerCommand> cpList = new ArrayList<PlayerCommand>();

		for (int i=0; i<original.length; i++)
		{
			try {
				cpList.add(original[i].getCopy());
			} catch(Exception e){}
		}

		PlayerCommand cp[] = new PlayerCommand[cpList.size()];
		cpList.toArray(cp);

		return cp;
	}
}