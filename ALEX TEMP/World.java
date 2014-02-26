import java.util.ArrayList;

public class World
{
	protected char[][] grid;
	
	protected ArrayList<Entity> entities;
	
	
	public int getGridWidth() {return grid.length;}
	public int getGridHeight() {return grid[0].length;}
	
	public World(char[][] grid)
	{
		this.grid = grid;
		
		this.entities = new ArrayList<Entity>();
	}
	
	
	public boolean AddEntity(Entity e)
	{
		this.entities.add(e);
		return true;
	}
	
	public void Update(float elapsed)
	{
		for(Entity e : this.entities)
			e.Update(elapsed, this);
	}
	
	public boolean TryMoveLeft(Entity e)
	{
		System.out.println(e.name + " move left.");
		
		return false;
	}
	
	public boolean TryMoveRight(Entity e)
	{
		System.out.println(e.name + " move right.");
		
		return false;
	}
	
	public boolean TryMoveUp(Entity e)
	{
		System.out.println(e.name + " move up.");
		
		return false;
	}
	
	public boolean TryMoveDown(Entity e)
	{
		System.out.println(e.name + " move down.");
		
		return false;
	}
	
	public boolean TryPlantBomb(Entity e)
	{
		System.out.println(e.name + " plants bomb.");
		
		return false;
	}
}