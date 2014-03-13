package BombermanGame;

/*
 * this class will eventually have a bunch of data associated with
 * a bomberman player (kills, powerups etc...)
 */
public class BombermanPlayer extends Entity
{
	//members
	
	private Powerup powerup;
	
	private int killCount;
	
	//constructor
	
	public BombermanPlayer(String name, Point location, char character)
	{
		super(name, location, character);
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
	
	public BombermanPlayer getCopy()
	{
		return new BombermanPlayer(this.getName(), this.getLocation(), this.getCharacter());
	}
}
