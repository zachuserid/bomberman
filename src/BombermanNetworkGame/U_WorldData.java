package BombermanNetworkGame;

import BombermanGame.GridObject;

public class U_WorldData {
	
	/*
	 * Dumb wrapper for world data required by the
	 * clients to be filled out after receiving data from
	 * the server. Though trivial now, it may contain
	 * more information in the future.
	 */

	public GridObject grid[][];
	
	public U_WorldData(GridObject[][] grid)
	{
		this.grid = grid;
	}
}
