package BombermanGame;

import java.util.ArrayList;

import Networking.ClientNetworkHandler;

public class BombermanClientNetworkHandler extends ClientNetworkHandler<PlayerCommand[], char[][]>{

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
		ArrayList<PlayerCommand[]> toSend = new ArrayList<PlayerCommand[]>();
		
		PlayerCommand joinCom[] = new PlayerCommand[1];
		
		joinCom[0] = new PlayerCommand(PlayerCommandType.Join, 0, 0);
		
		toSend.add( joinCom );
		
		this.sendData(toSend);
	}
    
	/*
	 * **Note: The first two bytes sent to us
	 * for each grid update will represent the
	 * width and height of the grid, respectively.
	 * As we are bounding the grid to 127 x 127,
	 * each value can be represented as one byte.
	 */
	@Override
	protected char[][][] parseReceive(byte[] data) 
	{
		//get the first two bytes out of the data
		int w = data[0];
		int h = data[1];
		
		if ( this.grid_width != w || this.grid_height != h )
		{
			System.out.println("Changing grid dimensions to ["+w+"]["+h+"]");
			this.grid_width = w;
			this.grid_height = h;
		}
		
		//we only require one world grid update, so we will ignore the
		// array aspect and only populate 1 char[][]
		
		char[][][] gridArr = new char[1][this.grid_width][this.grid_height];
		
		for (int i=0; i<this.grid_width; i++)
		{
			for (int j=0; j<this.grid_height; j++)
			{
				gridArr[0][i][j] = (char)data[ ( (i * this.grid_width) + j) + 2 ];
			}
		}
		
		return gridArr;
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
	protected char[][] getReceiveCopy(char[][] original)
	{
		int w = original.length;
		int h = original[0].length;
		char[][] gridCopy = new char[w][h];
		for (int i=0; i<w; i++)
		{
			for (int j=0; j<h; j++)
			{
				gridCopy[i][j] = original[i][j];
			}
		}
		
		return gridCopy;
	}
	

}
