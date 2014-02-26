import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;


public class BombermanView
{
	protected JFrame frame;
	
	protected int width, height;
	
	protected int gridX, gridY, gridDim;
	
	protected BufferStrategy buffer;
	
	protected BufferedImage drawBuffer;

	public BombermanView(int gridX, int gridY, int gridDim)
	{
		this.width = gridX * gridDim;
		this.height = gridY * gridDim;
		this.gridX = gridX;
		this.gridY = gridY;
		this.gridDim = gridDim;
		
		this.frame = new JFrame("Bomberman");
		//this.frame.setUndecorated(true);
		this.frame.setIgnoreRepaint(true);
		this.frame.setVisible(true);
		this.frame.setResizable(false);
		this.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		//rendering canvas
		Canvas c = new Canvas();
		
		c.setIgnoreRepaint(true);
		c.setSize(new Dimension(this.width, this.height));
		
		this.frame.add(c);
		this.frame.pack();
		
		c.createBufferStrategy(2);
		this.buffer = c.getBufferStrategy();
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

		GraphicsDevice gd = ge.getDefaultScreenDevice();

		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		
		this.drawBuffer = gc.createCompatibleImage(this.width, this.height);
	}
	
	public void Draw()
	{
		Graphics2D g = (Graphics2D) this.drawBuffer.createGraphics();
		
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, this.width, this.height);
		
		g.setColor(Color.WHITE);
		
		for(int x = 1; x < this.gridX; x++)
			g.drawLine(x * this.gridDim, 0, x * this.gridDim, this.height);
		
		for(int y = 1; y < this.gridY; y++)
			g.drawLine(0, y * this.gridDim, this.width, y * this.gridDim);
		
		g.setColor(Color.RED);
		g.drawLine(0, 0, (int)(this.width * Math.random()), (int)(this.height * Math.random()));
		
		
		Graphics finalGraphics = this.buffer.getDrawGraphics();
		finalGraphics.drawImage(this.drawBuffer, 0, 0, null);
		
		if(!this.buffer.contentsLost()) this.buffer.show();
	}
	
	public void Close()
	{
		this.drawBuffer.createGraphics().dispose();
		
		this.buffer.getDrawGraphics().dispose();
		
		this.frame.dispose();
	}
}
