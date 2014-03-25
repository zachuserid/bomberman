package BombermanGame;

/*
 * this class will eventually have a bunch of data associated with
 * a bomberman player (kills, powerups etc...)
 */
public class B_Player extends Entity
{
	//members
	
	protected Powerup powerup;
	
	protected int killCount;
	
	protected boolean alive;
	
	protected int bombCount;
	
	protected int bombRange;
	
	//constructor
	
	public B_Player(String name, Point location)
	{
		super(name, location, GridObject.Player);
		this.killCount = 0;
		this.powerup = Powerup.None;
		this.alive = true;
		this.bombCount = 15; //default
		this.bombRange = 2; //default
	}
	
	//getters/setters
	
	public void setPowerup(Powerup p)
	{
		this.powerup = p;
	}
	
	public void setKillCount(int k)
	{
		this.killCount = k;
	}
	
	public void setBombCount(int count)
	{
		this.bombCount = count;
	}
	
	public void setBombRange(int range)
	{
		this.bombRange = range;
	}
	
	public Powerup getPowerup()
	{
		return this.powerup;
	}
	
	public int getKillCount()
	{
		return this.killCount;
	}
	
	public boolean isAlive() {return this.alive;}
	
	public void Kill() {this.alive = false;}
	
	public void Revive() {this.alive = true;}
	
	public int getBombCount()
	{
		return this.bombCount;
	}
	
	public int getBombRange()
	{
		return this.bombRange;
	}
	
	//methods
	
	public void Update(float elapsed, World world)
	{
		
	}
	
	public B_Player getCopy()
	{
		B_Player p = new B_Player(this.getName(), this.getLocation());
		p.setKillCount(this.killCount);
		p.setPowerup(this.powerup);
		if (!this.isAlive()) p.Kill();
		return p;
	}
}
