package BombermanNetworkGame;

import BombermanGame.GridObject;
import BombermanGame.Point;
import BombermanGame.Powerup;

public class U_ClientData {
	
	/*
	 *  Wrapper class used to store objects with relevent
	 *  client side data requirements regarding the world
	 *  and the player and bomb lists in the world.
	 *  
	 *  It will be constructed by parseReceive in the client
	 *  network handler and given to the client, who will then
	 *  use it to setUpdateData() in his representation of
	 *  the world.
	 */
	
	public U_WorldData world;
	public U_PlayerData players[];
	public U_BombData bombs[];
	
	public U_ClientData(U_WorldData world, U_PlayerData players[], U_BombData bombs[])
	{
		this.world = world;
		this.players = players;
		this.bombs = bombs;
	}
	
	/* not sure why this was necessary....
	//World data getter methods
	
	public GridObject[][] getGrid()
	{
		return world.grid;
	}
	
	//Player data getter methods at provided index
	
	public int numPlayers()
	{
		return this.players.length;
	}
	
	public int getPlayerNumber(int index)
	{
		return this.players[index].playerNumber;
	}
	
	public String getPlayerName(int index)
	{
		return this.players[index].name;
	}
	
	public int getPlayerX(int index)
	{
		return this.players[index].position.X; 
	}
	
	public int getPlayerY(int index)
	{
		return this.players[index].position.Y;
	}
	
	public Point getPlayerPosition(int index)
	{
		return this.players[index].position;
	}
	
	public Powerup getPlayerPowerup(int index)
	{
		return this.players[index].powerup;
	}
	
	public boolean isPlayerAlive(int index)
	{
		return this.players[index].isAlive;
	}
	
	public int getPlayerBombCount(int index)
	{
		return this.players[index].numBombs;
	}
	
	public int getPlayerKills(int index)
	{
		return this.players[index].kills;
	}
	
	//Bomb data getter methods for provided index
	
	public int numBombs()
	{
		return this.bombs.length;
	}
	
	public int getBombX(int index)
	{
		return this.bombs[index].position.X;
	}
	
	public int getBombY(int index)
	{
		return this.bombs[index].position.Y;
	}
	
	public Point getBombPosition(int index)
	{
		return this.bombs[index].position;
	}
	
	public int getBombRange(int index)
	{
		return this.bombs[index].radius;
	}
	
	public float getBombTime(int index)
	{
		return this.bombs[index].radius;
	}*/
	
	//copy factory method
	public U_ClientData getCopy()
	{
		U_WorldData wCopy = this.world.getCopy();
		
		U_PlayerData pCopy[] = new U_PlayerData[this.players.length];
		for (int i=0; i<pCopy.length; i++)
			pCopy[i] = this.players[i].getCopy();
		
		U_BombData bCopy[] = new U_BombData[this.bombs.length];
		for (int i=0; i<bCopy.length; i++)
			bCopy[i] = this.bombs[i].getCopy();
		
		return new U_ClientData(wCopy, pCopy, bCopy);
	}

}
