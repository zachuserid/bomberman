package BombermanGame;

/*
 * this class will eventually have a bunch of data associated with
 * a bomberman player (kills, powerups etc...)
 */
public class BombermanPlayer extends Entity
{
	public BombermanPlayer(String name, Point location, char character)
	{
		super(name, location, character);
	}
	
	public void Update(float elapsed, World world)
	{
	}
}
