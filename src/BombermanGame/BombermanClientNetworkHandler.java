package BombermanGame;


import Networking.ClientNetworkHandler;

public class BombermanClientNetworkHandler extends ClientNetworkHandler<PlayerCommand[], BomberPacket>{

	//Members
	
	int grid_width;
	int grid_height;
	
	//Constructor
	
	public BombermanClientNetworkHandler(String ip, int port)
	{
		super(ip, port);
		//Initialize dimensions as "unknown" until we receive some packets
		grid_width = -1;
		grid_height = -1;
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
	 * **Note: The first two bytes sent to us
	 * for each grid update will represent the
	 * width and height of the grid, respectively.
	 * As we are bounding the grid to 127 x 127,
	 * each value can be represented as one byte.
	 */
	@Override
	protected BomberPacket[] parseReceive(byte[] data) 
	{
		//we only require one world grid update, so we will ignore the
		// array aspect and only populate 1 char[][]
		
		BomberPacket p = new BomberPacket();
		
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
					gridArr[i][j] = (char)data[ ( (i * this.grid_width) + j) + 2 ];
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
		//:<time>,<id>,<PlayerCommand>
		String toSend = ":"; //Start of player request
		
		boolean needTrim = false;
		
		for (int i=0; i<commands.length; i++)
		{
			if ( commands[i].Command == PlayerCommandType.Join )
			{
				System.out.println("Sending join game message");
				//Not sending a name, server will take care of that
				toSend += "|||join,,|||";
				continue;
			}
			
			toSend += commands[i].Time;
			toSend += "," + commands[i].Id;
			toSend += "," + commands[i].Command + ",";
			needTrim = true;
		}
		
		//remove trailing ','
		if (needTrim)
			toSend = toSend.substring(0, toSend.length()-1);
		
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
