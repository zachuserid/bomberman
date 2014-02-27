package BombermanGame;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.ArrayList;


public class BombermanPlayerController extends BombermanController implements KeyEventDispatcher
{
	public static BombermanPlayerController Default(BombermanPlayer player, World w) {return new BombermanPlayerController(player, w, 'a', 'd', 'w', 's', ' ');}
	
	protected int controlCount;
	//the arrays representing the state of a key being pressed
	protected boolean[] boolsDown;
	//the chars to map to input
	protected char[] cKeys;
	
	protected BombermanPlayer player;
	
	protected World world;
	
	protected ArrayList<PlayerCommand> commands;
	
	protected int commandIds;
	
	
	public PlayerCommand[] getCommandsClear()
	{
		PlayerCommand[] com = new PlayerCommand[this.commands.size()];
		
		this.commands.toArray(com);
		
		return com;
	}
	
	
	public BombermanPlayerController(BombermanPlayer player, World w, char left, char right, char up, char down, char bomb)
	{
		this.player = player;
		this.world = w;
		this.commands = new ArrayList<PlayerCommand>();
		this.commandIds = 0;
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
		
		this.controlCount = 5;
		
		this.boolsDown = new boolean[this.controlCount];
		
		//the keys mapped to a control
		this.cKeys = new char[this.controlCount];
		this.cKeys[0] = left;
		this.cKeys[1] = right;
		this.cKeys[2] = up;
		this.cKeys[3] = down;
		this.cKeys[4] = bomb;
	}
	
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent e)
	{
		char c = e.getKeyChar();
		
		for(int i = 0; i < this.controlCount; i++)
		{
			if(c == this.cKeys[i])
			{
				if(e.getID() == KeyEvent.KEY_PRESSED)
					this.boolsDown[i] = true;
				
				i = this.controlCount;
			}
		}
		
		return false;
	}

	@Override
	public void Update(float time)
	{
		if(this.CheckBool(0))
		{
			if(this.world.TryMoveLeft(this.player) == WorldActionOutcome.Approved)
			{
				this.commands.add(new PlayerCommand(PlayerCommandType.MoveLeft, time, this.commandIds++));
			}
		}
		if(this.CheckBool(1))
		{
			if(this.world.TryMoveRight(this.player) == WorldActionOutcome.Approved)
			{
				this.commands.add(new PlayerCommand(PlayerCommandType.MoveRight, time, this.commandIds++));
			}
		}
		if(this.CheckBool(2))
		{
			if(this.world.TryMoveUp(this.player) == WorldActionOutcome.Approved)
			{
				this.commands.add(new PlayerCommand(PlayerCommandType.MoveUp, time, this.commandIds++));
			}
		}
		if(this.CheckBool(3))
		{
			if(this.world.TryMoveDown(this.player) == WorldActionOutcome.Approved)
			{
				this.commands.add(new PlayerCommand(PlayerCommandType.MoveDown, time, this.commandIds++));
			}
		}
	}
	
	
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
