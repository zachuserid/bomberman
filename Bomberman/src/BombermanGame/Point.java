package BombermanGame;

public class Point
{
	public static Point Zero() {return new Point(0);}

	public int X;
	public int Y;

	public Point(int xy)
	{
		this.X = xy;
		this.Y = xy;
	}

	public Point(int x, int y)
	{
		this.X = x;
		this.Y = y;
	}
}