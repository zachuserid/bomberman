package BombermanGame;

public class Utils {

	public Utils() {}
	
	//TODO: Create a ConversionOverflowException for int > 127. Throw it.
	
	//Convert a single byte into an int
	public static int byteToInt(byte b) throws NumberFormatException
	{
		return Integer.parseInt( new String( new byte[]{ b } ) );
	}
	
	//Convert an int < 127 into a single byte
	public static byte intToByte(int i)
	{
		String s = ""+i;
		return s.getBytes()[0];
	}
	
	/*
	 * Conversions to and from byte array/int.
	 * Source:
	 *   http://stackoverflow.com/questions/5616052/how-can-i-convert-a-4-byte-array-to-an-integer
	 */
	
	//Convert 4 bytes into an integer
	public static int byteArrayToInt(byte[] bytes)
	{
		int value = ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16)
		          | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
		return value;
	}
	
	//Convert an integer into 4 bytes
	public static byte[] intToByteArr(int value)
	{
		byte bytes[] = new byte[4];
		bytes[0] = (byte) ((value >> 24) & 0xFF);
		bytes[1] = (byte) ((value >> 16) & 0xFF);
		bytes[2] = (byte) ((value >> 8) & 0xFF);
		bytes[3] = (byte) (value & 0xFF);
		return bytes;
	}
	
	//Used when the byte array represents a string which is
	// a literal of an integer.
	public static int byteArrToStrInt(byte[] bytes)
	{
		return Integer.parseInt(new String(bytes));
	}
	
	//used to convert an int into the byte representation
	// of the string literal of the int
	public static byte[] intToStrByteArr(int i)
	{
		String s = new String(""+i).trim();
		//pad s with 0's
		while (s.length() < 4)
			s = "0"+s;
		return s.getBytes();
	}
	
	//Returns the string of this int padded to 4 digits
	public static String intToPaddedStr(int i)
	{
		return intToPaddedStr(i, 4);
	}
	
	public static String intToPaddedStr(int i, int pad)
	{
		String s = new String(""+i).trim();
		
		//pad s with 0's
		while (s.length() < pad)
			s = "0"+s;
		
		return s;
	}
}
