package Test;

import BombermanGame.*;

public class WorldLogicMain
{
	
	
	public static void main(String[] args)
	{
		//GridGenerator.FromFile("Worlds/w1.txt")
		
		GridObject[][] grid = new GridObject[][]
				{
					new GridObject[] { GridObject.Empty, GridObject.Wall, GridObject.Empty, GridObject.Empty, GridObject.Empty }, 
					new GridObject[] { GridObject.Empty, GridObject.Empty, GridObject.Empty, GridObject.Wall, GridObject.HiddenPowerUp1 }, 
					new GridObject[] { GridObject.Wall, GridObject.Wall, GridObject.Wall, GridObject.Wall, GridObject.Empty }, 
					new GridObject[] { GridObject.HiddenDoor, GridObject.Empty, GridObject.Wall, GridObject.HiddenPowerUp2, GridObject.Empty }, 
					new GridObject[] { GridObject.Wall, GridObject.Empty, GridObject.Empty, GridObject.Empty, GridObject.Wall }
				};
				
		World w = new World(grid);
		
		ViewRenderer v = new B_View(w, 50);
		
		B_Player p = w.AddPlayer("Alex");
		B_Player p2 = w.AddPlayer("Jim");
		
		B_PlayerController c = B_PlayerController.Default(p2, w);
		
		long milliwait = 100;
		
		long start = System.currentTimeMillis();
		long time = 0;
		long prevTime = 0;
		
		while(!v.exitPrompt() && !w.isGameOver())
		{	
			float elapsedSeconds = (time-prevTime)/1000f;
			c.Update(elapsedSeconds);
			
			//to get list of commands from player controller
			//c.getCommandsClear();
			
			w.Update(elapsedSeconds);
			v.Draw();
						
			try { Thread.sleep(milliwait); } 
			catch (InterruptedException e) { e.printStackTrace(); }
			
			prevTime = time;
			time = System.currentTimeMillis() - start;
		}
		
		B_Player winner = w.getWinner();
		if(winner != null) System.out.println("Winner is " + winner.getName());
		else System.out.println("No Winner");
		
		for(B_Player player : w.getPlayers())
			System.out.println(player.getName() + " " + player.getKillCount());
		
		//v.Dispose();
	}
}
