package BombermanGame;

import java.io.*;
import java.util.ArrayList;

public class BombermanServerMain
{
	
	public static ArrayList<PlayerCommand[]> received = new ArrayList<PlayerCommand[]>();
	public static ArrayList<PlayerCommand[]> commands = new ArrayList<PlayerCommand[]>();
	public static World w = new World(GridGenerator.FromFile("Worlds/w1.txt"));


	public static boolean updates = false;
	public static boolean parsing = false;
	public static Writer printer;
	
	public static void main(String[] args)
	{
		String logger_path = "Logs/log.txt";
		if ( args.length >= 1 ) logger_path = args[0];
		
		try {
		    printer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logger_path), "utf-8"));
		} catch (IOException e) {
		  System.out.println("ERROR: Could not stat file: " + logger_path + ". " + e.getMessage());
		}
		
		BombermanServerNetworkHandler network = new BombermanServerNetworkHandler(8090);
		
		if(!network.Initialize())
		{
			System.out.println("Server Fail");
			return;
		}
		
		ViewRenderer v = new BombermanView(w, 50);
		
		BombermanNetworkPlayerController c = new BombermanNetworkPlayerController(w);
		
		long milliwait = 100;
		
		long start = System.currentTimeMillis();
		long time = 0;
		
		//Create the logger
		Thread testLogger = new Thread(new Runnable(){
			public void run()
			{
				while (true)
				{
					synchronized (commands)
					{					
					
						while (commands.size() == 0)
						{
							try { commands.wait(); } catch (InterruptedException e) {}
						}
						
						//Log commands made by player
						for (int j=0; j<commands.size(); j++)
						{
							PlayerCommand[] pc = commands.remove(0);
							for (int k=0; k<pc.length; k++)
							{
								try {
									printer.write("Received movement: " + pc[k].Command + " from player '" + pc[k].PlayerName + "'. Time: " + pc[k].Time + "\n");
									printer.flush();
								} catch (IOException e) {
									System.out.println("ERROR: Could not write to file: " + e.getMessage());
								}
							}
						}
						
						//Check for any collisions on the map
						int playersOnMap = 0;
						for (int i=0; i<w.getGridWidth(); i++)
						{
							for (int j=0; j<w.getGridHeight(); j++)
							{
								if (w.getElementAt(i, j) != '.' && w.getElementAt(i, j) != 'D'){
									playersOnMap++;
								}
							}
						}
						System.out.println("found " + playersOnMap + "/" + w.getPlayerCount());						
						if ( playersOnMap < w.getPlayerCount() ){
							try {
								printer.write("WARNING: COLLISSIONS DETECTED\n");
								printer.flush();
							} catch (IOException e) {
								System.out.println("ERROR: Could not write to file: " + e.getMessage());
							}
						}
						
					}
				}
			}

		}); //End thread
		
		testLogger.start();
		
		
		try
		{
			while(!v.exitPrompt())
			{	

				received = network.getData();
					
				for(PlayerCommand[] command : received)
				{
					commands.add(command);
						
					if(command[0].Command == PlayerCommandType.Join)
					{
						Point location = w.getNextPlayerLocation();
						BombermanPlayer p = new BombermanPlayer(command[0].PlayerName, location);
						
						synchronized (commands)
						{
							w.AddEntity(p);
							c.AddPlayer(p);
						}
						
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

				updates = true;
				if ( commands.size() > 0 )
				{
					synchronized(commands){
						commands.notify();
					}
				}
				
				
				ArrayList<World> aw = new ArrayList<World>();
				aw.add(w);
				//w.printGrid();
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
