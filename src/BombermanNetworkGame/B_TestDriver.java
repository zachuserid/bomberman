package BombermanNetworkGame;

import java.util.ArrayList;

import BombermanGame.B_Packet;
import BombermanGame.B_Player;
import BombermanGame.GridObject;
import BombermanGame.PlayerCommand;
import BombermanGame.PlayerCommandType;
import BombermanGame.World;

public class B_TestDriver 
{

	public static void main(String[] args) 
	{
		//----Init network handler----
		B_ClientNetworkHandler network = new B_ClientNetworkHandler("127.0.0.1", 8090);
		
		if(!network.Initialize(false, true))
		{
			System.out.println("Client network fail");
			return;
		}
		//-----------------------------
		
		int currentCommandId = 0;
		
		//Create a join request and send it
		PlayerCommand[] commands = new PlayerCommand[] {new PlayerCommand(PlayerCommandType.Join,
															currentCommandId++)};
		
		network.Send(commands);
		
		//TODO wait for join ack containing my player id to include in all new sends
		
		//Receive packets into this
		ArrayList<B_Packet> packets;
		
		//Sleep for a second..
		try { Thread.sleep(1000); } 
		catch (InterruptedException e) { e.printStackTrace(); }
		
		boolean haveJoinAck = false;
		
		System.out.println("First wave of get data");
		while (!haveJoinAck)
		{
			//Client blocks for first update() overriding receiverThread in network handler...
			B_Packet firstPacket[] = network.blockAndReceive();
			
			//Iterate over each received B_Packet, handle it accordingly
			for(B_Packet p : firstPacket)
			{
				if(p.Command == PlayerCommandType.Join)
				{
					System.out.println("**Received player " + ((B_Player)p.Data).getName() + " join from server. Moving on to game.");
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
		
		//Move and receive();
		System.out.println("Client sending some updates for the player (MoveRight x 10)");
		for(int i = 0; i < 2; i++)
		{
			//The 3rd update will be a bomb drop
			if (i==0)
			{
				commands = new PlayerCommand[]{ new PlayerCommand(PlayerCommandType.PlantBomb,
														currentCommandId++) };
				System.out.println("Dropping a bomb!!!");
			}
			else 
			{
				commands = new PlayerCommand[] {new PlayerCommand(PlayerCommandType.MoveRight, 
												currentCommandId++)};
			}
			network.Send(commands);
		
			packets = network.getData();
			//Once more, Iterate over each received B_Packet, 
			// handle it accordingly
			for(B_Packet p : packets)
			{
				//System.out.println("~~Client update from server. Type: " + p.Command.toString()); 
				if (p.Command == PlayerCommandType.Update)
				{
					//System.out.println("Received update command from server:");
					//GridObject payload[][] = (GridObject[][])p.Data;
					//(new World(payload)).printGrid();
					
					B_Player playerArray[] = (B_Player[])p.MetaData;
					for (B_Player pl: playerArray)
					{
						//System.out.println("Received player: " + pl.getName() + " at " + pl.getX() +","+ pl.getY()
						//		+" powerup: " + pl.getPowerup() + " kills: " + pl.getKillCount());
					}
					
				}
			}
			
			try { Thread.sleep(1000); } 
			catch (InterruptedException e) { e.printStackTrace(); }
		}

		//Shutdown the network handler
		network.Stop();

	}

}
