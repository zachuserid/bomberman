import java.util.ArrayDeque;
import java.util.ArrayList;

public class DoubleBuffer<T> {

    //members

    protected boolean bA;

    protected ArrayDeque<T> bufferA, bufferB;

    //constructor

    public DoubleBuffer() {
        bufferA = new ArrayDeque<T>();
        bufferB  = new ArrayDeque<T>();
    }
    
    public ArrayDeque<T> getBufferA()
    {
        return bufferA;
        
    }
    
    public ArrayDeque<T> getBufferB()
    {
        return bufferB;
        
    }
    
    
    //methods

    protected ArrayDeque<T> getWrite()
    {
        if(this.bA) return this.bufferA;
        return this.bufferB;
    }

    protected ArrayDeque<T> getRead()
    {
        if(this.bA) return this.bufferB;
        return this.bufferA;
    }

    //swaps the buffers
    protected void swapBuffer()
    {
        synchronized(this.getWrite())
        {
            synchronized(this.getRead())
            {
                this.bA = !this.bA;
            }
        }
    }

    public ArrayList<T> readAll(boolean swap)
    {
        //Swap the buffers
        if (swap) this.swapBuffer();

        //Create auxiliary array
        ArrayDeque<T> b = this.getRead();
        ArrayList<T> toReturn = new ArrayList<T>();

        //Populate and return array
        synchronized(b)
        {
            while(!b.isEmpty()) toReturn.add(b.pop());  
            //System.out.println("Reading all from buffer with size " + b.size());
        }

        return toReturn;
    }

    public T read(boolean swap)
    {
        //swap if required
        if (swap) this.swapBuffer();

        //Get working array and auxiliary variable
        ArrayDeque<T> b = this.getRead();
        T toReturn;

        synchronized(b)
        {
            toReturn = b.pop();
            //System.out.println("Read from buffer with size: " + b.size());
        }
        return toReturn;
    }

    public void write(T data, boolean swap)
    {
        ArrayDeque<T> b = this.getWrite();
        synchronized(b)
        {
            b.add(data);
            //System.out.println("Writing to buffer. Size " + b.size());
        }

        //swap the buffer
        if (swap) this.swapBuffer();
    }

    public void writeAll(T []data, boolean swap)
    {   
        ArrayDeque<T> b = this.getWrite();
        synchronized(b)
        {
            for (int i=0; i<data.length; i++)
            {
                b.add(data[i]);
            }
        }

        //swap the buffer
        if (swap) this.swapBuffer();
    }

}