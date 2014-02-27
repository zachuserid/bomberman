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
}
