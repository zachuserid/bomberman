/**
 * @(#)World.java
 *
 *
 * @author
 * @version 1.00 2014/2/22
 */


public class World{

	BoardElement world[][];
	int width;
	int height;

    public World(BoardElement[][] gameB) {
    	world = gameB;
    }

    public World(int x, int y) {
    	width = x;
    	height = y;
    	//Generate random board with dimensions
    }
    
    public char getCharAt(int x, int y){
    	return 'c';
    }
    
    public int getHeight(){
    	return 5;
    }
    
    public int getWidth(){
    	return 5;
    }


}
