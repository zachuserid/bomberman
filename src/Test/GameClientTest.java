package Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import BombermanGame.*;

public class GameClientTest
{

	public static void main(String[] args)
	{
		//Create an IP address object
		InetAddress addr;
    	try {
    		addr = InetAddress.getByName("localhost");
    	} catch (UnknownHostException e)
    	{
    		System.out.println("Client: Unknown host exception!");
    		return;
    	}
    	
    	//Create a game server and client
		BombermanClientNetworkHandler client = new BombermanClientNetworkHandler(addr, 8080, "Worlds/w1.txt");

		//Start up the services
		if ( !client.Initialize() )
		{
			System.out.println("Could not initialize client");
			return;
		}
		
		//TODO: construct some objects and test send, get data
		
	}
}
