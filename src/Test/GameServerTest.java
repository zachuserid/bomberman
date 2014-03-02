package Test;

import java.util.ArrayList;

import BombermanGame.*;

public class GameServerTest
{

	public static void main(String[] args) throws InterruptedException
	{
    	
		BombermanServerNetworkHandler server = new BombermanServerNetworkHandler(8080);
		
		System.out.println("--Starting (Test) Game Server--");
		
		//Start up the services
		if ( !server.Initialize() )
		{
			System.out.println("Could not initialize game server");
			return;
		}
			
		//TODO: build objects and test send and get data
		ArrayList<PlayerCommand[]> received;
		int h=0;
		while ( true )
		{
			Thread.sleep(1000);
			received = server.getData();
			h++;
			for ( int i=0; i<received.size(); i++ )
			{
				PlayerCommand coms[] = received.get(i);
				for (int j=0; j<coms.length; j++)
				{
					System.out.println("command received: " + coms[j].toString());
				}
			}
			if ( h == 20 ) break;
		}
		
		char wGrid[][] = 
			{  
				{'.', '.', '.', 'c', '.'},
			    {'.', 'e', '.', 'D', '.'},
				{'.', '.', '.', '.', '.'},
				{'.', '.', 'e', '.', '.'},
				{'P', '.', '.', '.', '.'}
			};
		
		ArrayList<World> worldList = new ArrayList<World>();
		
		World world = new World(wGrid);

		System.out.println("Server, sending grid:");
		
		world.printGrid();
		
		worldList.add(world);
		
		server.sendData(worldList);
		
		Thread.sleep(5000);
		
		server.Stop();
		
	}
}
