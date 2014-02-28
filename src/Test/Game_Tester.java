package Test;

import java.util.ArrayList;

import BombermanGame.BombermanController;
import BombermanGame.BombermanPlayer;
import BombermanGame.BombermanPlayerController;
import BombermanGame.BombermanView;
import BombermanGame.GridGenerator;
import BombermanGame.PlayerCommand;
import BombermanGame.Point;
import BombermanGame.ViewRenderer;
import BombermanGame.World;

public class Game_Tester
{

	public static void main(String[] args)
	{
		//M
		World w = new World(GridGenerator.FromFile("Worlds/w1.txt"));
				
		//V
		ViewRenderer v = new BombermanView(w, 50);
				
		//C
		ArrayList<BombermanController> c = new ArrayList<BombermanController>();
				
				
		BombermanPlayer p = new BombermanPlayer("Alex", Point.Zero());
				
		BombermanPlayerController pc = BombermanPlayerController.Default(p, w);					
						
		c.add(pc);
				
		w.AddEntity(p);	
				
		long milliwait = 100;
				
		long start = System.currentTimeMillis();
		long time = 0;
				
		try
		{
			while(!v.exitPrompt())
			{
				for(BombermanController ctrl : c) ctrl.Update(time);
						
				v.Draw();
						
				try { Thread.sleep(milliwait); } 
				catch (InterruptedException e) { e.printStackTrace(); }
						
				time = System.currentTimeMillis() - start;
			}
		} catch(Exception e){v.Dispose();}
				
		for(PlayerCommand com : ((BombermanPlayerController)c.get(0)).getCommandsClear()) System.out.println(com.Command.toString()); 
	}
}
