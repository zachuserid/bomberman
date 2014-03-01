package BombermanGame;

import java.net.InetAddress;

import Networking.ClientNetworkHandler;

import java.io.BufferedReader;
import java.io.FileReader;

public class BombermanClientNetworkHandler extends ClientNetworkHandler<PlayerCommand[], char[][]>{

	//Members
	
	int grid_dimension;
	
	//Constructor
	
	public BombermanClientNetworkHandler(InetAddress ip, int port, String filePath)
	{
		super(ip, port);
		
		BufferedReader br;
		try 
	    {
			//opens the reader with the path
			br = new BufferedReader(new FileReader(filePath));
			
			//reads in the first line of the file
	        grid_dimension = Integer.parseInt( br.readLine() );

	    } catch (Exception e)
	    {
	    	System.out.println("Client could not read dimensions from file: '"+filePath+"'");
	    }
	}


	//Methods
    
	@Override
	protected char[][][] parseReceive(byte[] data) 
	{
		//we only require one world grid update, so we will ignore the
		// array aspect and only populate 1 char[][]
		char[][][] gridArr = new char[1][this.grid_dimension][this.grid_dimension];
		
		for (int i=0; i<this.grid_dimension; i++)
		{
			for (int j=0; j<this.grid_dimension; j++)
			{
				gridArr[0][i][j] = (char)data[ (i * this.grid_dimension) + j ];
			}
		}
		
		return gridArr;
	}



	@Override
	protected byte[] parseSend(PlayerCommand[] commands) 
	{
		//:<time>,<id>,<PlayerCommand>
		String toSend = ":"; //Start of player request
		for (int i=0; i<commands.length; i++)
		{
			toSend += commands[i].Time;
			toSend += "," + commands[i].Id;
			toSend += "," + commands[i].Command + ",";
		}
		
		//remove trailing ','
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
