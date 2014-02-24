/**
 * @(#)Board.java
 *
 *
 * @author
 * @version 1.00 2014/2/22
 */


public class Board {

	char board[][];
	int width;
	int height;

    public Board(char[][] gameB) {
    	board = gameB;
    }

    public Board(int x, int y) {
    	width = x;
    	height = y;
    	//Generate random board with dimensions
    }


}