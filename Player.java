/**
 * @(#)Player.java
 *
 * An instanciable player class
 * to be operated by a user.
 * Will send movements to the server
 *
 * @author
 * @version 1.00 2014/2/23
 */


public class Player {

	private int X;
	private int Y;
	private int health;
	//the character representation of the player
	private char ID;
	private boolean alive;

    public Player(int x, int y, int h, char id, boolean a) {
    	X = x;
    	Y = y;
    	health = h;
    	ID = id;
    	alive = a;
    }

    /*
     * Take the number of damange given
     * Return false if the player is dead,
     * by this damage or previously
     */
    public boolean takeDamage(int dmg){
    	if ( alive ){
    		health -= dmg;

    		if ( health <= 0 ){
    			alive = false;
    		}
    	}

    	return alive;
    }

    //take one damage, using above function
    public boolean takeDamage(){
    	return takeDamage(1);
    }

	public boolean isAlive(){
		return alive;
	}
	
	public void update(){}
	
	private void handleInput(){
		
	}

}
