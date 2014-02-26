/**
 * @(#)Text2.java
 *
 *
 * @author
 * @version 1.00 2014/2/25
 */


public interface Sendable<T> {
	/*
	 * Method to return a sendable
	 * representation of the object.
	 * 
	 * Should be synchronized on the T object
	 */
	public byte[] getBytes();

}

/*
 * 2D array to 1D byte array:

	public byte[] getBytes(){
		byte theBytes[] = new byte[(WIDTH * HEIGHT) + 1];
		for (int i=0; i<WIDTH; i++){
			for (int j=0; j<HEIGHT; j++){
				theBytes[ (i*WIDTH) + j ] = (byte)grid[i][j];
			}
		}
		return theBytes;
	}

*/