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

    public void runTest()
    {	
    	//Client thread
    	new Thread(new Runnable(){
    		public void run(){
    	    	
    	    	TestClient testClient = new TestClient("localhost", 8090);
    	    	
    			if ( testClient.Initialize() )
    	    	{
    				
    	    		for(int i = 0; i < 10; i++)
    	    		{
    	    			System.out.println("Client: Sending packet");
    	    			
    	    			ArrayList<String> sendBuf = new ArrayList<String>();
    	    			sendBuf.add("Message " + i);
    	    			testClient.sendData(sendBuf);
    	    			
    	    			try { Thread.sleep(2000); } 
    	    			catch(Exception e){System.out.println("Client: Could not sleep");}
    	    			
    	    			//THE RECEIVE BLOCK====================
    	    			
    	    			System.out.println("Client: Trying to pull data");
	        	    	
	        	    	ArrayList<String> recBuf  = testClient.getData();
	        	    	
	        	    	if(recBuf.size() == 0) System.out.println("Client: No data");
	        	    	else
	        	    	{
	        	    		System.out.println("~~Client: Pulled ");
	        	    		for ( int n=0; n<recBuf.size(); n++ ) 
	        	    			System.out.print(recBuf.get(n) + ", ");
		        			System.out.println("");
	        	    	}
	        	    	
    	    			//======================================
    	    			
    	    		}
    	    		
    	    		System.out.println("Terminating Client");
        			testClient.Stop();
        			
    	    	} else {
    	    		System.out.println("Failed to start test client");
    	    	}
    		}
    	}).start();
    	  	
    	
    	//Server thread
    	new Thread(new Runnable(){
    		public void run(){
    			
    			TestServer testServer = new TestServer(8090);
    		    
    	    	if ( testServer.Initialize() )
    	    	{
    	    		
    	    		//Allow traffic to get from client to server
        	    	try { Thread.sleep(5000); } 
        	    	catch(Exception e){}
        	    	
        	    	ArrayList<String> sendBuf = new ArrayList<String>();

        	    	for ( int i=0; i<10; i++)
        	    	{
	    				sendBuf.add("TOCLIENT_Message " + i);
        	    	}
        	    	
	    			testServer.sendData(sendBuf);
        	    	
        	    	
        	    	for(int i = 0; i < 10; i++)
        	    	{
	        	    	System.out.println("Server: Trying to pull data");
	        	    	
	        	    	ArrayList<String> recBuf = testServer.getData();
	        	    	
	        	    	if(recBuf.size() == 0) System.out.println("Server: No data");
	        	    	else
	        	    	{
	        	    		System.out.println("~~Server: Pulled ");
	        	    		for ( int n=0; n<recBuf.size(); n++ ) 
	        	    			System.out.print(recBuf.get(n) + ", ");
		        			System.out.println("");
	        	    	}
	        			
	        			try { Thread.sleep(2000); } catch(Exception e){
	        				System.out.println("Server: Could not sleep");
	        			}
        	    	}
        			
        	    	System.out.println("Terminating Server");
        			testServer.Stop();
    	    	} else {
    	    		System.out.println("Failed to start test server");
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
    		test.runTest();
    	}
    }
}

//==================TEST CLASSES BELOW=====================

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
		
		@Override
		public boolean preProcessPacket(DatagramPacket p)
		{
			this.addSubscriber("new guy", p.getAddress(), p.getPort());
			return true;
		}
		
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
	public TestClient(String ip, int port)
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