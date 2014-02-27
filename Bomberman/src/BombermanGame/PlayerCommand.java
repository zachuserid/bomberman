package BombermanGame;

import Network.Sendable;

public class PlayerCommand implements Sendable<PlayerCommand>
{
	public String PlayerName;

	public float Time;

	public PlayerCommandType Command;

	public int Id;

	public PlayerCommand(PlayerCommandType command, float time, int id)
	{
		this.Command = command;
		this.Time = time;
		this.Id = id;

		this.PlayerName = "";
	}
	
	public PlayerCommand getCopy()
	{
		//TODO: Implement this
		return null;
	}
	
	public byte[] getBytes()
	{
		//TODO: implement this
		return null;
	}
}
