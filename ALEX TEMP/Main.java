import javax.swing.JFrame;



public class Main
{
	public static void main(String[] args)
	{
		World w = new World(GridGenerator.FromFile(""));
		
		BombermanPlayerController c = BombermanPlayerController.Default();
		
		w.AddEntity(new ClientPlayer("Alex", Point.Zero()));	
		
		BombermanView v = new BombermanView(w.getGridWidth(), w.getGridHeight(), 50);
		
		long milliwait = 100;
		
		try //if something happens, make sure to close the view...
		{
			long start = System.currentTimeMillis();
		
			while(System.currentTimeMillis() - start < 10000)
			{
				if(c.MoveLeft()) System.out.println("Controller tried to move left");
				if(c.MoveRight()) System.out.println("Controller tried to move right");
				if(c.MoveUp()) System.out.println("Controller tried to move up");
				if(c.MoveDown()) System.out.println("Controller tried to move down");
				if(c.PlantBomb()) System.out.println("Controller tried to plant bomb");
			
				w.Update((float)milliwait); 
				v.Draw();
			
				try { Thread.sleep(milliwait); } 
				catch (InterruptedException e) { e.printStackTrace(); }
			}
		}
		finally {v.Close();}
	}
}