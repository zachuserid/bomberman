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
		this.playerColors[0] = Color.GREEN;
		this.playerColors[0] = Color.BLUE;
		this.playerColors[0] = Color.YELLOW;
	}
	
	@Override
	public void CustomDraw(Graphics2D g)
	{
		//draws the board elements
		g.setColor(Color.LIGHT_GRAY);
		for(int x = 0; x < this.gridX; x++)
			for(int y = 0; y < this.gridY; y++)
			{
				char c = this.world.getElementAt(x, y);
				if(c == 'D')
				{
					int xl = x * this.gridDim;
					int yl = y * this.gridDim;
					
					g.drawRect(xl + 5, yl + 5, this.gridDim - 10, this.gridDim - 10);
				}
			}
		
		//draws the grid
		g.setColor(Color.GRAY);
				
		for(int x = 1; x < this.gridX; x++)
			g.drawLine(x * this.gridDim, 0, x * this.gridDim, this.getRenderHeight());
				
		for(int y = 1; y < this.gridY; y++)
			g.drawLine(0, y * this.gridDim, this.getRenderWidth(), y * this.gridDim);
		
		//draws the entities
		int i = 0;
		for(Entity e : this.world.getEntities())
		{
			g.setColor(this.playerColors[i]);
			
			int x = e.getX() * this.gridDim;
			int y = e.getY() * this.gridDim;
			
			g.drawOval(x, y, this.gridDim, this.gridDim);
			
			i++;
		}
	}
	
	
}
