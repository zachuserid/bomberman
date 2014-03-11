package BombermanGame;


import Networking.ClientNetworkHandler;
import Networking.DoubleBuffer;
import java.util.ArrayList;

public class BombermanClientNetworkHandler extends ClientNetworkHandler<PlayerCommand[], BomberPacket>{

	//Members
	
	int grid_width;
	int grid_height;
	
	DoubleBuffer<PlayerCommand> commandBacklog;
	
	//Constructor
	
	public BombermanClientNetworkHandler(String ip, int port)
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
		
		joinCom[0] = new PlayerCommand(PlayerCommandType.Join, 0, 0);
		
		this.Send(joinCom);
	}
    
	/*
	 * Note: The first two bytes sent to us
	 * for each grid update will represent the
	 * width and height of the grid, respectively.
	 * As we are bounding the grid to 127 x 127,
	 * each value can be represented as one byte.
	 */
	@Override
	protected BomberPacket[] parseReceive(byte[] data) 
	{	
		BomberPacket p = new BomberPacket();
		
		/*
		 * PlayerCommandType[5] = Join
		 * p.Command = PlayerCommandType.Join
		 * p.Data = (BombermanPlayer):
		 * 		the player that is joining the game
		 */
		if((int)data[0] == 5)
		{
			String s = new String(data);
			s = s.substring(1);
			String[] ss = s.split(",");
			s = ss[0];
			char c = ss[1].charAt(0);
			int x = (int)ss[2].getBytes()[0];
			int y = (int)ss[3].getBytes()[0];
			BombermanPlayer player = new BombermanPlayer(s, new Point(x, y), c);
			p.Data = player;
			p.Command = PlayerCommandType.Join;
		}
		/*
		 * PlayerCommandType[7] = Ack
		 * p.Command = PlayerCommandType.Ack
		 * p.Data = (int): 
		 * 		highest acknowledged update request by server.
		 */
		else if ((int)data[0] == 7)
		{
			byte bytesToInt[] = {data[1], data[2], data[3], data[4]};
			int highAck = java.nio.ByteBuffer.wrap(bytesToInt).getInt();
			System.out.println("~~~Client received ack count: " + highAck);
			
			//Received the ack, remove all from the backlog that
			// are <= highAck.
			ArrayList<PlayerCommand> backlog = this.commandBacklog.readAll(true);
			
			ArrayList<PlayerCommand> newBacklog = new ArrayList<PlayerCommand>();
			for (PlayerCommand pc: backlog)
			{
				if (pc.Id > highAck)
					newBacklog.add(pc);
				else
					System.out.println("~~~~Removing acked packet: " + pc.Id);
			}
			PlayerCommand newBacklogArray[] = new PlayerCommand[newBacklog.size()];
			newBacklog.toArray(newBacklogArray);
			
			this.commandBacklog.writeAll(newBacklogArray, false);
			
			//Already handled the Ack, don't buffer this Packet
			p = null;
		}
		/*
		 * Dealing with a world update
		 * p.Command = PlayerCommandType.Update
		 * p.Data = (char[][]):
		 * 		the character map representing the most recent world
		 */
		else
		{
			//get the first two bytes out of the data
			int w = data[1];
			int h = data[2];
			
			if ( this.grid_width != w || this.grid_height != h )
			{
				System.out.println("Changing grid dimensions to ["+w+"]["+h+"]");
				this.grid_width = w;
				this.grid_height = h;
			}
			
			char[][] gridArr = new char[this.grid_width][this.grid_height];
			
			for (int i=0; i<this.grid_width; i++)
			{
				for (int j=0; j<this.grid_height; j++)
				{
					gridArr[i][j] = (char)data[ ( (i * this.grid_width) + j) + 3 ];
				}
			}
			
			p.Command = PlayerCommandType.Update;
			p.Data = gridArr;
		}
		
		return new BomberPacket[] {p};
	}


	@Override
	protected byte[] parseSend(PlayerCommand[] commands) 
	{
		//Format ':<time>,<id>,<PlayerCommand>'
		
		//TODO: Get the non-acked commands and append them
		// before resending everything to date.
		ArrayList<PlayerCommand> backlog = this.commandBacklog.readAll(true);
		
		//To store the new and backlogged commands
		PlayerCommand allCommands[] = new PlayerCommand[commands.length + backlog.size()];
		System.out.println("commands.length: " + commands.length + 
				". backlog.size: " + backlog.size() + 
				". all.length: " + allCommands.length);
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
		
		boolean needTrim = false;
		
		for (int i=0; i<allCommands.length; i++)
		{
			if ( allCommands[i].Command == PlayerCommandType.Join )
			{
				System.out.println("Sending join game message");
				//Not sending a name, server will take care of that
				toSend += "|||join,,|||";
				continue;
			}
			
			toSend += allCommands[i].Time;
			toSend += "," + allCommands[i].Id;
			toSend += "," + allCommands[i].Command + ",";
			needTrim = true;
		}
		
		//remove trailing ','
		if (needTrim)
			toSend = toSend.substring(0, toSend.length()-1);
		
		//Add these commands to the backlog to resend unless 
		// we receive an ack for them.
		this.commandBacklog.writeAll(commands, false);
		
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
	protected BomberPacket getReceiveCopy(BomberPacket original)
	{
		if(original.Command == PlayerCommandType.Join)
		{
			BomberPacket p = new BomberPacket();
			p.Command = PlayerCommandType.Join;
			
			BombermanPlayer or = (BombermanPlayer)original.Data;
			
			BombermanPlayer player = new BombermanPlayer(or.getName(), new Point(or.getX(), or.getY()), or.getCharacter());
			
			p.Data = player;
			
			return p;
		}
		else
		{
			BomberPacket p = new BomberPacket();
			p.Command = original.Command;
			
			char[][] or = (char[][])original.Data;
			
			int w = or.length;
			int h = or[0].length;
			char[][] gridCopy = new char[w][h];
			for (int i=0; i<w; i++)
			{
				for (int j=0; j<h; j++)
				{
					gridCopy[i][j] = or[i][j];
				}
			}
			
			p.Data = gridCopy;
			
			return p;
		}
	}
}
