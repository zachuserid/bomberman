import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NetworkHandlerTest
{
    private NetworkHandler net;
    
    
    /**
     * Default constructor for test class NetworkHandlerTest
     */
    public NetworkHandlerTest()
    {
    }

    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     */
    @Before
    public void setUp()
    {
        net = new NetworkHandler();
    }

    /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    @After
    public void tearDown()
    {
        
    }
    
    @Test
    public void NetworkInitializeTest()
    {
        assertEquals(true, recieveThread.start());
        assertEquals(true, sendThread.start());
    }
    
    public void startSender()
    {
        assertEquals(true, startSender());
    }
    
    public void startReceiver()
    {
        assertEquals(true, startReceiver());
    }
        
    
    public void blockAndReceive()
    {
        DatagramPacket packet = new DatagramPacket(new byte[64000], 64000);
        assertEquals(parseReceiver(packet.getData()), toReturn);
    }
    
    public void SenderThreadMethod()
    {
        if(active.equals(true))
        {
            assertEqual(false, SenderThreadMethod.b.isEmpty());
            assertEqual(d, parseSend(b.get(i)));
        }
       
    }
    
    public void ReceiverThreadMethod()
    {
        if(active.equals(true))
        {
            asserEqual(!null, packet);
            assertEqual(true, preProcessPacket(packet));
        }
    }
}
