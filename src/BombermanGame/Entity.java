package BombermanGame;

/*
 * this is an entity in the world
 * an entity has dynamic position and a name
 */
public abstract class Entity
{
	protected String name;

	protected Point location;
	
	protected GridObject gridObject;

	//Properties
	public String getName(){return this.name;}

	public int getX() {return this.location.X;}
	public void setX(int x) {this.location.X = x;}

	public int getY() {return this.location.Y;}
	public void setY(int y) {this.location.Y = y;}

	public Point getLocation() {return this.location;}
	public void setLocation(Point l) {this.location = l;}
	
	public GridObject getGridObject(){return this.gridObject;}

	public Entity(String name, Point location, GridObject gObject)
	{
		this.name = name;
		this.location = location;
		this.gridObject = gObject;
	}

	public abstract void Update(float elapsed, World w);
}