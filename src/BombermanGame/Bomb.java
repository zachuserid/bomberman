package BombermanGame;

public class Bomb extends Entity
{
	protected float timer;
	
	protected int power;
	
	protected int range;
	
	public float getTime() { return this.timer; }
	
	public int getRange() { return this.range; }
	
	public boolean isDetonated()
	{
		return this.timer <= 0;
	}
	
	public int getPower() {return this.power;}
	
	public void setRange(int r)
	{
		this.range = r;
	}
	
	public Bomb(String playerName, Point location, int power, float time)
	{
		super(playerName, location, GridObject.Bomb);
		
		this.timer = time;
		this.power = power;
		 //TODO: Pull this value from the player when created...
		this.range = 2;
	}
	
	@Override
	public void Update(float elapsed, World w)
	{
		this.timer -= elapsed;
		
	}
}
