package BombermanGame;

import java.util.ArrayList;

public class B_TestDriver 
{

	public static void main(String[] args) 
	{
		//----Init network handler----
		BombermanClientNetworkHandler network = new BombermanClientNetworkHandler("127.0.0.1", 8090);
		
		if(!network.Initialize())
		{
			System.out.println("Client network fail");
			return;
		}
		//-----------------------------
		
		//Create a join request and send it
		PlayerCommand[] commands = new PlayerCommand[] {new PlayerCommand(PlayerCommandType.Join, 0)};
		
		network.Send(commands);
		
		//TODO wait for join ack containing my player id to include in all new sends
		
		//Receive packets into this
		ArrayList<BomberPacket> packets;
		
		//Sleep for a second..
		try { Thread.sleep(1000); } 
		catch (InterruptedException e) { e.printStackTrace(); }
		
		boolean haveJoinAck = false;
		
		System.out.println("First wave of get data");
		while (!haveJoinAck)
		{
			//TODO: Replace this while (not have initial join ack) to a
			// clientBlocksForFirstUpdate() overriding receiverThread in network handler...
			
			//Create packet array to receive messages from network handler
			packets = network.getData();
			//Iterate over each received BombermanPacket, handle it accordingly
			for(BomberPacket p : packets)
			{
				System.out.println("--Type: " + p.Command.toString());
				if(p.Command == PlayerCommandType.Join)
				{
					System.out.println("Received player " + ((BombermanPlayer)p.Data).name + " join from server");
					haveJoinAck = true;
				} 
				else if (p.Command == PlayerCommandType.Update)
				{
					System.out.println("Received update command from server:");
					char payload[][] = (char[][])p.Data;
					for (int i=0; i<payload.length; i++)
					{
						for (int j=0; j<payload[0].length; j++)
						{
							System.out.print(payload[i][j]);
						}
						System.out.println("");
					}
				} 
				else if (p.Command == PlayerCommandType.Ack)
				{
					//NOTE: SHOULD NEVER GET IN HERE >> REMOVE ONCE CONFIRMED
					int highAck = (int)p.Data;
					System.out.println("~~~BADBADBAD Received ack from server for id: " + highAck);
				}
			}
		}
		
		//Send 10 movements, with a second between them
		System.out.println("Client sending some updates for the player (MoveRight x 10)");
		for(int i = 0; i < 10; i++)
		{
			commands = new PlayerCommand[] {new PlayerCommand(PlayerCommandType.MoveRight, i)};
			
			network.Send(commands);
			
			try { Thread.sleep(1000); } 
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		packets = network.getData();
		//Once more, Iterate over each received BombermanPacket, 
		// handle it accordingly
		System.out.println("Second wave of get data");
		for(BomberPacket p : packets)
		{
			if(p.Command == PlayerCommandType.Join)
			{
				//NOTE SHOULD NEVER GET IN HERE >> REMOVE ONCE CONFIRMED
				System.out.println("BADBADBAD Received player " + ((BombermanPlayer)p.Data).name + " join from server");
			} 
			else if (p.Command == PlayerCommandType.Update)
			{
				System.out.println("Received update command from server:");
				char payload[][] = (char[][])p.Data;
				for (int i=0; i<payload.length; i++)
				{
					for (int j=0; j<payload[0].length; j++)
					{
						System.out.print(payload[i][j]);
					}
					System.out.println("");
				}
			}
			else if (p.Command == PlayerCommandType.Ack)
			{
				//NOTE SHOULD NEVER GET IN HERE >> REMOVE ONCE CONFIRMED (same as above)
				int highAck = (int)p.Data;
				System.out.println("~~~BADBABA Received ack from server for id: " + highAck);
			}
		}

		//Shutdown the network handler
		network.Stop();

	}

}
