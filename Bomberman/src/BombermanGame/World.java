package BombermanGame;

import java.util.ArrayList;
import Network.Sendable;

/*
 * the world is the model for the bomberman game
 * requests to alter the model are done via associated methods that return WorldActionOutcome
 * a world also has a list of entities
 */
public class World implements Sendable<World>
{
	//this is the backing character grid
	protected char[][] grid;

	//list of entities in the world
	protected ArrayList<Entity> entities;

	//the width of the world in "squares" (not pixels)
	public int getGridWidth() {return grid.length;}
	public int getGridHeight() {return grid[0].length;}
	
	//returns the (currently)char elements at x,y
	public char getElementAt(int x, int y)
	{
		return this.grid[x][y];
	}

	public ArrayList<Entity> getEntities() {return this.entities;}


	//creates a new world with a grid
	public World(char[][] grid)
	{
		this.grid = grid;

		this.entities = new ArrayList<Entity>();
	}

	//adds an entity to world (should be mapped to an outside controller to be dynamic)
	public boolean AddEntity(Entity e)
	{
		this.entities.add(e);
		return true;
	}

	//currently unused, may be needed for "static" object updates (animations or something)
	public void Update(float time)
	{

	}

	//these are all the requests for an entity to try and do something
	public WorldActionOutcome TryMoveLeft(Entity e)
	{
		int x = e.getX() - 1;
		if(x >= 0)
		{
			e.setX(x);
			return WorldActionOutcome.Approved;
		}

		return WorldActionOutcome.DeniedStatic;
	}

	public WorldActionOutcome TryMoveRight(Entity e)
	{
		int x = e.getX() + 1;
		if(x < this.getGridWidth())
		{
			e.setX(x);
			return WorldActionOutcome.Approved;
		}

		return WorldActionOutcome.DeniedStatic;
	}

	public WorldActionOutcome TryMoveUp(Entity e)
	{
		int y = e.getY() - 1;
		if(y >= 0)
		{
			e.setY(y);
			return WorldActionOutcome.Approved;
		}

		return WorldActionOutcome.DeniedStatic;
	}

	public WorldActionOutcome TryMoveDown(Entity e)
	{
		int y = e.getY() + 1;
		if(y < this.getGridHeight())
		{
			e.setY(y);
			return WorldActionOutcome.Approved;
		}

		return WorldActionOutcome.DeniedStatic;
	}

	public WorldActionOutcome TryPlantBomb(Entity e)
	{
		System.out.println(e.name + " plants bomb.");

		return WorldActionOutcome.Approved;
	}
	
	@Override
	public byte[] getBytes()
	{
		return new String("WORLD STRING").getBytes();
	}
	@Override
	public World getCopy()
	{
		//add actual copy
		
		return new World(this.grid);
	}
}