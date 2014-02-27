package BombermanGame;

import javax.swing.JFrame;
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

/*
 * this class handles basic buffered rendering
 * it allows a child class to implement a draw method
 * with a 2d graphics renderer
 */
public abstract class ViewRenderer
{
	protected JFrame frame;
	
	protected BufferStrategy backBuffer;
	
	protected BufferedImage drawBuffer;
	
	protected Canvas canvas;
	
	protected Color clearColor;
	
	public int getRenderWidth() {return this.canvas.getWidth();}
	
	public int getRenderHeight() {return this.canvas.getHeight();}
	
	public String getName() {return this.frame.getName();}
	
	
	public ViewRenderer(String name, int width, int height)
	{
		//JFrame settings
		this.frame = new JFrame(name);
		this.frame.setIgnoreRepaint(true);
		this.frame.setVisible(true);
		this.frame.setResizable(false);
		this.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		//this.frame.setUndecorated(true);
		
		//rendering canvas
		this.canvas = new Canvas();			
		this.canvas.setIgnoreRepaint(true);
		this.canvas.setSize(new Dimension(width, height));
		
		//adds canvas to frame and packs
		this.frame.add(this.canvas);
		this.frame.pack();
		
		//creates a buffer strategy for the canvas
		this.canvas.createBufferStrategy(2);
		this.backBuffer = this.canvas.getBufferStrategy();
		
		//creates the drawBuffer to use the local graphics device
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		
		this.drawBuffer = gc.createCompatibleImage(width, height);
		
		this.clearColor = Color.BLACK;
	}
	
	public void Draw()
	{
		//creates the drawable graphics (sets rendertarget to drawbuffer)
		Graphics2D g = (Graphics2D) this.drawBuffer.createGraphics();
		
		//clears the draw buffer with clear color
		g.setColor(this.clearColor);
		g.fillRect(0, 0, this.getRenderWidth(), this.getRenderHeight());
		
		//calls child implementation
		this.CustomDraw(g);
		
		//draws the image from the drawbuffer to the backbuffer
		Graphics finalGraphics = this.backBuffer.getDrawGraphics();
		finalGraphics.drawImage(this.drawBuffer, 0, 0, null);
		
		if(!this.backBuffer.contentsLost()) this.backBuffer.show();
	}
	
	//this is the method the child will implement to draw their own graphics
	protected abstract void CustomDraw(Graphics2D graphics);
	
	public void Dispose()
	{	
		this.backBuffer.getDrawGraphics().dispose();
		
		this.frame.dispose();
	}
}
