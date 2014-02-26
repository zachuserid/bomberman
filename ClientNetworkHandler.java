/**
 * @(#)ClientNetworkHandler.java
 *
 *
 * @author
 * @version 1.00 2014/2/24
 */


public class ClientNetworkHandler extends NetworkHandler<PlayerUpdate[], char[][]> {

	//Accept a new return buffer to fill with <R> type data
    public ClientNetworkHandler() {
    }

    public void sendData(PlayerUpdate[] data){
    	
    	byte[] parsed = parseSend(data);
    	
    }
	
	public char[][] getData(){
		
		
		return null;
		
	}

	
	protected char[][] parseReceive(){
		
	}
	
	protected byte[] parseSend
	
	
    /*
     * lock on the write buffer and buffer the packet
     */
    void bufferData(DatagramPacket packet){

    	String buf = new String(packet.getData());

		byte bytes[] = buf.getBytes();

		updateWritable.acquireUninterruptibly();
		updateReadable.acquireUninterruptibly();

		for (int j=0; j<bytes.length; j++){
			playerUpdateBufferIn[uIn++] = bytes[j];
		}

		updateReadable.release();
		updateWritable.release();


    }
    

}