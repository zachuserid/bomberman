package BombermanNetworkGame;

import BombermanGame.Bomb;
import BombermanGame.Point;

public class U_BombData {
	
	public Point position;
	public int radius;
	public String bName;
	public float time;

	public U_BombData(String name, Point p, int rad, float t)
	{
		this.bName = name;
		this.position = p;
		this.radius = rad;
		this.time = t;
	}
	
	public U_BombData(Bomb b)
	{
		this.position = b.getLocation();
		this.radius = b.getRange();
		this.bName = b.getName();
		this.time = b.getTime();
	}
	
	public U_BombData getCopy()
	{
		return new U_BombData(this.bName, this.position, this.radius, this.time);
	}
}
