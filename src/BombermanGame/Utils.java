package BombermanGame;

public class Utils {

	public Utils() {}
	
	public static int byteToInt(byte b) throws NumberFormatException
	{
		return Integer.parseInt( new String( new byte[]{ b } ) );
	}

}
