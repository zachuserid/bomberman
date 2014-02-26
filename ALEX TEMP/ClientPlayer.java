
public class ClientPlayer extends Entity
{
	protected BombermanPlayerController controller;
	
	public ClientPlayer(String name, Point location, BombermanPlayerController controller)
	{
		super(name, location);
		
		this.controller = controller;
	}

	public ClientPlayer(String name, Point location)
	{
		this(name, location, BombermanPlayerController.Default());
	}
	
	public void Update(float elapsed, World world)
	{
		if(this.controller.MoveLeft()) world.TryMoveLeft(this);
		if(this.controller.MoveRight()) world.TryMoveRight(this);
		if(this.controller.MoveUp()) world.TryMoveUp(this);
		if(this.controller.MoveDown()) world.TryMoveDown(this);
		if(this.controller.PlantBomb()) world.TryPlantBomb(this);
	}
}
