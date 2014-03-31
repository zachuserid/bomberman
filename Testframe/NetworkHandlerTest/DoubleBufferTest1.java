import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayDeque;
import java.util.ArrayList;


public class DoubleBufferTest1<T>
{
    private DoubleBuffer net;
    protected ArrayDeque<T> bufferA, bufferB;
    /**
     * Default constructor for test class ChequingAccountTest
     */
    public DoubleBufferTest1()
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
        net = new DoubleBuffer();        
    }
//
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
    public void swapBufferTestfail()
    {
        
        assertEquals(bufferA, bufferB);
        
        //net = new DoubleBuffer();
        //assertEquals(bufferA, bufferB);
    }

    /**
     * Verify that the inherited deposit method works.
     */
    @Test
    public void readAllTest()
    {
        char[] array = new char[12800];
        char[] bufferAarray = new char[6400];
        char[] bufferBarray = new char[6400];
        for(int k=0; k<=6400 ; k++)
        {
            //bufferAarray[k] = DoubleBuffer.get.bufferA[k];
        }
        for(int z=0; z<=6400 ; z++)
        {
            //bufferBarray[z] = DoubleBuffer.get.bufferB[z];
          }
          
        for(int i=0; i<= 6400; i++)
        {
        //array[i] = (bufferB[i]);
        }
        for(int j = 0 ; j<= 6400 ; j++)
        {
        //array [j] = (bufferA[j]);
        }
        assertEquals(array, DoubleBuffer.toReturn);
       
       
    }

    @Test
    public void readTest()
    {
        assertEquals(getRead(),toReturn);
    }
    
    @Test
    public void writeTest()
    {
        assertEquals(data, getWrite);
    }
    
    @Test
    public void writeAllTest()
    {
        assertEquals(data, getWrite);
    }
    
}