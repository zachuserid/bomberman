package Network;

/**
 * @(#)NetworkHandlerTest.java
 *
 *
 * @author
 * @version 1.00 2014/2/26
 */

import java.net.*;
import java.util.concurrent.*;

@SuppressWarnings("unused")
public class NetworkHandlerTest {

    public NetworkHandlerTest() {
    }

    public void startClient()
    {
    	InetAddress addr;
    	try {
    		addr = InetAddress.getByName("127.0.0.1");
    	} catch (UnknownHostException e)
    	{
    		System.out.println("Unknown host exception!");
    		return;
    	}
    	
    	TestClient testClient = new TestClient(5000, addr, 8080);
    	
    	TestServer testServer = new TestServer(5000, 8080);
    	
    	if ( !testClient.Initialize() )
    	{
    		System.out.println("testClient failed to start");
    	}
    	if ( !testServer.Initialize() )
    	{
    		System.out.println("testServer failed to start");
    	}
    	
    	try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	String sendBuf[] = new String[25000];
    	String recBuf[] = new String[25000];
    	
    	for ( int i=0; i<25000; i++ ){
    		sendBuf[i] = "String index" + i;
    		testClient.sendData(sendBuf);

    		testServer.getData(recBuf);
    		if ( recBuf[i] != null ){
    			System.out.println("recBuf["+i+"] = " + recBuf[i]);
    		} else {
    			System.out.println("recBuf["+i+"] = Null");
    		}
    	}
    	
    	
    	System.out.println("Terminating");
    	
    	testClient.Stop();
    	testServer.Stop();
    	
    	
    	
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
    protected String parseReceive(byte[] data)
    {
    	return new String( data );
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
    protected String parseReceive(byte[] data)
    {
    	return new String( data );
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