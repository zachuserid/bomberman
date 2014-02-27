/**
 * @(#)NetworkHandlerTest.java
 *
 *
 * @author
 * @version 1.00 2014/2/26
 */

public class NetworkHandlerTest {

    public NetworkHandlerTest() {
    }


    public static void main(String args[])
    {
    	if ( args.length != 2 ){
    		System.out.println("Insufficient arguments");
    		return;
    	}

    	System.out.println("Starting network handler");
    }


}

class TestServer extends ServerNetworkHandler<String, String>{

	//listen port
	int port;

	//CTOR
	public TestServer(int port){
		this.port = port;
	}

	//return a R(eceive) type object from parsing raw packet data
	@Override
    protected String parseReceive(byte[] data)
    {
    	return new String( data );
    }

    //return raw packet data from an S(end) type object
    @Override
    protected byte[] parseSend(String data){
    	return data.getBytes();
    }

    //sends data for either a client or server
    @Override
    protected abstract void Send(byte[] data){

    }

    //a copy method to be implemented at lowest level
    @Override
    public abstract S getSendCopy(S original);

	//a copy of the receive type object given
	@Override
    public abstract R getReceiveCopy(R original);

}



class testClient extends ClientNetworkHandler<String, String> {

	//return a R(eceive) type object from parsing raw packet data
	@Override
    protected abstract R parseReceive(byte[] data);

    //return raw packet data from an S(end) type object
    @Override
    protected abstract byte[] parseSend(S data);

    //binds socket for either client or server
    @Override
    protected abstract void BindSocket(DatagramSocket socket) throws SocketException;

    //sends data for either a client or server
    @Override
    protected abstract void Send(byte[] data);

    //a copy method to be implemented at lowest level
    @Override
    public abstract S getSendCopy(S original);

	//a copy of the receive type object given
	@Override
    public abstract R getReceiveCopy(R original);

}