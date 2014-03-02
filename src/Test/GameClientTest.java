package Test;

import java.util.ArrayList;
import java.util.Date;

import BombermanGame.*;

public class GameClientTest
{

	public static void main(String[] args) throws InterruptedException
	{
    	
    	//Create a game server and client
		BombermanClientNetworkHandler client = new BombermanClientNetworkHandler("127.0.0.1", 8080);

		System.out.println("--Starting (Test) Game Client--");
		
		//Start up the services
		if ( !client.Initialize() )
		{
			System.out.println("Could not initialize client");
			return;
		}
		
		client.joinGame();
		
		PlayerCommand moves[] = new PlayerCommand[2];
		for (int i=0; i<moves.length; i++)
		{
			moves[i] = new PlayerCommand(PlayerCommandType.MoveRight, new Date().getTime(), i);
		}
		
		ArrayList<PlayerCommand[]> moveList = new ArrayList<PlayerCommand[]>();
		
		moveList.add(moves);
		
		client.sendData(moveList);
		
		System.out.println("About to receive some data");
		ArrayList<char[][]> received;
		
		int h=0;
		while ( true )
		{
			Thread.sleep(1000);
			received = client.getData();
			h++;
			for ( int i=0; i<received.size(); i++ )
			{
				char[][] rec_grid = received.get(i);
				World rec_world = new World(rec_grid);
				System.out.println("Received this world update from server:");
				rec_world.printGrid();
			}
			if ( h == 20 ) break;
		}
		
		System.out.println("Finished receiving data");
		
		client.Stop();
		
	}
}
