package BombermanNetworkGame;


import BombermanGame.B_Packet;
import BombermanGame.B_Player;
import BombermanGame.Bomb;
import BombermanGame.GridObject;
import BombermanGame.PlayerCommand;
import BombermanGame.PlayerCommandType;
import BombermanGame.PlayerName;
import BombermanGame.Point;
import BombermanGame.Powerup;
import BombermanGame.Utils;
import BombermanGame.World;
import Networking.ClientNetworkHandler;
import Networking.DoubleBuffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class B_ClientNetworkHandler extends ClientNetworkHandler<PlayerCommand[], B_Packet>{

	//Members

	int grid_width;
	int grid_height;
	
	DoubleBuffer<PlayerCommand> commandBacklog;

	//Constructor

	public B_ClientNetworkHandler(String ip, int port)
	{
		super(ip, port);
		//Initialize dimensions as "unknown" until we receive some packets
		grid_width = -1;
		grid_height = -1;

		commandBacklog = new DoubleBuffer<PlayerCommand>();
	}


	//Methods

	//Create packet requesting a join game, and call super.sendData();
	public void joinGame()
	{
		PlayerCommand joinCom[] = new PlayerCommand[1];

		joinCom[0] = new PlayerCommand(PlayerCommandType.Join, 0);

		this.Send(joinCom);
	}
    
	//Remove messages from our backlog buffer that have been confirmed by the server
	protected void handleAck(int highAck)
	{		
		//Received the ack, remove all from the backlog that
		// are <= highAck.
		ArrayList<PlayerCommand> backlog = this.commandBacklog.readAll(true);

		ArrayList<PlayerCommand> newBacklog = new ArrayList<PlayerCommand>();
		for (PlayerCommand pc: backlog)
		{
			if (pc.Id > highAck)
				newBacklog.add(pc);
			//else
			//	System.out.println("~~~~Removing acked packet: " + pc.Id);
		}

		PlayerCommand newBacklogArray[] = new PlayerCommand[newBacklog.size()];
		newBacklog.toArray(newBacklogArray);

		this.commandBacklog.writeAll(newBacklogArray, false);
	}

	/*
	 * Note: The first two bytes sent to us
	 * for each grid update will represent the
	 * width and height of the grid, respectively.
	 * As we are bounding the grid to 127 x 127,
	 * each value can be represented as one bytcommandBackloge.
	 */
	@Override
	protected B_Packet[] parseReceive(byte[] data) 
	{	
		B_Packet p = new B_Packet();

		int commandType; 

		try{
			commandType = Utils.byteToInt(data[0]);
			//System.out.println("Parsing: " + new String(data));
		} catch (NumberFormatException e){
			System.out.println("Number format exception parsing: '" + new String(data) + "'");
			commandType = -1;
		}

		/*
		 * This type is an acknowledgment from 
		 * the server to our join request. It will
		 * contain details regarding our character and
		 * starting position
		 * 
		 * PlayerCommandType[5] = Join
		 * p.Command = PlayerCommandType.Join
		 * p.Data = (BombermanPlayer):
		 * 		the player that is joining the game
		 */
		if(commandType == 5)
		{
			//Handle the ack value nested in the servers response to join game request
			byte bytesToInt[] = {data[1], data[2], data[3], data[4]};
			int ackValue = Utils.byteArrToStrInt(bytesToInt);
			handleAck(ackValue);

			//Build the player

			int playerNumber = Utils.byteToInt(data[5]);

			String playerName = PlayerName.values()[playerNumber].toString();

			int xPos = Utils.byteToInt(data[6]);
			int yPos = Utils.byteToInt(data[7]);

			this.grid_width = Utils.byteToInt(data[8]);
			this.grid_height = Utils.byteToInt(data[9]);

			B_Player player = new B_Player(playerName, new Point(xPos, yPos));

			p.Data = player;

			p.Command = PlayerCommandType.Join;

		}
		/*
		 * PlayerCommandType[7] = Ack
		 * p.Command = PlayerCommandType.Ack
		 * p.Data = (int): 
		 * 		highest acknowledged update request by server.
		 */
		else if (commandType == 7)
		{
			byte bytesToInt[] = {data[1], data[2], data[3], data[4]};
			int ackValue = Utils.byteArrToStrInt(bytesToInt);

			this.handleAck(ackValue);

			//Already handled the Ack, don't buffer this Packet
			p = null;
		}
		/*
		 * Dealing with a world update
		 * p.Command = PlayerCommandType.Update
		 * p.Data = (World):
		 * 		the character map representing the most recent world
		 */
		else if (commandType == 6) //update..
		{	
			if ( this.grid_width == -1 || this.grid_height == -1 )
			{
				//TODO: If not handled anywhere else, this indicates game started before
				// request to join. 
				System.out.println("ERROR: DID NOT RECEIVE INITIAL JOIN ACK BEFORE UPDATE.. GRID NOT SET");
				return null;
			}

			B_Player players[] = new B_Player[4];
			ArrayList<B_Player> playerList = new ArrayList<B_Player>();

			//TODO: handle player isDead bool, winner bytes when implemented in server send
			
			//Assuming 4 players here..
			for (int i=0; i<4; i++)
			{
				String pName = PlayerName.values()[i].toString();

				int xPos = Utils.byteToInt(data[(i*5)+1]);

				int yPos = Utils.byteToInt(data[(i*5)+2]);

				B_Player pl = new B_Player(pName, new Point(xPos, yPos));

				int kills = Utils.byteToInt(data[(i*5)+3]);
				pl.setKillCount(kills);

				Powerup powerup = Powerup.values()[Utils.byteToInt(data[(i*5)+4])];
				pl.setPowerup(powerup);
				
				int isAlive = Utils.byteToInt(data[(i*5)+5]);

				if (isAlive == 0)
					pl.Kill();
				
				players[i] = pl;
				playerList.add(pl);
				
				//debug
				//System.out.println("player ("+xPos+","+yPos+") "+i+" killcount: " 
				//                 + kills+" powerup: "+powerup+" isAlive: " + pl.isAlive());
			}

			GridObject[][] gridArr = new GridObject[this.grid_width][this.grid_height];

			int i=0, j=0;
			for (i=0; i<this.grid_width; i++)
			{
				for (j=0; j<this.grid_height; j++)
				{
					gridArr[i][j] = GridObject.values()[ data[ ( (i * this.grid_width) + j) + 21 ] ];
				}
			}

			//TODO: Deprecate below or fix integer parsing
			
			int startB = (i*j)+21;

			int numBombs = Utils.byteToInt(data[startB++]);
			ArrayList<Bomb> worldBombs = new ArrayList<Bomb>();

			for (int k=0; k<numBombs; k++)
			{
				int playerNum = Utils.byteToInt(data[startB++]);
				String bombName = PlayerName.values()[playerNum].toString();

				int bX = Utils.byteToInt(data[startB++]);
				int bY = Utils.byteToInt(data[startB++]);

				byte timeBytes[] = new byte[]{ data[startB++], data[startB++], data[startB++], data[startB++] };
				float bTime = ByteBuffer.wrap(timeBytes).getFloat();

				int bPower = Utils.byteToInt(data[startB++]);
				worldBombs.add(new Bomb(bombName, new Point(bX, bY), bPower, bTime));
			}

			p.Command = PlayerCommandType.Update;
			World world = new World(gridArr);
			world.setPlayers(playerList);
			world.setBombs(worldBombs);
			p.MetaData = players;
			p.Data = world;
		}

		return new B_Packet[] {p};
	}


	@Override
	protected byte[] parseSend(PlayerCommand[] commands) 
	{
		ArrayList<PlayerCommand> backlog = this.commandBacklog.readAll(true);

		//To store the new and backlogged commands
		PlayerCommand allCommands[] = new PlayerCommand[commands.length + backlog.size()];
		
		//debug
		//System.out.println("commands.length: " + commands.length + 
		//		". backlog.size: " + backlog.size() + 
		//		". all.length: " + allCommands.length);
		
		//populate all
		int count;
		for (count=0; count<backlog.size(); count++)
		{
			allCommands[count] = backlog.get(count);
		}
		for (int i=0; i<commands.length; i++)
		{
			allCommands[i+count] = commands[i];
		}

		String toSend = ":"; //Start of player request

		//Handle update or join command to send
		
		//append header (1 char)
		if (allCommands[0].Command == PlayerCommandType.Join)
			toSend += PlayerCommandType.valueOf("Join").ordinal();
		else
			toSend += PlayerCommandType.valueOf("Update").ordinal();
		
		//append command id (4 chars)
		toSend+= Utils.intToPaddedStr(allCommands[0].Id);

		if ( allCommands[0].Command == PlayerCommandType.Join )
		{
			//Not sending a name, server will take care of that
			System.out.println("Sending join game message");
			
			//whether playing or spectating (bool) (1 char)
			toSend += "1";
		}
		else
		{
			for (int i=0; i<allCommands.length; i++)
			{
				//the update type (1 char per iter.)
				toSend += allCommands[i].Command.ordinal();
			}
		}

		//Add these commands to the backlog to re-send unless 
		// we receive an ack for them.
		this.commandBacklog.writeAll(commands, false);
		
		//send our char[] as bytes
		return toSend.getBytes();
	}



	@Override
	protected PlayerCommand[] getSendCopy(PlayerCommand[] original) 
	{
		PlayerCommand commands[] = new PlayerCommand[original.length];

		for (int i=0; i<original.length; i++)
		{
			commands[i] = original[i].getCopy();
		}

		return commands;
	}



	@Override
	protected B_Packet getReceiveCopy(B_Packet original)
	{
		B_Packet p = new B_Packet();

		if(original.Command == PlayerCommandType.Join)
		{
			p.Command = PlayerCommandType.Join;

			B_Player or = (B_Player)original.Data;

			B_Player player = new B_Player(or.getName(), new Point(or.getX(), or.getY()));

			p.Data = player;

			return p;
		}
		else if(original.Command == PlayerCommandType.Update)
		{
			p.Command = original.Command;

			World orig = (World)original.Data;

			int w = orig.getGridWidth();
			int h = orig.getGridHeight();

			GridObject[][] gridCopy = new GridObject[w][h];

			for (int i=0; i<w; i++)
			{
				for (int j=0; j<h; j++)
				{
					gridCopy[i][j] = orig.getElementAt(j, i);
				}
			}

			World wCopy = new World(gridCopy);			

			B_Player[] players = (B_Player[])original.MetaData;

			B_Player copyPlayers[] = new B_Player[players.length];
			ArrayList<B_Player> copyPlayersList = new ArrayList<B_Player>();
			for (int i=0; i<copyPlayers.length; i++)
			{
				B_Player tmp = players[i].getCopy();
				copyPlayersList.add(tmp);
				copyPlayers[i] = tmp;
			}

			ArrayList<Bomb> copyBombs = new ArrayList<Bomb>();
			for (Bomb b: orig.getBombs())
				copyBombs.add(new Bomb(b.getName(), b.getLocation(), b.getPower(), b.getTime()));

			wCopy.setPlayers(copyPlayersList);

			wCopy.setBombs(copyBombs);

			p.Data = wCopy;

			p.MetaData = copyPlayers;

		}

		return p;
	}
}