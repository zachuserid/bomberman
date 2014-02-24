/**
 * @(#)AbstractServerNetworkHandler.java
 *
 *
 * @author
 * @version 1.00 2014/2/24
 */


public abstract class AbstractServerNetworkHandler<R> extends NetworkHandler<R> {

    public AbstractServerNetworkHandler() {
    }

    void handleNewPlayer(){
    }

    R parseData(){
    	return (R)null;
    }

}