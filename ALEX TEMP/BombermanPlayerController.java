import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;


public class BombermanPlayerController implements KeyEventDispatcher
{
	public static BombermanPlayerController Default() {return new BombermanPlayerController('a', 'd', 'w', 's', ' ');}
	
	protected int controlCount;
	//the arrays representing the state of a key being pressed
	protected boolean[] boolsDown;
	//the chars to map to input
	protected char[] cKeys;
	
	public BombermanPlayerController(char left, char right, char up, char down, char bomb)
	{
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
	
	
	public boolean MoveLeft()
	{
		if(this.boolsDown[0])
		{
			this.boolsDown[0] = false;
			return true;
		}
		return false;
	}
	
	public boolean MoveRight()
	{
		if(this.boolsDown[1])
		{
			this.boolsDown[1] = false;
			return true;
		}
		return false;
	}
	
	public boolean MoveUp()
	{
		if(this.boolsDown[2])
		{
			this.boolsDown[2] = false;
			return true;
		}
		return false;
	}
	
	public boolean MoveDown()
	{
		if(this.boolsDown[3])
		{
			this.boolsDown[3] = false;
			return true;
		}
		return false;
	}
	
	public boolean PlantBomb()
	{
		if(this.boolsDown[4])
		{
			this.boolsDown[4] = false;
			return true;
		}
		return false;
	}
}
