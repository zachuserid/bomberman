package BombermanNetworkGame;

import java.io.*;
import java.util.ArrayList;

import BombermanGame.B_Player;
import BombermanGame.B_View;
import BombermanGame.GridGenerator;
import BombermanGame.GridObject;
import BombermanGame.PlayerCommand;
import BombermanGame.PlayerCommandType;
import BombermanGame.ViewRenderer;
import BombermanGame.World;

public class B_ServerMain
{
	
	public static ArrayList<PlayerCommand[]> received = new ArrayList<PlayerCommand[]>();
	public static ArrayList<PlayerCommand> commands = new ArrayList<PlayerCommand>();
	
	public static World w = new World(GridGenerator.RandomGrid(7, 7, false));

	public static boolean updates = false;
	public static boolean parsing = false;
	public static Writer printer;
	public static boolean shutdown = false;
	
	public static final int MAX_PLAYERS = 4;
	
	public static void main(String[] args)
	{
		
		int port = 8090;
		
		if (args.length == 1)
			port = Integer.parseInt(args[0]);
		
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
		
		System.out.println("Starting server on port "+port);
		B_ServerNetworkHandler network = new B_ServerNetworkHandler(port, MAX_PLAYERS);
		
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

		boolean startGame = false;
		int playerjoinCount = 0;
		ArrayList<B_Player> playersJoined = new ArrayList<B_Player>();
		long prevTime = 0;
		
		try
		{
			v.Draw();
			while(!v.exitPrompt())
			{	
				float elapsedSeconds = (time-prevTime)/1000f;
				
				
				if (!startGame){
					
					PlayerCommand[] joins = network.getJoinRequests();
					for(PlayerCommand command : joins)
					{
						//System.out.println("This: " + command.Command);
						
						if ((command.Command == PlayerCommandType.Join) && (playerjoinCount < MAX_PLAYERS)){
							
							B_Player p = w.AddPlayer(command.PlayerName);
							
							c.AddPlayer(p);
							
							playersJoined.add(p);
							
							playerjoinCount ++;
							
							System.out.println("~~~~~~~Got join request from " + command.PlayerName + " " + playerjoinCount + " players in game");
							
							v.Draw();
							//String joinString = new String(new String(new byte[]{(byte)5}) + p.getName() + "," + p.getCharacter() + ","
							//		+p.getX() + "," + p.getY());
												
							//network.Send(command.PlayerName, joinString);
							
						}
						if ((command.Command == PlayerCommandType.Start) || (playerjoinCount==MAX_PLAYERS)){
						 					
							for (B_Player p: playersJoined)
							{
								network.ackJoinRequest(p,  w.getGridWidth(), w.getGridHeight());
							}
							
							
							startGame = true;
							network.Send(new B_NetworkPacket(w, w.getPlayers()));					
						}
					}
				}
				else
				{
					received = network.getData();
						
					for(PlayerCommand[] command : received)
					{
						//If some error occurred and we have empty updates, bail to next
						if (command.length == 0) continue;
						
						//Test logger stuff
						synchronized(commands)
						{
							for(int i = 0; i < command.length; i++)
								commands.add(command[i].getCopy());
						}
							
						ArrayList<PlayerCommand> cs = new ArrayList<PlayerCommand>();

						for(PlayerCommand pc : command){
							cs.add(pc);
						}
							
						c.AddCommands(command[0].PlayerName, cs);
						
					}
					
					c.Update(elapsedSeconds);
					w.Update(elapsedSeconds);
					
					network.Send(new B_NetworkPacket(w, w.getPlayers()));

					
					v.Draw();					
	
					updates = true;
					if ( commands.size() > 0 )
					{
						synchronized(commands){
							commands.notify();
						}
					}
					
					//w.printGrid();
					//network.Send(new B_NetworkPacket(w, w.getPlayers()));
							
					try { Thread.sleep(milliwait); } 
					catch (InterruptedException e) { e.printStackTrace(); }
					
					prevTime = time;
					time = System.currentTimeMillis() - start;
					
					
					
					if (w.isGameOver()){
						//network.Send(new B_NetworkPacket(w, w.getPlayers()));
						
						break;
					}
				}
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
		
		//The game is over. Compute winner, end prompt
				B_Player winner = w.getWinner();
				if(winner != null) 
					System.out.println("Winner is " + winner.getName());
				else 
					System.out.println("No Winner");

				for(B_Player currPlayer : w.getPlayers())
					System.out.println(currPlayer.getName() + " killcount: " + currPlayer.getKillCount());
	}
}
