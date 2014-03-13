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
	
	public B_NetworkPacket getCopy()
	{
		BombermanPlayer copyPlayers[] = new BombermanPlayer[this.players.length];
		for (int i=0; i<copyPlayers.length; i++)
			copyPlayers[i] = this.players[i].getCopy();
		
		World copyWorld = this.world.getCopy();
		
		return new B_NetworkPacket(copyWorld, copyPlayers);
	}
	
}
