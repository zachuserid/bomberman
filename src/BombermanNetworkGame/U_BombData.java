package BombermanNetworkGame;

import BombermanGame.Bomb;
import BombermanGame.Point;

public class U_BombData {
	
	public Point position;
	public int radius;

	public U_BombData(Point p, int rad)
	{
		this.position = p;
		this.radius = rad;
	}
	
	public U_BombData(Bomb b)
	{
		this.position = b.getLocation();
		this.radius = b.getRange();
	}
}
