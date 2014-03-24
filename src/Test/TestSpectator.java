package Test;

import java.util.ArrayList;

import BombermanGame.*;
import BombermanNetworkGame.B_ClientNetworkHandler;
import BombermanNetworkGame.U_ClientData;

public class TestSpectator
{
	
	static B_ClientNetworkHandler network;

	public static void main(String[] args)
	{
		
		TestSpectator client = new TestSpectator();
		
		// ----Initializations & Declerations----
		//Network handler for client
		
		String serverAddr = "127.0.0.1";
		int serverPort = 8090;
		
		if (args.length == 2)
		{
			serverAddr = args[0];
			serverPort = Integer.parseInt(args[1]);
		}
			
		System.out.println("Server location: "+serverAddr+":"+serverPort);
		network = new B_ClientNetworkHandler(serverAddr, serverPort);

		//The world object to be updated
		World theWorld = new World(GridGenerator.EmptyGrid(7, 7));

		//the view
		B_View view = new B_View(theWorld, 50);

		if (!network.Initialize(false, true)) 
		{
			System.out.println("Client network fail");
			return;
		}
		// -----------------------------
		
		client.joinAsSpectator(theWorld);

		client.spectatorUpdateLoop(view, theWorld);
		
		network.Stop();

		//The game is over. Compute winner, end prompt
		client.displayWinner(theWorld, view);
		
		//TODO: Maybe display an end screen with winner information,
		// rather than closing window
		if (view != null)
			view.Dispose();
		
	} //End main
	
	//================================Client Methods===================================
	
	//Join the game as player, initialize the controller objects. 
	//Add all players to the world
	//Returns success or failure boolean
	public B_PlayerController joinAsPlayer(World world)
	{
		B_Packet received[];

		//join server as player
		network.joinGame(1);

		boolean haveJoinAck = false;
		
		B_PlayerController controller = null;
		
		//Wait until I have an acknowledgement for my join.
		//This will contain all of my player information
		System.out.println("Waiting for an ack to my join...");
		while (!haveJoinAck)
		{
			//Client blocks for first update() overriding receiverThread in network handler...
			received = network.blockAndReceive();

			//Iterate over each received B_Packet, handle it accordingly
			for(B_Packet p : received)
			{
				if(p.Command == PlayerCommandType.Join)
				{
					B_Player player = (B_Player)p.Data;
					int myPlayerId = PlayerName.valueOf(player.getName()).ordinal();
					
					//Create players in our world object
					for (int i=0; i<myPlayerId; i++)
					{
						world.AddPlayer(PlayerName.values()[i].toString());
					}
					
					//Initialize player controller class with my initialized player
					controller = B_PlayerController.Default(world.AddPlayer(player.getName()), world);
					
					for (int i=myPlayerId+1; i<4; i++)
					{
						world.AddPlayer(PlayerName.values()[i].toString());
					}
					
					System.out.println("Received my player: " + player.getName());
					haveJoinAck = true;
				} 
			}
		}

		//Start the network's receiver thread
		if (!network.startReceiver())
		{
			System.out.println("Could not start receiver thread");
			network.Stop();
		}
		
		return controller;
	}
	
	//Join the game as player, initialize the controller objects. 
	//Add all players to the world
	//Returns success or failure boolean
	public void joinAsSpectator(World world)
	{
		B_Packet received[];

		//join server as player
		network.joinGame(0);

		boolean haveJoinAck = false;
				
		//Wait until I have an acknowledgement for my join.
		//This will contain all of my player information
		System.out.println("Waiting for an ack to my join...");
		while (!haveJoinAck)
		{
			//Client blocks for first update() overriding receiverThread in network handler...
			received = network.blockAndReceive();

			//Iterate over each received B_Packet, handle it accordingly
			for(B_Packet p : received)
			{
				if(p.Command == PlayerCommandType.Join)
				{
					B_Player player = (B_Player)p.Data;
					
					//Create players in our world object
					for (int i=0; i<4; i++)
					{
						world.AddPlayer(PlayerName.values()[i].toString());
					}
					
					System.out.println("Received my player: " + player.getName());
					haveJoinAck = true;
				} 
			}
		}

		//Start the network's receiver thread
		if (!network.startReceiver())
		{
			System.out.println("Could not start receiver thread");
			network.Stop();
		}
	}	
	
	public void playerUpdateLoop(B_PlayerController controller, B_View view, World world)
	{
		long milliwait = 100;
		long start = System.currentTimeMillis();
		long time = 0;
		long prevTime = 0;

		//Arraylist in which to received updates
		ArrayList<B_Packet> updatePackets;

		//Main client game loop.. While window open and game not over.
		while(!view.exitPrompt() && !world.isGameOver())
		{	
			//Update view with any world update from server
			updatePackets = network.getData();
			for(B_Packet packet : updatePackets)
			{
				if (packet.Command == PlayerCommandType.Update)
				{
					U_ClientData update = (U_ClientData)packet.Data;
					world.setUpdatedData(update);
				}
			}
			
			float elapsedSeconds = (time-prevTime)/1000f;
			controller.Update(elapsedSeconds);
			
			//Get list of commands from player controller
			PlayerCommand myUpdates[] = controller.getCommandsClear();
			
			if (myUpdates.length > 0)
				network.Send(myUpdates);
			

			//theWorld.Update(elapsedSeconds);
			view.Draw();

			try { Thread.sleep(milliwait); } 
			catch (InterruptedException e) { e.printStackTrace(); }

			prevTime = time;
			time = System.currentTimeMillis() - start;

		}
	}
	
	public void spectatorUpdateLoop(B_View view, World world)
	{
		long milliwait = 100;

		//Arraylist in which to received updates
		ArrayList<B_Packet> updatePackets;

		//Main client game loop.. While window open and game not over.
		while(!view.exitPrompt() && !world.isGameOver())
		{	
			//Update view with any world update from server
			updatePackets = network.getData();
			for(B_Packet packet : updatePackets)
			{
				if (packet.Command == PlayerCommandType.Update)
				{
					U_ClientData update = (U_ClientData)packet.Data;
					world.setUpdatedData(update);
				}
			}
			
			view.Draw();

			try { Thread.sleep(milliwait); } 
			catch (InterruptedException e) { e.printStackTrace(); }
		}
	}
	
	//Game is over, display end game details
	public void displayWinner(World world, B_View view)
	{
		B_Player winner = world.getWinner();
		if(winner != null) 
			System.out.println("Winner is " + winner.getName());
		else 
			System.out.println("No Winner");

		for(B_Player currPlayer : world.getPlayers())
			System.out.println(currPlayer.getName() + " killcount: " 
		                       + currPlayer.getKillCount());
	}
	
}