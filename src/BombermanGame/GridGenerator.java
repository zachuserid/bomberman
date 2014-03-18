package BombermanGame;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/*
 * this class simply reads a grid from a file
 * it may eventually have a random generation or other fancy things
 */
public class GridGenerator
{
	//TODO: implement this method for GridObject[][]
	public static char[][] FromFile(String path)
	{
		//creates the grid
		char[][] grid = null;
		
		//creates a reader
		BufferedReader br = null;
		
		try 
	    {
			//opens the reader with the path
			br = new BufferedReader(new FileReader(path));
			
			//reads in a line
	        String line = br.readLine();
	        
	        //the "height" of the grid (actually width)
	        int x;
	        x = Integer.parseInt(line);

	        grid = new char[x][];
	        
	        line = br.readLine();
	        
	        x = 0;
	        
	        while (line != null) 
	        {
	        	grid[x] = line.toCharArray();
	        	x++;
	        	
	            line = br.readLine();
	        }
	    }
		catch(IOException e) {}
	    finally 
	    {
	        try {br.close();} catch(IOException e){}
	    }	
	    
	    return grid;
	}
	
	//Returns a randomized grid for width and height provided
	public static GridObject[][] RandomGrid(int width, int height, boolean doorInMiddle)
	{
		if (width < 4) width = 4;
		if (height < 4) height = 4;
		
		GridObject grid[][] = new GridObject[width][height];
		
		int maxDoors = 1, numDoors = 0;
		if (doorInMiddle) maxDoors = 0;
		
		//Max of 6% coverage by powerups
		int maxPowerups = (int)((width*height)*0.07), numPowerups = 0;
		
		//Hidden powerups appear as blocks, so we will include these with max walls
		int maxWalls = ((width*height)/2) - maxPowerups, numWalls = 0;
		
		int rand;
		
		//So won't randomly choose a player or bomb in the starting grid..
		int usableGridTypes[] = { 0, 1, 2, 4, 5, 6, 7, 8, 9};
		
		int randMax = usableGridTypes.length;
		
		for (int i=0; i<width; i++)
		{
			for (int j=0; j<height; j++)
			{
				rand = (int)(Math.random() * randMax);
				
				GridObject object = GridObject.values()[usableGridTypes[rand]];
				
				//Handle too many objects 
				if (object == GridObject.Wall)
				{
					if (numWalls >= maxWalls) object = GridObject.Empty;
					else numWalls++;
				}
				else if (object == GridObject.PowerUp1 || object == GridObject.PowerUp2
					  || object == GridObject.PowerUp3 || object == GridObject.HiddenPowerUp1
					  || object == GridObject.HiddenPowerUp2 || object == GridObject.HiddenPowerUp3)
				{
					if (numPowerups >= maxPowerups) object = GridObject.Empty;
					else numPowerups++;
				}
				if (object == GridObject.Door || object == GridObject.HiddenDoor)
				{
					if (numDoors >= maxDoors) object = GridObject.Wall;
					else numDoors++;
				}
				
				grid[i][j] = object;
			}
		}
		
		if (doorInMiddle)
		{
			grid[width/2][height/2] = GridObject.Door;
		}
		else if (numDoors == 0)
		{
			int randX = (int)(Math.random() * width);
			int randY = (int)(Math.random() * height);
			grid[randX][randY] = GridObject.HiddenDoor;
		}
		
		return grid;
	}
	
	//Returns an empty grid
	public static GridObject[][] EmptyGrid(int width, int height)
	{
		GridObject grid[][] = new GridObject[width][height];
		for (int i=0; i<width; i++)
		{
			for (int j=0; j<height; j++)
			{
				grid[i][j] = GridObject.Empty;
			}
		}
		return grid;
	}
	
	//Returns predefined static grid used for testing.
	//Contains 2 hidden powerups and 1 door.
	public static GridObject[][] TestingGrid()
	{
		return new GridObject[][]
			{
				new GridObject[] { GridObject.Empty, GridObject.Empty, GridObject.Empty, GridObject.Empty, GridObject.Empty, GridObject.Empty, GridObject.Empty }, 
				new GridObject[] { GridObject.Empty, GridObject.Empty, GridObject.Empty, GridObject.Wall, GridObject.HiddenPowerUp1, GridObject.Empty, GridObject.Empty }, 
				new GridObject[] { GridObject.Empty, GridObject.Empty, GridObject.Empty, GridObject.Empty, GridObject.Empty, GridObject.Empty, GridObject.Empty }, 
				new GridObject[] { GridObject.HiddenDoor, GridObject.Empty, GridObject.Empty, GridObject.HiddenPowerUp2, GridObject.Empty, GridObject.Empty, GridObject.Empty }, 
				new GridObject[] { GridObject.Empty, GridObject.Empty, GridObject.Empty, GridObject.Empty, GridObject.Empty, GridObject.Empty, GridObject.Empty },
				new GridObject[] { GridObject.Empty, GridObject.Empty, GridObject.Empty, GridObject.Empty, GridObject.Wall, GridObject.Empty, GridObject.Empty },
				new GridObject[] { GridObject.Wall, GridObject.Wall, GridObject.Empty, GridObject.Empty, GridObject.Wall, GridObject.Empty, GridObject.Empty }
			};
	}
}
