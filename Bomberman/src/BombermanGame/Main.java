package BombermanGame;


import java.util.ArrayList;



public class Main
{
	public static void main(String[] args)
	{
		//M
		World w = new World(GridGenerator.FromFile(""));
		
		//V
		BombermanView v = new BombermanView(w, 50);
		
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
			while(time < 10000)
			{
				for(BombermanController ctrl : c) ctrl.Update(time);
				
				v.Draw();
				
				try { Thread.sleep(milliwait); } 
				catch (InterruptedException e) { e.printStackTrace(); }
				
				time = System.currentTimeMillis() - start;
			}
		} finally{v.Close();}
		
		for(PlayerCommand com : ((BombermanPlayerController)c.get(0)).getCommandsClear()) System.out.println(com.Command.toString()); 
	}
}