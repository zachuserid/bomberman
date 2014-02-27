package BombermanGame;

public class GridGenerator
{
	public static char[][] FromFile(String path)
	{
		char[][] grid = null;
		
		/*
		BufferedReader br = new BufferedReader(new FileReader(path));
	    try 
	    {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        
	        int x, y;
	        x = Integer.parseInt(line);
	        line = br.readLine();
	        y = Integer.parseInt(line);

	        grid = new char[x][y];
	        
	        line = br.readLine();
	        
	        while (line != null) 
	        {

	            line = br.readLine();
	        }
	        
	        System.out.print(x + ", " + y);
	        //String everything = sb.toString();
	    }
	    finally 
	    {
	        br.close();
	    }*/
		
		grid = new char[5][5];
	    
	    return grid;
	}
}
