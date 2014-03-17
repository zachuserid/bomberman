import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
 
 
public class B_ServerMainTest
{
    private B_ServerMain net;
     
    /**
     * Default constructor for test class ChequingAccountTest
     */
    public B_ServerMainTest()
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
        net = new B_ServerMain();        
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
 
     /**
     * Verify that the balance in a newly created account is 
     * the amount passed to the constructor.
     */
    @Test
    public void NetworkInitializeTest()
    {
        
        B_ServerNetworkHandler network = new B_ServerNetworkHandler(8090, 2);
        assertEquals(true, network.Initialize());
        assertEquals(received, network.getData());
    }
 
    /**
     * Verify that the inherited deposit method works.
     */
    @Test
    public void runTest()
    {
        int counter =0;
        while(counter==0)
        {
        Thread testLogger = new Thread(new Runnable());
        if(commands.wait())
        {
            assertEquals(false, shutdown);
            assertEquals(p ,w.AddPlayer(command.PlayerName));
            assertEquals(w.getPlayerCount(),PlayersOnMap);
        }
        Thread testLogger1 = new Thread(new Runnable());
        Thread testLogger2 = new Thread(new Runnable());
        if(commands.wait())
        {
            assertEquals(false, shutdown);
            assertEquals(p ,w.AddPlayer(command.PlayerName));
            assertEquals(w.getPlayerCount(),PlayersOnMap);
        }
    }
        assertEquals(received, network.getData());
        assertEquals(true, updates);
        assertEquals(true, network.Stop());
    }
}
