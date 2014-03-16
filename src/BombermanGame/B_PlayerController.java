package BombermanGame;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/*
 * this class handles input from the keyboard and tries to make actions in the world.
 * it hooks to the dispatch key event to get key states
 * on update these key states are compared to determine if the player is trying to do something
 * if an action is performed, it is added to an action log for this controller
 */
public class B_PlayerController extends B_Controller implements KeyEventDispatcher
{
	//creates a default player controller with WASD keys
	public static B_PlayerController Default(B_Player player, World w) {return new B_PlayerController(player, w, 'a', 'd', 'w', 's', ' ');}
	
	//the amount of controls (this is to make adding/removing them easier)
	protected int controlCount;
	
	//the arrays representing the state of a key being pressed and released
	protected boolean[] boolsDown;
	protected boolean[] boolsUp;
	
	//the chars to map to input
	protected char[] cKeys;
	
	//the player entity mapped to this control
	protected B_Player player;
	
	//the world mapped to this control
	protected World world;
	
	//the log of commands that were approved by the world
	protected ArrayList<PlayerCommand> commands;
	
	//the current id that would be given to a new command
	protected int commandIds;
	
	
	public PlayerCommand[] getCommandsClear()
	{
		PlayerCommand[] com = new PlayerCommand[this.commands.size()];
		
		this.commands.toArray(com);
		
		return com;
	}
	
	
	public B_PlayerController(B_Player player, World w, char left, char right, char up, char down, char bomb)
	{
		this.player = player;
		this.world = w;
		this.commands = new ArrayList<PlayerCommand>();
		this.commandIds = 0;
		
		//adds this object as a listener to the key dispatch event
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
		
		this.controlCount = 5;
		
		this.boolsDown = new boolean[this.controlCount];
		
		//creates the up array and sets it as starting all true
		this.boolsUp = new boolean[this.controlCount];
		for(int i = 0; i < this.controlCount; i++)
			this.boolsUp[i] = true;
		
		//the keys mapped to a control
		this.cKeys = new char[this.controlCount];
		this.cKeys[0] = left;
		this.cKeys[1] = right;
		this.cKeys[2] = up;
		this.cKeys[3] = down;
		this.cKeys[4] = bomb;
	}
	
	//this is what happens every time a dispatch key even occurs
	@Override
	public boolean dispatchKeyEvent(KeyEvent e)
	{
		//gets the character of the event
		char c = e.getKeyChar();
		
		//checks it against every control
		for(int i = 0; i < this.controlCount; i++)
		{
			if(c == this.cKeys[i])
			{
				//if its pressed and was released, this is a valid keydown input
				if(e.getID() == KeyEvent.KEY_PRESSED)
				{
					if(this.boolsUp[i])
					{
						this.boolsUp[i] = false;
						this.boolsDown[i] = true;
					}
				}
				else if(e.getID() == KeyEvent.KEY_RELEASED)
					this.boolsUp[i] = true;
				
				i = this.controlCount;
			}
		}
		
		return false;
	}

	@Override
	public void Update(float time)
	{
		if(!this.player.isAlive()) return;
		
		//checks the states of the commands and updates if necessary
		//if an an action is valid (according to the world) it is added to the list of commands	
		if(this.CheckBool(0))
		{
			if(this.world.TryMoveLeft(this.player) == WorldActionOutcome.Approved)
			{
				this.commands.add(new PlayerCommand(PlayerCommandType.MoveLeft, this.commandIds++));
			}
		}
		if(this.CheckBool(1))
		{
			if(this.world.TryMoveRight(this.player) == WorldActionOutcome.Approved)
			{
				this.commands.add(new PlayerCommand(PlayerCommandType.MoveRight, this.commandIds++));
			}
		}
		if(this.CheckBool(2))
		{
			if(this.world.TryMoveUp(this.player) == WorldActionOutcome.Approved)
			{
				this.commands.add(new PlayerCommand(PlayerCommandType.MoveUp, this.commandIds++));
			}
		}
		if(this.CheckBool(3))
		{
			if(this.world.TryMoveDown(this.player) == WorldActionOutcome.Approved)
			{
				this.commands.add(new PlayerCommand(PlayerCommandType.MoveDown, this.commandIds++));
			}
		}
		if(this.CheckBool(4))
		{
			if(this.world.TryPlantBomb(this.player) == WorldActionOutcome.Approved)
			{
				this.commands.add(new PlayerCommand(PlayerCommandType.PlantBomb, this.commandIds++));
			}
		}
	}
	
	
	//every time a value is requested, it is reset to false
	protected boolean CheckBool(int i)
	{
		if(this.boolsDown[i])
		{
			this.boolsDown[i] = false;
			return true;
		}
		return false;
	}
}
