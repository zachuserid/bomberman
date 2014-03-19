package BombermanNetworkGame;

import BombermanGame.GridObject;
import BombermanGame.Bomb;
import BombermanGame.B_Player;

public class ClientUpdate {
	
	//TODO: Create objects for the data required for each of these objects..
	
	private GridObject grid[][];
	private B_Player players[];
	private Bomb bombs[];
	
	public ClientUpdate(GridObject[][] grid, B_Player[] players, Bomb[] bombs)
	{
		this.grid = grid;
		this.players = players;
		this.bombs = bombs;
	}
	
	public GridObject[][] getGrid()
	{
		return this.grid;
	}
	
	public B_Player[] getPlayers()
	{
		return this.players;
	}
	
	public Bomb[] getBombs()
	{
		return this.bombs;
	}
}
