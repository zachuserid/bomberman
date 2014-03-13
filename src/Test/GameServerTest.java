package Test;

import java.io.*;
import java.util.ArrayList;
import BombermanGame.*;

public class GameServerTest
{
	
	public static ArrayList<PlayerCommand[]> received = new ArrayList<PlayerCommand[]>();
	public static ArrayList<PlayerCommand> commands = new ArrayList<PlayerCommand>();
	public static World w = new World(GridGenerator.FromFile("Worlds/w1.txt"));


	public static boolean updates = false;
	public static boolean parsing = false;
	public static Writer printer;
	public static boolean shutdown = false;
	
	public static final int MAX_PLAYERS = 4;
	
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
		
		BombermanServerNetworkHandler network = new BombermanServerNetworkHandler(8090, MAX_PLAYERS);
		
		if(!network.Initialize())
		{
			System.out.println("Server Fail");
			return;
		}
		
		ViewRenderer v = new BombermanView(w, 50);
		
		BombermanNetworkPlayerController c = new BombermanNetworkPlayerController(w);
		
		long milliwait = 100;

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
								if (w.getElementAt(i, j) != '.' && w.getElementAt(i, j) != 'D'){
									playersOnMap++;
								}
							}
						}
						//System.out.println("found " + playersOnMap + "/" + w.getPlayerCount());						
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
		
		int gameState = 1; //{accept joins & start, handle updates, exit}
		int joinCount = 0;
		boolean newUpdates = false;
		ArrayList<BombermanPlayer> playersJoined = new ArrayList<BombermanPlayer>();
		
		try
		{
			while(!v.exitPrompt())
			{	
				
				if (gameState == 1)
				{										
					PlayerCommand[] joins = network.getJoinRequests();
					for(PlayerCommand command : joins)
					{
						joinCount++;
							
						BombermanPlayer p = w.AddPlayer(command.PlayerName);
							
						c.AddPlayer(p);
							
						playersJoined.add(p);
						
						System.out.println("~~~~Got join request from " + command.PlayerName 
											+ " " + joinCount + " players in game");
					}
					
					//Not enough players to move on to update stage yet
					if (joinCount >= MAX_PLAYERS)
					{
						//TODO: Note that sending the joinAck should only be done
						// after we handle the 'start game' signal, since the client
						// will imediately begin the update loop after a join ack
						
						//ack all joins
						for (BombermanPlayer p: playersJoined)
						{
							network.ackJoinRequest(p,  w.getGridWidth(), w.getGridHeight());
						}
						
						gameState++;
					}
				}
				if (gameState == 2)
				{
					newUpdates = false;
					received = network.getData();
										
					for(PlayerCommand[] command : received)
					{						
						//If some error occured and we have empty updates, bail to next
						if (command.length == 0) continue;
						
						synchronized(commands)
						{
							for(int i = 0; i < command.length; i++)
								commands.add(command[i].getCopy());
						}
							
						ArrayList<PlayerCommand> cs = new ArrayList<PlayerCommand>();
						for(PlayerCommand pc : command)
						{
							newUpdates = true;
							cs.add(pc);
						}
						
						c.AddCommands(command[0].PlayerName, cs);
						
						c.Update(time);
						v.Draw();
						
						//Send out new state
						if (newUpdates)
						{
							network.Send(new B_NetworkPacket(w, w.getPlayers()));
						}
						
					}
					
					//if (some endgame condition)
					//	gameState++;
				}
				
				v.Draw();

				updates = true;
				if (commands.size() > 0) 
				{
					synchronized (commands) 
					{
						commands.notify();
					}
				}
					
						
				try { Thread.sleep(milliwait); } 
				catch (InterruptedException e) { e.printStackTrace(); }
				
			} //End while not view terminating
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
