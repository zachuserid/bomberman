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
	
	//constructor
	
	public B_Player(String name, Point location)
	{
		super(name, location, GridObject.Player);
		killCount = 0;
		powerup = Powerup.None;
		this.alive = true;
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
	
	//methods
	
	public void Update(float elapsed, World world)
	{
		//TODO: however the PlayerCommands modifications are
		// being processed on this object, ensure the kills and powerup
		// are updated accordingly.
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
