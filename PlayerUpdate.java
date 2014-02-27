/**
 * @(#)PlayerUpdate.java
 *
 *
 * @author
 * @version 1.00 2014/2/24
 */


public class PlayerUpdate implements Sendable<PlayerUpdate>{

	//enum {}

    public PlayerUpdate() {
    }

	public PlayerUpdate getCopy()
    {
    	return new PlayerUpdate();
    }

    public byte[] getBytes()
    {
    	return new byte[5];
    }

}