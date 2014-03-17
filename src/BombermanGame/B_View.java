package BombermanGame;

import java.awt.Color;
import java.awt.Graphics2D;

/*
 * this class renders a bomberman world
 * it has simple loops to represent entities and such as shapes
 */
public class B_View extends ViewRenderer
{
	protected int gridX, gridY, gridDim;
	
	protected World world;
	
	protected Color[] playerColors;
	

	public B_View(World w, int gridDim)
	{
		super("Bomberman", w.getGridWidth() * gridDim, w.getGridHeight() * gridDim);
		
		this.world = w;
		this.gridX = w.getGridWidth();
		this.gridY = w.getGridHeight();
		this.gridDim = gridDim;
		
		//default player colors for now
		this.playerColors = new Color[4];
		this.playerColors[0] = Color.RED;
		this.playerColors[1] = Color.GREEN;
		this.playerColors[2] = Color.BLUE;
		this.playerColors[3] = Color.YELLOW;
	}
	
	public void setWorld(World w)
	{
		this.world = w;
	}
	
	public boolean sameWorld(World w)
	{
		for (int i=0; i<this.world.getGridWidth(); i++)
		{
			for (int j=0; j<this.world.getGridHeight(); j++)
			{
				if (this.world.getElementAt(i, j) != w.getElementAt(i, j))
					return false;
			}
		}
		return true;
	}
	
	@Override
	public void CustomDraw(Graphics2D g)
	{
		//draws the board elements
		//System.out.println("~~char[0][0] is " + this.world.getElementAt(0, 0) );
		g.setColor(Color.LIGHT_GRAY);
		for(int x = 0; x < this.gridX; x++)
			for(int y = 0; y < this.gridY; y++)
			{
				GridObject c = this.world.getElementAt(x, y);
				
				int xl = x * this.gridDim;
				int yl = y * this.gridDim;
				
				Point p = new Point(xl, yl);
				
				if(c == GridObject.Empty) this.DrawEmpty(g, p);
				else if(c == GridObject.Wall || c == GridObject.HiddenDoor
						|| c == GridObject.HiddenPowerUp1
						|| c == GridObject.HiddenPowerUp2
						|| c == GridObject.HiddenPowerUp3) this.DrawWall(g, p);
				else if(c == GridObject.PowerUp1) this.DrawPowerUp(g, p, 0);
				else if(c == GridObject.PowerUp2) this.DrawPowerUp(g, p, 1);
				else if(c == GridObject.PowerUp3) this.DrawPowerUp(g, p, 2);
				else if(c == GridObject.Door) this.DrawDoor(g, p);
				else if(c == GridObject.Bomb) this.DrawBomb(g, p, -1, 0);
			}
		
		//draws the grid
		g.setColor(Color.GRAY);
				
		for(int x = 1; x < this.gridX; x++)
			g.drawLine(x * this.gridDim, 0, x * this.gridDim, this.getRenderHeight());
				
		for(int y = 1; y < this.gridY; y++)
			g.drawLine(0, y * this.gridDim, this.getRenderWidth(), y * this.gridDim);
		
		//draws the entities
		int i = 0;
		for(B_Player e : this.world.getPlayers())
		{
			if(e.isAlive())
			{
				int x = e.getX() * this.gridDim;
				int y = e.getY() * this.gridDim;
			
				this.DrawPlayer(g, new Point(x, y), i);
			}
			
			i++;
		}
		
		B_Player[] players = this.world.getPlayers();
		
		for(Bomb b : this.world.getBombs())
		{
			int x = b.getX() * this.gridDim;
			int y = b.getY() * this.gridDim;
			
			i = 0;
			for(B_Player p : players)
			{
				if(p.name == b.name) break;
				i++;
			}
			
			this.DrawBomb(g, new Point(x, y), i, b.getTime());
		}
	}
	
	protected void DrawEmpty(Graphics2D g, Point p)
	{
		
	}
	
	protected void DrawPowerUp(Graphics2D g, Point p, int index)
	{
		g.setColor(this.playerColors[index]);
		
		g.fillRect(p.X + 20, p.Y + 20, this.gridDim - 40, this.gridDim - 40);
	}
	
	protected void DrawWall(Graphics2D g, Point p)
	{
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(p.X + 5, p.Y + 5, this.gridDim - 10, this.gridDim - 10);
	}
	
	protected void DrawDoor(Graphics2D g, Point p)
	{
		g.setColor(Color.LIGHT_GRAY);
		g.drawRect(p.X + 5, p.Y + 5, this.gridDim - 10, this.gridDim - 10);
	}
	
	protected void DrawBomb(Graphics2D g, Point p, int index, float time)
	{
		if(index == -1)
		{
			g.setColor(Color.LIGHT_GRAY);

			g.fillOval(p.X + 10, p.Y + 10, this.gridDim - 20, this.gridDim - 20);
		}
		else
		{
			g.setColor(this.playerColors[index]);
			
			if(time % 2 >= 1) g.fillOval(p.X + 16, p.Y + 16, this.gridDim - 32, this.gridDim - 32);
			else if( time < 0) g.fillOval(p.X, p.Y, this.gridDim, this.gridDim);
			else g.fillOval(p.X + 10, p.Y + 10, this.gridDim - 20, this.gridDim - 20);
		}
	}
	
	protected void DrawPlayer(Graphics2D g, Point p, int index)
	{
		g.setColor(this.playerColors[index]);
		
		g.drawOval(p.X, p.Y, this.gridDim, this.gridDim);
	}
}
