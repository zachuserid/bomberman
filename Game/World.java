import java.util.ArrayList;

public class World
{
	protected char[][] grid;
	
	protected ArrayList<Entity> entities;
	
	
	public int getGridWidth() {return grid.length;}
	public int getGridHeight() {return grid[0].length;}
	
	public ArrayList<Entity> getEntities() {return this.entities;}
	
	
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
	
	public void Update(float time)
	{
		
	}
	
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
}