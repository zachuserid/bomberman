package BombermanNetworkGame;

import BombermanGame.B_Player;
import BombermanGame.World;

public class B_NetworkPacket {
	
	//members
	
	private World world;
	
	private B_Player players[];
	
	public B_NetworkPacket(World w, B_Player[] players)
	{
		this.world = w;
		this.players = players;
	}

	public World getWorld()
	{
		return this.world;
	}
	
	public B_Player[] getPlayers()
	{
		return this.players;
	}
	
	public B_NetworkPacket getCopy()
	{
		B_Player copyPlayers[] = new B_Player[this.players.length];
		for (int i=0; i<copyPlayers.length; i++)
			copyPlayers[i] = this.players[i].getCopy();
		
		World copyWorld = this.world.getCopy();
		
		return new B_NetworkPacket(copyWorld, copyPlayers);
	}
	
}
