package BombermanGame;

public class Bomb extends Entity
{
	protected float timer;
	
	protected int power;
	
	public float getTime() {return timer;}
	
	public boolean isDetonated()
	{
		return this.timer <= 0;
	}
	
	public int getPower() {return this.power;}
	
	public Bomb(String playerName, Point location, int power, float time)
	{
		super(playerName, location, GridObject.Bomb);
		
		this.timer = time;
		this.power = power;
	}
	
	@Override
	public void Update(float elapsed, World w)
	{
		this.timer -= elapsed;
		
	}
}
