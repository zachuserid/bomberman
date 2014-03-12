package BombermanGame;

public class B_NetworkPacket {
	
	//members
	
	private World world;
	
	private BombermanPlayer players[];
	
	public B_NetworkPacket(World w, BombermanPlayer[] players)
	{
		this.world = w;
		this.players = players;
	}

	public World getWorld()
	{
		return this.world;
	}
	
	public BombermanPlayer[] getPlayers()
	{
		return this.players;
	}
	
}
