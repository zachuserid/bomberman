package Test;

/**
 * @(#)NetworkHandlerTest.java
 *
 *
 * @author
 * @version 1.00 2014/2/26
 */

import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.*;

import Networking.ClientNetworkHandler;
import Networking.ServerNetworkHandler;

@SuppressWarnings("unused")
public class NetworkHandlerTest {

    public NetworkHandlerTest() {
    }

    public void startClient()
    {	
    	//Sending thread
    	new Thread(new Runnable(){
    		public void run(){
    			
    	    	InetAddress addr;
    	    	try {
    	    		addr = InetAddress.getByName("127.0.0.1");
    	    	} catch (UnknownHostException e)
    	    	{
    	    		System.out.println("Unknown host exception!");
    	    		return;
    	    	}
    	    	
    	    	TestClient testClient = new TestClient(addr, 8090);
    	    	
    			if ( testClient.Initialize() )
    	    	{
    	    		for(int i = 0; i < 10; i++)
    	    		{
    	    			System.out.println("Sending packet");
    	    			
    	    			ArrayList<String> sendBuf = new ArrayList<String>();
    	    			sendBuf.add("Message " + i);
    	    			testClient.sendData(sendBuf);
    	    			
    	    			try { Thread.sleep(2000); } 
    	    			catch(Exception e){System.out.println("Could not sleep");}
    	    			
    	    			System.out.println("Trying to pull data");
	        	    	
	        	    	ArrayList<String> recBuf  = testClient.getData();
	        	    	
	        	    	if(recBuf.size() == 0) System.out.println("No data");
	        	    	else
	        	    	{
	        	    		System.out.println("Client Pulled ");
	        	    		for ( int n=0; n<recBuf.size(); n++ ) System.out.print(recBuf.get(n));
		        			System.out.println("");
	        	    	}
    	    		}
    	    		
    	    		System.out.println("Terminating Client");
        			testClient.Stop();
    	    	}	
    		}
    	}).start();
    	  	
    	//Receiving thread
    	new Thread(new Runnable(){
    		public void run(){
    			
    			TestServer testServer = new TestServer(8080);
    		    
    	    	if ( testServer.Initialize() )
    	    	{
    	    		//Allow traffic to get from client to server
        	    	try { Thread.sleep(5000); } 
        	    	catch(Exception e){}
        	    	
        	    	
        	    	for(int i = 0; i < 10; i++)
        	    	{
	        	    	System.out.println("Trying to pull data");
	        	    	
	        	    	ArrayList<String> recBuf  = testServer.getData();
	        	    	
	        	    	if(recBuf.size() == 0) System.out.println("No data");
	        	    	else
	        	    	{
	        	    		System.out.println("Pulled ");
	        	    		for ( int n=0; n<recBuf.size(); n++ ) System.out.print(recBuf.get(n));
		        			System.out.println("");
	        	    	}
	        			
	        			try { Thread.sleep(2000); } catch(Exception e){
	        				System.out.println("Could not sleep");
	        			}
        	    	}
        			
        	    	System.out.println("Terminating Server");
        			testServer.Stop();
    	    	}
    		}
    	}).start();
    	
    	
    }

    public static void main(String args[])
    {
    	if ( args.length < 1 ){
    		System.out.println("Insufficient arguments");
    		return;
    	}
    	
    	NetworkHandlerTest test = new NetworkHandlerTest();
    	
    	System.out.println("Starting network handlers");
    	
    	if ( args[0].toLowerCase().equals("client") )
    	{
    		test.startClient();
    	}
    }
}


/*
 *  The test server class
 */
class TestServer extends ServerNetworkHandler<String, String>
{
		public TestServer(int port)
		{
			super(port);
		}

		
		//Methods 
		
		//return a R(eceive) type object from parsing raw packet data
		@Override
	    protected String[] parseReceive(byte[] data)
	    {
	    	return new String[] { new String(data) };
	    }

		
	    //return raw packet data from an S(end) type object
	    @Override
	    protected byte[] parseSend(String data)
	    {
	    	return data.getBytes();
	    }

	    
	    //a copy method to be implemented at lowest level
	    @Override
	    public String getSendCopy(String original)
	    {
	    	return new String (original);
	    }

	    
		//a copy of the receive type object given
		@Override
	    public String getReceiveCopy(String original)
		{
			return new String (original);
		}
}



/*
 * The test client class
 */
class TestClient extends ClientNetworkHandler<String, String>
{
	public TestClient(InetAddress ip, int port)
	{
		super(ip, port);
	}
	
	//Methods
	
	//return a R(eceive) type object from parsing raw packet data
	@Override
	protected String[] parseReceive(byte[] data)
    {
		return new String[] { new String(data) };
    }
	

    //return raw packet data from an S(end) type object
    @Override
    protected byte[] parseSend(String data)
    {
    	return data.getBytes();
    }

    
    //a copy method to be implemented at lowest level
    @Override
    public String getSendCopy(String original)
    {
    	if (original == null){
    		return "got null, giving new string";
    	}
    	return new String (original);
    }
    

	//a copy of the receive type object given
	@Override
    public String getReceiveCopy(String original)
	{
		return new String (original);
	}
	

}