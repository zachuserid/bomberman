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
		PlayerCommand[] commands = new PlayerCommand[] {new PlayerCommand(PlayerCommandType.Join, 0, 0)};
		
		network.Send(commands);
		
		//Receive packets into this
		ArrayList<BomberPacket> packets;
		
		//Sleep for a second..
		try { Thread.sleep(1000); } 
		catch (InterruptedException e) { e.printStackTrace(); }
		
		//Create packet array to receive messages from network handler
		packets = network.getData();
		
		//Iterate over each received BombermanPacket, handle it accordingly
		System.out.println("First wave of get data");
		for(BomberPacket p : packets)
		{
			if(p.Command == PlayerCommandType.Join)
			{
				System.out.println("Received player " + ((BombermanPlayer)p.Data).name + " join from server");
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
				int highAck = (int)p.Data;
				System.out.println("~~~Received ack from server for id: " + highAck);
			}
		}
		
		//Send 10 movements, with a second between them
		for(int i = 0; i < 10; i++)
		{
			commands = new PlayerCommand[] {new PlayerCommand(PlayerCommandType.MoveRight, 0, i)};
			
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
				System.out.println("Received player " + ((BombermanPlayer)p.Data).name + " join from server");
			} else if (p.Command == PlayerCommandType.Update)
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
				int highAck = (int)p.Data;
				System.out.println("~~~Received ack from server for id: " + highAck);
			}
		}

		//Shutdown the network handler
		network.Stop();

	}

}
