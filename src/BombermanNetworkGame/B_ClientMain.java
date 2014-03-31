package BombermanNetworkGame;

import java.util.ArrayList;
import java.util.Random;
import BombermanGame.*;
import BombermanNetworkGame.B_ClientNetworkHandler;


public class B_ClientMain
{
	
	static B_ClientNetworkHandler network;
	int cmdID = 0;
	
	public B_ClientMain(int n){
		if (n == 1){
			// ----Initializations & Declerations----
			//Network handler for client
			
			String serverAddr = "127.0.0.1";
			int serverPort = 8090;
			
			//Log
			System.out.println("Server location: "+serverAddr+":"+serverPort);
			
			//Start client
			network = new B_ClientNetworkHandler(serverAddr, serverPort);
			
			
			this.startAIClient();
		}
	}
	
	public static void main(String[] args)
	{
		
		B_ClientMain client = new B_ClientMain(0);
		
		boolean playing = true;
		// ----Initializations & Declerations----
		//Network handler for client
		
		String serverAddr = "127.0.0.1";
		int serverPort = 8090;
		
		if (args.length >= 1)
		{
			if (args[0].equals("spectator"))
				playing = false;
		}
		
		if (args.length == 3)
		{
			serverAddr = args[1];
			serverPort = Integer.parseInt(args[2]);
		}
		
		
		//Log
		System.out.println("Server location: "+serverAddr+":"+serverPort);
		
		//Start client
		network = new B_ClientNetworkHandler(serverAddr, serverPort);

		if (playing)
			client.startPlayerClient();
		else
			client.startSpectatorClient();
		
	} //End main
	
	//================================Client Methods===================================
	
	//=====Public Start Methods=====
	
	public void startPlayerClient()
	{
		System.out.println("Starting player");

		// The world object to be updated
		World theWorld = new World(GridGenerator.EmptyGrid(7, 7));

		// the view
		B_View view = new B_View(theWorld, 50);

		// Controller for key events to PlayerCommand updates
		B_PlayerController controller;

		if (!network.Initialize(false, true))
		{
			System.out.println("Client network fail");
			return;
		}
		// -----------------------------

		controller = this.joinAsPlayer(theWorld);

		if (controller == null)
		{
			System.out.println("An error occured while joining game. Exiting.");
			return;
		}

		this.playerUpdateLoop(controller, view, theWorld);

		network.Stop();

		// The game is over. Compute winner, end prompt
		this.displayWinner(theWorld, view);

		// TODO: Maybe display an end screen with winner information,
		// rather than closing window
		if (view != null)
			view.Dispose();
	}
	
	public void startAIClient(){
		System.out.println("Starting player");

		// The world object to be updated
		World theWorld = new World(GridGenerator.EmptyGrid(7, 7));

		// the view
		B_View view = new B_View(theWorld, 50);

		// Controller for key events to PlayerCommand updates
		B_PlayerController controller;// = new B_PlayerController();

		if (!network.Initialize(false, true))
		{
			System.out.println("AI Client network fail");
			return;
		}
		// -----------------------------

		controller = this.joinAsAI(theWorld);

		if (controller == null)
		{
			System.out.println("An error occured while joining game. Exiting.");
			return;
		}

		this.aiUpdateLoop(controller, view, theWorld);

		network.Stop();

		// The game is over. Compute winner, end prompt
		this.displayWinner(theWorld, view);

		// TODO: Maybe display an end screen with winner information,
		// rather than closing window
		if (view != null)
			view.Dispose();
	}
	
	public void startSpectatorClient()
	{
		System.out.println("Starting spectator");
		//The world object to be updated
		World theWorld = new World(GridGenerator.EmptyGrid(7, 7));

		//the view
		B_View view = new B_View(theWorld, 50);

		if (!network.Initialize(false, true)) 
		{
			System.out.println("Spectator Client network fail");
			return;
		}
		// -----------------------------
		
		this.joinAsSpectator(theWorld);

		this.spectatorUpdateLoop(view, theWorld);
		
		network.Stop();

		//The game is over. Compute winner, end prompt
		this.displayWinner(theWorld, view);
		
		//TODO: Maybe display an end screen with winner information,
		// rather than closing window
		if (view != null)
			view.Dispose();
	}
	
	//=====Private Methods For Client Flow=====
	
	//Join the game as player, initialize the controller objects. 
	//Add all players to the world
	//Returns success or failure boolean
	private B_PlayerController joinAsPlayer(World world)
	{
		B_Packet received[];

		//join server as player
		network.joinGame(1);

		boolean haveJoinAck = false;
		
		B_PlayerController controller = null;
		
		//Point point = new Point(0);
		//B_Player b = new B_Player("name", point);
		//B_PlayerController startController;
		//startController = B_PlayerController.Default(b, world);
		
		//Wait until I have an acknowledgment for my join.
		//This will contain all of my player information
		System.out.println("Waiting for an ack to my join...");
		
		(new Thread(new Runnable(){
			B_Packet received[];
			B_PlayerController controller;
			boolean startAck = false;
		   
			public void run(){
		    	
		    	//controller = B_PlayerController.Default(world.AddPlayer(player.getName()), world);
		    	
		        while (!startAck){
		        	
		        	
		        	
		            //key = get key pressed from controller
		            //if (key == 't')
		            //    network.send(new PlayerCommand(PlayerCommandType.Start, 0, 0), ...);
		        	
		        	//Client blocks for first update() overriding receiverThread in network handler...
					received = network.blockAndReceive();
					
					//Iterate over each received B_Packet, handle it accordingly
					for(B_Packet p : received)
					{
						if(p.Command == PlayerCommandType.Start)
						{
							
							//Initialize player controller class with my initialized player
							//startController = B_PlayerController.Default(world.AddPlayer(player.getName()), world);
							
							startAck = true;
						} 
					}
						        	
		        	
		        	
		        }
		    }
		})).start();
		
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
	
	private B_PlayerController joinAsAI(World world)
	{
		B_Packet received[];

		//join server as player
		network.joinGame(1);

		boolean haveJoinAck = false;
		
		B_PlayerController controller = null;
		
		
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
	private void joinAsSpectator(World world)
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
	
	private void playerUpdateLoop(B_PlayerController controller, B_View view, World world)
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
	
	private void aiUpdateLoop(B_PlayerController controller, B_View view, World world)
	{
		long milliwait = 100;
		long start = System.currentTimeMillis();
		long time = 0;
		long prevTime = 0;
		int playerCommandID = 1;

		//Arraylist in which to received updates
		ArrayList<B_Packet> updatePackets;

		//Main client game loop.. While window open and game not over.
		while(!view.exitPrompt() && !world.isGameOver())
		{	
			this.playAI(controller, view, world);
			
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
	
	
	private void spectatorUpdateLoop(B_View view, World world)
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
	
	public void playAI(B_PlayerController controller, B_View view, World world){
		
		//ArrayList<PlayerCommandType> commandList = new ArrayList<PlayerCommandType>();
		//ArrayList<String> commandTypes = new ArrayList<String>();
		
		PlayerCommandType[] cmdType = null;
		
		cmdType[0]=PlayerCommandType.MoveLeft;
		cmdType[1]=PlayerCommandType.MoveRight;
		cmdType[2]=PlayerCommandType.MoveUp;
		cmdType[3]=PlayerCommandType.MoveDown;
		cmdType[4]=PlayerCommandType.PlantBomb;
		cmdType[5]=PlayerCommandType.UsePowerup;
	
		
		
		//String name = controller.player.getName();
		while(!view.exitPrompt() && !world.isGameOver())
		{	
			Random r = new Random();
			int value = r.nextInt(6);
			
			PlayerCommand[] pc = new PlayerCommand[1];
			
				
			
			cmdID++;
			
			
			//network.Send(new PlayerCommand(cmdType[value], cmdID));
			
		}
	}
	
	//Game is over, display end game details
	private void displayWinner(World world, B_View view)
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