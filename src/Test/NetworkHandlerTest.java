package Test;

/**
 * @(#)NetworkHandlerTest.java
 *
 *
 * @author
 * @version 1.00 2014/2/26
 */

import java.net.*;
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
    	    	
    	    	TestClient testClient = new TestClient(50000, addr, 8080);
    	    	
    			if ( !testClient.Initialize() )
    	    	{
    	    		System.out.println("testClient failed to start");
    	    	}
    			
    			String sendBuf[] = new String[25];
    			
    			for ( int i=0; i<25; i++ )
    			{
    				
    	    		sendBuf[i] = "String index" + i + "-";
    	    		
    			}
    			
    	    	testClient.sendData(sendBuf);
    	    		
    	    	
    			
    			System.out.println("Terminating Client");
    			
    			try { Thread.sleep(20000); } catch(Exception e){
    				System.out.println("Could not sleep");
    			}
    			
    			testClient.Stop();
    			
    		}
    	}).start();
    	
    	
    	
    	
    	//Receiving thread
    	new Thread(new Runnable(){
    		public void run(){
    			
    			String recBuf[] = new String[25];
    			
    			TestServer testServer = new TestServer(50000, 8080);
    		    
    	    	if ( !testServer.Initialize() )
    	    	{
    	    		System.out.println("testServer failed to start");
    	    	}
    	    	
    	    	//Allow traffic to get from client to server
    	    	try { Thread.sleep(5000); } catch(Exception e){}
    	    	
    	    	testServer.getData(recBuf);
    	    	
    			for ( int i=0; i<25; i++ )
    			{
    	    		if ( recBuf[i] != null && !recBuf[i].equals("") ){
    	    			System.out.println("IMPORTANT: recBuf["+i+"] = " + recBuf[i]);
    	    		}
    	    	}
    			
    			
    			System.out.println("Terminating Server");
    			
    			try { Thread.sleep(20000); } catch(Exception e){
    				System.out.println("Could not sleep");
    			}
    			
    			testServer.Stop();
    			
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
    	
    	System.out.println("Starting network handler");
    	
    	if ( args[0].toLowerCase().equals("client") )
    	{
    		test.startClient();
    	}
    }


}


/*
 *  The test server class
 */
class TestServer extends ServerNetworkHandler<String, String>{

	//listen port
	int port;

	//Constructors
	
	public TestServer(int size, int port){
		super(new String[size], new String[size], new String[size], new String[size], port);
	}

	
	//Methods 
	
	//return a R(eceive) type object from parsing raw packet data
	@Override
    protected int parseReceive(String[] array, int currIndex, byte[] data)
    {
    	String rawStr = new String( data );
    	String strs[] = rawStr.split("-");
    	for (int i=0; i<(strs.length-1); i++)
    	{
    		array[currIndex++] = strs[i];
    	}
    	return currIndex;
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
class TestClient extends ClientNetworkHandler<String, String> {

	//Constructors 
	
	public TestClient(int size, InetAddress ip, int port){
		super(new String[size], new String[size], new String[size], new String[size], ip, port);
	}
	
	//Methods
	
	//return a R(eceive) type object from parsing raw packet data
	@Override
	protected int parseReceive(String[] array, int currIndex, byte[] data)
    {
    	String rawStr = new String( data );
    	String strs[] = rawStr.split("-");
    	for (int i=0; i<(strs.length-1); i++)
    	{
    		array[currIndex++] = strs[i];

    	}
    	return currIndex;
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