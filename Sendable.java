/**
 * @(#)Text2.java
 *
 *
 * @author
 * @version 1.00 2014/2/25
 */


public interface Sendable<T> {
	//A factory implementation of a copy constructor
	public T getCopy();

	/*
	 * Method to return a sendable
	 * representation of the object.
	 */
	public byte[] getBytes();

}

/*
 * This, or something similar can be the getBytes()
 * method for the World class, as it will be useful
 * for manipulating since the networkhandler will
 * be passed a world to send out

	public byte[] getBytes(){
		byte theBytes[] = new byte[(WIDTH * HEIGHT) + 1];
		for (int i=0; i<WIDTH; i++){
			for (int j=0; j<HEIGHT; j++){
				theBytes[ (i*WIDTH) + j ] = (byte)ground[i][j];
			}
		}
		return theBytes;
	}

*/