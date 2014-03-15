package BombermanGame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

//this class controls the player(s) that are over a network
public class B_NetworkPlayerController extends B_Controller
{
	protected Hashtable<String,PlayerMovePair> players;
	
	protected World world;
	
	public B_NetworkPlayerController(World w)
	{
		this.players = new Hashtable<String, PlayerMovePair>();
		
		this.world = w;
	}
	
	
	public void AddPlayer(B_Player player)
	{
		this.players.put(player.name, new PlayerMovePair(player));
	}
	
	public void AddCommands(String name, ArrayList<PlayerCommand> commands)
	{
		PlayerMovePair pair = this.players.get(name);
		if (pair != null)
			pair.Commands.addAll(commands);
	}
	
	
	@Override
	public void Update(float time)
	{
		Collection<PlayerMovePair> pairs = this.players.values();
		
		for(PlayerMovePair p : pairs)
		{
			for(PlayerCommand c : p.Commands)
			{
				if(c.Command == PlayerCommandType.MoveDown) this.world.TryMoveDown(p.Player);
				else if (c.Command == PlayerCommandType.MoveLeft) this.world.TryMoveLeft(p.Player);
				else if (c.Command == PlayerCommandType.MoveRight) this.world.TryMoveRight(p.Player);
				else if (c.Command == PlayerCommandType.MoveUp) this.world.TryMoveUp(p.Player);
			}
		}
			
	}
}

class PlayerMovePair
{
	public B_Player Player;
	
	public ArrayList<PlayerCommand> Commands;
	
	public PlayerMovePair(B_Player player)
	{
		this.Player = player;
		this.Commands = new ArrayList<PlayerCommand>();
	}
}