package Test;

import java.util.ArrayList;

import BombermanGame.*;
import BombermanNetworkGame.B_ClientNetworkHandler;

public class Z_Client
{


	public static void main(String[] args)
	{
		// ----Initializations & Declerations----
		//Network handler for client
		B_ClientNetworkHandler network = new B_ClientNetworkHandler("127.0.0.1", 8090);

		//This client's player
		B_Player player = null;

		//The world object to be updated
		World theWorld = null;

		//the view
		B_View view = null;

		//Controller for key events to PlayerCommand updates
		B_PlayerController controller = null;

		if (!network.Initialize(false, true)) {
			System.out.println("Client network fail");
			return;
		}
		// -----------------------------

		int currentCommandId = 0;

		B_Packet received[];

		//Create a join request and send it
		PlayerCommand[] commands = new PlayerCommand[] {new PlayerCommand(PlayerCommandType.Join,
															currentCommandId++)};
		network.Send(commands);

		ArrayList<Point> doorLocations = new ArrayList<Point>();

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
					player = (B_Player)p.Data;
					System.out.println("Received my player: " + player.getName());
					haveJoinAck = true;
				} 
			}
		}

		boolean haveWorld = false;
		//Wait until I have the first broadcast update from the server,
		// containing the initial world object, before intializing player controller
		while(!haveWorld)
		{
			
			//Client blocks for first update() overriding receiverThread in network handler...
			received = network.blockAndReceive();
			System.out.println("A");
			//Iterate over each received B_Packet, handle it accordingly
			for(B_Packet packet: received)
			{
				System.out.println("B");
				if(packet.Command == PlayerCommandType.Update)
				{
					System.out.println("Got initial world... Starting client");

					//Intialize the world
					theWorld = (World)packet.Data;

					for (int i=0; i<theWorld.getGridWidth(); i++)
					{
						for (int j=0; j<theWorld.getGridHeight(); j++)
						{
							if (theWorld.getElementAt(i, j) == GridObject.Door)
								doorLocations.add(new Point(i, j));
						}
					}

					//Intialize the view with the world
					view = new B_View(theWorld, 50);

					//Flag to exit this loop
					haveWorld = true;
				} 
			}
		}

		//Start the network's receiver thread
		if (!network.startReceiver())
		{
			System.out.println("Could not start receiver thread");
			network.Stop();
			return;
		}

		//Initialize player controller class
		controller = B_PlayerController.Default(player, theWorld);

		long milliwait = 100;
		long start = System.currentTimeMillis();
		long time = 0;
		long prevTime = 0;

		//Arraylist in which to received updates
		ArrayList<B_Packet> updatePackets;

		boolean gameOver = false;

		//Main client game loop.. While window open and game not over.
		while(!view.exitPrompt() && !gameOver)
		{	
			//Get list of commands from player controller
			PlayerCommand myUpdates[] = controller.getCommandsClear();
			if (myUpdates.length > 0)
					network.Send(myUpdates);

			//Update view with any world update from server
			updatePackets = network.getData();
			for(B_Packet packet : updatePackets)
			{
				if (packet.Command == PlayerCommandType.Update)
				{

					theWorld = (World)packet.Data;
					view.setWorld(theWorld);

					//Check endgame condition
					int kills = 0;
					for (B_Player pl: theWorld.getPlayers())
					{
						kills += pl.getKillCount();
						//Check if this player is on a door
						for (Point doorLoc: doorLocations)
						{
							if (pl.getX() == doorLoc.X && pl.getY() == doorLoc.Y)
								gameOver = true;
						}
					}

					if (kills >= 3)
						gameOver = true;
				}
			}

			float elapsedSeconds = (time-prevTime)/1000f;
			controller.Update(elapsedSeconds);

			//theWorld.Update(elapsedSeconds);
			view.Draw();

			try { Thread.sleep(milliwait); } 
			catch (InterruptedException e) { e.printStackTrace(); }

			prevTime = time;
			time = System.currentTimeMillis() - start;

		}

		//The game is over. Compute winner, end prompt
		B_Player winner = theWorld.getWinner();
		if(winner != null) 
			System.out.println("Winner is " + winner.getName());
		else 
			System.out.println("No Winner");

		for(B_Player currPlayer : theWorld.getPlayers())
			System.out.println(currPlayer.getName() + " killcount: " + currPlayer.getKillCount());
	}
}