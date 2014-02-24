/**
 * @(#)NetworkHandler.java
 *
 *
 * @author
 * @version 1.00 2014/2/24
 */

//R: struct type to return to server/game
public abstract class NetworkHandler<R> {

    public NetworkHandler() {

    }

    abstract R[] getData();

    abstract void sendData();


}