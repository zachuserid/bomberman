package Test;

import BombermanGame.*;

public class GameServerTest
{

	public static void main(String[] args)
	{
    	
		BombermanServerNetworkHandler server = new BombermanServerNetworkHandler(8080);
		
		//Start up the services
		if ( !server.Initialize() )
		{
			System.out.println("Could not initialize game server");
			return;
		}
		
		//TODO: build objects and test send and get data
		
	}
}
