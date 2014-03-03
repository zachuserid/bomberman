package BombermanGame;

import java.util.ArrayList;

public class BombermanServerMain
{

	public static void main(String[] args)
	{
		BombermanServerNetworkHandler network = new BombermanServerNetworkHandler(8080);
		
		if(!network.Initialize())
		{
			System.out.println("Server Fail");
			return;
		}
		
		World w = new World(GridGenerator.FromFile("Worlds/w1.txt"));
		ViewRenderer v = new BombermanView(w, 50);
		
		BombermanNetworkPlayerController c = new BombermanNetworkPlayerController(w);
		
		
		long milliwait = 100;
		
		long start = System.currentTimeMillis();
		long time = 0;
		
		ArrayList<PlayerCommand[]> received;
				
		try
		{
			while(!v.exitPrompt())
			{			
				received = network.getData();
				
				for(PlayerCommand[] command : received)
				{
					if(command[0].Command == PlayerCommandType.Join)
					{
						Point location = w.getNextPlayerLocation();
						BombermanPlayer p = new BombermanPlayer(command[0].PlayerName, location);
						
						w.AddEntity(p);
						c.AddPlayer(p);
						
						//needs to get sent to player somehow...
						char playerChar = w.getElementAt(location.X, location.Y);
						
					}
					else
					{
						ArrayList<PlayerCommand> cs = new ArrayList<PlayerCommand>();
						for(PlayerCommand pc : command) cs.add(pc);
						c.AddCommands(command[0].PlayerName, cs);
					}
				}
				
				c.Update(time);
				
				v.Draw();
				
				ArrayList<World> aw = new ArrayList<World>();
				aw.add(w);
				
				network.sendData(aw);
						
				try { Thread.sleep(milliwait); } 
				catch (InterruptedException e) { e.printStackTrace(); }
						
				time = System.currentTimeMillis() - start;
			}
		} catch(Exception e){v.Dispose();}
		finally
		{
			network.Stop();
		}
	}

}
