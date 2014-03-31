package BombermanGame;

public class Bomb extends Entity
{
	protected float timer;
	
	protected int power;
	
	//if the server confirmed this bomb
	protected boolean confirmed = false;
	
	public float getTime() { return this.timer; }
	
	public int getRange() { return this.power; }
	
	public void setTimer(float time) {this.timer = time;}
	
	public boolean isConfirmed() {return this.confirmed;}
	
	public void confirm() {this.confirmed = true;}
	
	public boolean isDetonated()
	{
		return this.timer <= 0;
	}
	
	public int getPower() {return this.power;}
	
	public void setRange(int r)
	{
		this.power = r;
	}
	
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
	
	public Bomb getCopy()
	{
		return new Bomb(this.name, this.location, this.power, this.timer);
	}
}
