package BombermanGame;

import Networking.Sendable;

//this is a command from a player to the server
public class PlayerCommand implements Sendable<PlayerCommand>
{
	public String PlayerName;

	public PlayerCommandType Command;

	public int Id;

	public PlayerCommand(PlayerCommandType command, int id)
	{
		this.Command = command;
		this.Id = id;
		
		//name is initially empty to save space in packet
		//the server will populate this with the packet's user name
		this.PlayerName = "";
	}
	
	public PlayerCommand(String name, PlayerCommandType command, int id)
	{
		this.Command = command;
		this.Id = id;
		this.PlayerName = name;
	}

	@Override
	public byte[] getBytes()
	{
		String byteStr = "";
		byteStr += this.Id + "," + this.Command;
		return byteStr.getBytes();
	}

	@Override
	public PlayerCommand getCopy()
	{
		return new PlayerCommand(this.PlayerName, this.Command, this.Id);
	}
	
	@Override
	public String toString()
	{
		return "Player " + this.PlayerName + " request " + this.Id + 
				" for move " + this.Command;
	}
}
