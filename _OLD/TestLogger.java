/**
 * @(#)TestLogger.java
 *
 *
 * @author
 * @version 1.00 2014/2/23
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TestLogger {


	File outputFile;
	BufferedWriter writer;

    public TestLogger(String fileName) {


    	try {
			outputFile = new File(fileName);

			// if file doesnt exists, then create it
			if (!outputFile.exists()) {
				outputFile.createNewFile();
			}

			writer = new BufferedWriter(new FileWriter(outputFile.getAbsoluteFile()));

		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public void log(String line){
    	try {
    		writer.write(line);

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public void finish(){
    	try {
        	writer.close();
    	} catch (Exception e){}
    }


}