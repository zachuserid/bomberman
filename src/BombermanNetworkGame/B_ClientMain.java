package BombermanNetworkGame;

import java.util.ArrayList;

import BombermanGame.*;
import BombermanNetworkGame.B_ClientNetworkHandler;

public class B_ClientMain
{

	public static void main(String[] args)
	{
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
		B_ClientNetworkHandler network = new B_ClientNetworkHandler(serverAddr, serverPort);

		//This client's player
		//B_Player player = null;

		//The world object to be updated
		World theWorld = new World(GridGenerator.EmptyGrid(7, 7));

		//the view
		B_View view = new B_View(theWorld, 50);

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
					int myPlayerId = PlayerName.valueOf(player.getName()).ordinal();
					
					//Create players in our world object
					for (int i=0; i<myPlayerId; i++)
					{
						theWorld.AddPlayer(PlayerName.values()[i].toString());
					}
					
					//Initialize player controller class with my initialized player
					controller = B_PlayerController.Default(theWorld.AddPlayer(player.getName()), theWorld);
					
					for (int i=myPlayerId+1; i<4; i++)
					{
						theWorld.AddPlayer(PlayerName.values()[i].toString());
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
			return;
		}

		long milliwait = 100;
		long start = System.currentTimeMillis();
		long time = 0;
		long prevTime = 0;

		//Arraylist in which to received updates
		ArrayList<B_Packet> updatePackets;

		//Main client game loop.. While window open and game not over.
		while(!view.exitPrompt() && !theWorld.isGameOver())
		{	
			//Update view with any world update from server
			updatePackets = network.getData();
			for(B_Packet packet : updatePackets)
			{
				if (packet.Command == PlayerCommandType.Update)
				{
					U_ClientData update = (U_ClientData)packet.Data;
					theWorld.setUpdatedData(update);
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
		
		network.Stop();

		//The game is over. Compute winner, end prompt
		B_Player winner = theWorld.getWinner();
		if(winner != null) 
			System.out.println("Winner is " + winner.getName());
		else 
			System.out.println("No Winner");

		for(B_Player currPlayer : theWorld.getPlayers())
			System.out.println(currPlayer.getName() + " killcount: " 
		                       + currPlayer.getKillCount());
		
		//TODO: Maybe display an end screen with winner information,
		// rather than closing window
		if (view != null)
			view.Dispose();
	}
}