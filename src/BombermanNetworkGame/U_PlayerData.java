package BombermanNetworkGame;

import BombermanGame.B_Player;
import BombermanGame.Point;
import BombermanGame.Powerup;
import BombermanGame.PlayerName;

public class U_PlayerData {
	
	/*
	 * A wrappre for some but not all of a
	 * player object's data members, used by the
	 * networking to pass relevant information from
	 * server to clients.
	 */
	
	public Point position;
	public Powerup powerup;
	public boolean isAlive;
	public int playerNumber;
	public String name;
	
	public U_PlayerData(Point ps, Powerup pu, boolean alive, int pNum)
	{
		this.position = ps;
		this.powerup = pu;
		this.isAlive = alive;
		this.playerNumber = pNum;
		this.name = PlayerName.values()[pNum].toString();
	}
	
	public U_PlayerData(B_Player player)
	{
		this.position = player.getLocation();
		this.powerup = player.getPowerup();
		this.isAlive = player.isAlive();
		this.name = player.getName();
		this.playerNumber = PlayerName.valueOf(this.name).ordinal();
	}
		
	
}
