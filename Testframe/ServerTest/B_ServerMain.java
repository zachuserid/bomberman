import java.io.*;
import java.util.ArrayList;

import BombermanGame.B_Player;
import BombermanGame.B_View;
import BombermanGame.GridGenerator;
import BombermanGame.GridObject;
import BombermanGame.PlayerCommand;
import BombermanGame.ViewRenderer;
import BombermanGame.World;

public class B_ServerMain
{

	public static ArrayList<PlayerCommand[]> received = new ArrayList<PlayerCommand[]>();
	public static ArrayList<PlayerCommand> commands = new ArrayList<PlayerCommand>();

	public static GridObject[][] grid = new GridObject[][]
			{
				new GridObject[] { GridObject.Empty, GridObject.Empty, GridObject.Empty, GridObject.Empty, GridObject.Empty }, 
				new GridObject[] { GridObject.Empty, GridObject.Empty, GridObject.Empty, GridObject.Wall, GridObject.HiddenPowerUp1 }, 
				new GridObject[] { GridObject.Empty, GridObject.Empty, GridObject.Wall, GridObject.Wall, GridObject.Empty }, 
				new GridObject[] { GridObject.Empty, GridObject.Empty, GridObject.Wall, GridObject.HiddenPowerUp2, GridObject.Empty }, 
				new GridObject[] { GridObject.Wall, GridObject.Empty, GridObject.Empty, GridObject.Empty, GridObject.Wall }
			};

	public static World w = new World(grid);


	public static boolean updates = false;
	public static boolean parsing = false;
	public static Writer printer;
	public static boolean shutdown = false;

	public static void main(String[] args)
	{
		String logger_path = "Logs/log.txt";
		if ( args.length >= 1 ) logger_path = args[0];

		File f= null;
		try 
		{
			f = new File(logger_path);
			FileOutputStream st = new FileOutputStream(f);
			///st = new FileOutputStream(logger_path);
		    printer = new BufferedWriter(new OutputStreamWriter(st, "utf-8"));
		} 
		catch (IOException e) 
		{
		  System.out.println("ERROR: Could not stat file: " + logger_path + ". " + e.getMessage() + f.getAbsolutePath());
		}

		B_ServerNetworkHandler network = new B_ServerNetworkHandler(8090, 2);

		if(!network.Initialize())
		{
			System.out.println("Server Fail");
			return;
		}

		ViewRenderer v = new B_View(w, 50);

		B_NetworkPlayerController c = new B_NetworkPlayerController(w);

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
							if(shutdown) return;
							try { commands.wait(); } catch (InterruptedException e) {}
						}

						//Log commands made by player

						for(PlayerCommand command : commands)
						{
							try 
							{
								printer.write("Received movement: " + command.Command.toString() 
										+ " from player '" + command.PlayerName + "\n");
								printer.flush();
							} 
							catch (IOException e) 
							{
								System.out.println("ERROR: Could not write to file: " + e.getMessage());
							}
						}

						commands.clear();

						//Check for any collisions on the map
						int playersOnMap = 0;
						for (int i=0; i<w.getGridWidth(); i++)
						{
							for (int j=0; j<w.getGridHeight(); j++)
							{
								if (w.getElementAt(i, j) == GridObject.Player){
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
				PlayerCommand[] joins = network.getJoinRequests();
				for(PlayerCommand command : joins)
				{
					B_Player p =w.AddPlayer(command.PlayerName);

					c.AddPlayer(p);

					System.out.println("~~~~~~~Got join request from " + command.PlayerName);

					//String joinString = new String(new String(new byte[]{(byte)5}) + p.getName() + "," + p.getCharacter() + ","
					//		+p.getX() + "," + p.getY());

					//network.Send(command.PlayerName, joinString);
				}


				received = network.getData();

				for(PlayerCommand[] command : received)
				{
					synchronized(commands)
					{
						for(int i = 0; i < command.length; i++)
							commands.add(command[i].getCopy());
					}

					ArrayList<PlayerCommand> cs = new ArrayList<PlayerCommand>();
					for(PlayerCommand pc : command)
						cs.add(pc);
					c.AddCommands(command[0].PlayerName, cs);
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

				//w.printGrid();
				network.Send(new B_NetworkPacket(w, w.getPlayers()));

				try { Thread.sleep(milliwait); } 
				catch (InterruptedException e) { e.printStackTrace(); }

				time = System.currentTimeMillis() - start;
			}
		} catch(Exception e)
		{
			System.out.println("Exception in server main: " + e.getMessage());
			e.printStackTrace();
			if (v != null) v.Dispose(); 
		}
		finally
		{
			System.out.println("Stopping server");

			network.Stop();

			synchronized(commands)
			{
				shutdown = true;
				commands.notify();
			}
		}
	}
}