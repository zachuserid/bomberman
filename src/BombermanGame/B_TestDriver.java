package BombermanGame;

import java.util.ArrayList;

public class B_TestDriver 
{

	public static void main(String[] args) 
	{
		BombermanClientNetworkHandler network = new BombermanClientNetworkHandler("127.0.0.1", 8090);
		
		if(!network.Initialize())
		{
			System.out.println("Client network fail");
			return;
		}
		
		PlayerCommand[] commands = new PlayerCommand[] {new PlayerCommand(PlayerCommandType.Join, 0, 0)};
		
		network.Send(commands);
		
		try { Thread.sleep(1000); } 
		catch (InterruptedException e) { e.printStackTrace(); }
		
		ArrayList<BomberPacket> packets = network.getData();
		
		for(BomberPacket p : packets)
		{
			if(p.Command == PlayerCommandType.Join)
			{
				System.out.println(((BombermanPlayer)p.Data).name + " joined client");
			}
		}
		
		for(int i = 0; i < 10; i++)
		{
			commands = new PlayerCommand[] {new PlayerCommand(PlayerCommandType.MoveRight, 0, 0)};
			
			network.Send(commands);
			
			try { Thread.sleep(1000); } 
			catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		network.Stop();

	}

}
