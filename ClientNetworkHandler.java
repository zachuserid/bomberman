/**
 * @(#)ClientNetworkHandler.java
 *
 *
 * @author
 * @version 1.00 2014/2/24
 */


public class ClientNetworkHandler<S, R> extends NetworkHandler<S, R> {

	//Accept a new return buffer to fill with <R> type data
    public ClientNetworkHandler(R[] ret) {
    	super(ret);
    }

	R[] getData(){
		return (R[])null;
	}

    void sendData(S data){
    }

}