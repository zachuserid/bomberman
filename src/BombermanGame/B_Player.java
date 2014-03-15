package BombermanGame;

/*
 * this class will eventually have a bunch of data associated with
 * a bomberman player (kills, powerups etc...)
 */
public class B_Player extends Entity
{
	//members
	
	private Powerup powerup;
	
	private int killCount;
	
	//constructor
	
	public B_Player(String name, Point location, char character)
	{
		super(name, location, character);
		killCount = 0;
		powerup = Powerup.None;
	}
	
	//getters/settets
	
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
	
	//methods
	
	public void Update(float elapsed, World world)
	{
		//TODO: however the PlayerCommands modifications are
		// being processed on this object, ensure the kills and powerup
		// are updated accordingly.
	}
	
	public B_Player getCopy()
	{
		return new B_Player(this.getName(), this.getLocation(), this.getCharacter());
	}
}
