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
	
	public U_WorldData getCopy()
	{
		int w = this.grid.length;
		int h = this.grid[0].length;

		GridObject[][] gridCopy = new GridObject[w][h];

		for (int i=0; i<w; i++)
		{
			for (int j=0; j<h; j++)
			{
				gridCopy[i][j] = this.grid[i][j];
			}
		}
		
		return new U_WorldData(gridCopy);
	}
}
