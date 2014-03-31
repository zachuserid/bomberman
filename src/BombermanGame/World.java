package BombermanGame;

import java.util.ArrayList;

import BombermanNetworkGame.U_BombData;
import BombermanNetworkGame.U_ClientData;
import BombermanNetworkGame.U_PlayerData;
import Networking.Sendable;

/*
 * the world is the model for the bomberman game
 * requests to alter the model are done via associated methods that return WorldActionOutcome
 * a world also has a list of entities
 */
public class World implements Sendable<World>
{
	//this is the backing character grid
	protected GridObject[][] grid;

	//list of players in the world for creating networkpacket
	protected ArrayList<B_Player> players;

	protected ArrayList<Bomb> bombs;

	protected int playersDead = 0;

	protected boolean atDoor = false;

	//the width of the world in "squares" (not pixels)
	public int getGridWidth() {return grid[0].length;}
	public int getGridHeight() {return grid.length;}

	public int getPlayerCount()
	{
		return this.players.size();
	}

	protected Point getNextPlayerLocation() 
	{ 
		for(int x = 0; x < this.getGridWidth(); x++) 
			for(int y = 0; y < this.getGridHeight(); y++)
				if(this.getElementAt(x, y)==GridObject.Empty)
					{
						boolean valid = true;

						for(B_Player p : this.players)
						{
							if(p.getX() == x && p.getY() == y) valid = false;
						}

						if(valid)return new Point(x,y);
					}

		return Point.Zero();
	}

	public void setGrid(GridObject[][] newGrid)
	{
		this.grid = newGrid;
	}

	public void setPlayers(ArrayList<B_Player> players)
	{
		this.players = players;
	}

	public void setBombs(ArrayList<Bomb> bombs)
	{
		this.bombs = bombs;
	}
	
	public GridObject[][] getGrid()
	{
		return this.grid;
	}

	public B_Player[] getPlayers()
	{
		B_Player playerArr[] = new B_Player[this.players.size()];
		this.players.toArray(playerArr);
		return playerArr;
	}

	public B_Player getWinner()
	{
		if(!this.isGameOver()) return null;

		if(players.size() == this.playersDead) return null;

		if(this.players.size() - this.playersDead == 1)
		{
			for(B_Player p : this.players)
			{
				if(p.isAlive()) return p;
			}
		}

		for(B_Player p : this.players)
		{
			if(this.getElementAt(p.getLocation()) == GridObject.Door)
				return p;
		}

		return null;
	}

	public ArrayList<Bomb> getBombs()
	{
		return this.bombs;
	}

	@Override
	public byte[] getBytes()
	{
		int width = this.getGridWidth();
		int height = this.getGridHeight();

		byte worldBytes[] = new byte[width * height];
		for (int i=0; i<width; i++)
		{
			for (int j=0; j<height; j++)
			{
				worldBytes[ (j * height) + i ] = this.grid[j][i].getByte();
			}
		}
		return worldBytes;
	}

	@Override
	public World getCopy()
	{
		World world = new World(this.grid);

		for(B_Player p : this.players) 
			world.AddExistingPlayer(p);
		
		for(Bomb b : this.bombs) 
			world.AddExistingBomb(b);
		
		return world;
	}

	public boolean isGameOver() {return this.playersDead >= this.players.size() -1 || this.atDoor;}

	//add more stuff here
	public void setUpdatedData(U_ClientData data)
	{
		this.playersDead = 0;
		
		//updates the local grid with the sent grid
		this.grid = data.world.grid;
		
		U_PlayerData[] players = data.players;
		
		//updates all players
		for(int i = 0; i < players.length; i++)
		{
			B_Player player = this.players.get(i);
			
			if(players[i].isAlive)
			{
				player.Revive();
				player.setLocation(players[i].position);
				player.setBombCount(players[i].numBombs);
				player.setKillCount(players[i].kills);
				
				if(this.getElementAt(players[i].position) == GridObject.Door)
					this.atDoor = true;
			}
			else
			{
				player.Kill();
				this.playersDead++;
			}
		}

		//TODO: update bomb positions based on bomb getter methods in data
		ArrayList<Bomb> l = (ArrayList<Bomb>) this.bombs.clone();
		U_BombData[] bombs = data.bombs;
		
		//updates all bombs
		for(int n = 0; n < bombs.length; n++)
		{
			boolean found = false;
			
			int index = 0;

			//bomb not found in local or there are no bombs left in local list
			while(!found && index < l.size())
			{
				Bomb b = l.get(index);
				//if they are the same bomb, found in local
				if(b.getX() == bombs[n].position.X && b.getY() == bombs[n].position.Y)
				{
					if(!b.isConfirmed()) b.confirm();
					
					b.setTimer(bombs[n].time);

					l.remove(index);
					found = true;
				}
				else index++;
			}
			
			//if the bomb was not in the local list
			if(!found)
			{
				Bomb b = new Bomb(bombs[n].bName, bombs[n].position, bombs[n].radius, bombs[n].time);
				
				b.confirm();
				
				this.bombs.add(b);
			}	
		}
		
		//if there are still local bombs
		for(int i = 0; i < l.size(); i++)
		{
			System.out.println("Local " + i);
			Bomb b = l.get(i);
			if(b.isConfirmed() || b.getTime() <= 0)
				this.bombs.remove(b);
		}
	}

	//returns the (currently)char elements at x,y
	public GridObject getElementAt(int x, int y)
	{
		return this.grid[y][x];
	}

	public GridObject getElementAt(Point p)
	{
		return this.grid[p.Y][p.X];
	}

	public void SetElementAt(int x, int y, GridObject o)
	{
		this.grid[y][x] = o;
	}

	protected void SetElementAt(Point p, GridObject o)
	{
		this.SetElementAt(p.X, p.Y, o);
	}


	//creates a new world with a grid
	public World(GridObject[][] grid)
	{
		this.grid = grid;

		this.players = new ArrayList<B_Player>();
		this.bombs = new ArrayList<Bomb>();
	}

	//adds an entity to world (should be mapped to an outside controller to be dynamic)
	public B_Player AddPlayer(String name)
	{
		if(this.players.size() >= 4) return null;

		B_Player p = new B_Player(name, this.getNextPlayerLocation());

		this.players.add(p);

		return p;
	}

	public void Update(float time)
	{
		if(this.isGameOver()) return;

		//removes bombs that have exploded
		for(int i = 0; i < this.bombs.size(); i++)
			if(this.bombs.get(i).isDetonated())
				this.bombs.remove(i);

		for(Bomb b : this.bombs)
		{
			b.Update(time, this);

			if(b.isDetonated())
				this.explode(b);
		}
	}

	protected void explode(Bomb b)
	{
		System.out.println("range " + b.getRange());
		
		this.SetElementAt(b.getLocation(), GridObject.Empty);

		B_Player player = null;

		for(B_Player p : this.players)
			if(b.name == p.name)
			{
				player = p;
				break;
			}
		//on it
		this.explodeLocation(new Point(b.getX(), b.getY()), player);

		//right
		for(int i = 1; i <= b.getRange(); i++)
		{
			int x = b.getX() + i;
			if(x > this.getGridWidth() - 1) break;
			else
			{
				if(this.explodeLocation(new Point(x, b.getY()), player)) break;
			}
		}
		//left
		for(int i = 1; i <= b.getRange(); i++)
		{
			int x = b.getX() - i;
			if(x < 0) break;
			else
			{
				if(this.explodeLocation(new Point(x, b.getY()), player)) break;
			}
		}
		//up
		for(int i = 1; i <= b.getRange(); i++)
		{
			int y = b.getY() - i;
			if(y < 0) break;
			else
			{
				if(this.explodeLocation(new Point(b.getX(),y), player)) break;
			}
		}
		//up
		for(int i = 1; i <= b.getRange(); i++)
		{
			int y = b.getY() + i;
			if(y > this.getGridHeight() - 1) break;
			else
			{
				if(this.explodeLocation(new Point(b.getX(),y), player)) break;
			}
		}
	}

	protected boolean explodeLocation(Point p, B_Player player)
	{
		GridObject o = this.getElementAt(p.X, p.Y);
		if(o == GridObject.Wall)
		{
			this.SetElementAt(new Point(p.X, p.Y), GridObject.Empty);
			return true;
		}
		else if(o == GridObject.HiddenDoor)
		{
			this.SetElementAt(new Point(p.X, p.Y), GridObject.Door);
			return true;
		}
		else if(o == GridObject.HiddenPowerUp1)
		{
			this.SetElementAt(new Point(p.X, p.Y), GridObject.PowerUp1);
			return true;
		}
		else if(o == GridObject.HiddenPowerUp2)
		{
			this.SetElementAt(new Point(p.X, p.Y), GridObject.PowerUp2);
			return true;
		}
		else if(o == GridObject.HiddenPowerUp3)
		{
			this.SetElementAt(new Point(p.X, p.Y), GridObject.PowerUp3);
			return true;
		}
		else
		{
			for(B_Player other : this.players)
				if(other.isAlive())
				{
					if(p.X == other.getX() && p.Y == other.getY())
					{
						player.setKillCount(player.getKillCount() + 1);
						other.Kill();
						this.playersDead++;
						return true;
					}
				}
		}

		return false;
	}

	//these are all the requests for an entity to try and do something
	public WorldActionOutcome TryMoveLeft(B_Player e)
	{
		int x = e.getX() - 1;
		if(x >= 0) return this.TryMove(new Point(x, e.getY()), e);

		return WorldActionOutcome.DeniedStatic;
	}

	public WorldActionOutcome TryMoveRight(B_Player e)
	{
		int x = e.getX() + 1;
		if(x < this.getGridWidth()) return this.TryMove(new Point(x, e.getY()), e);

		return WorldActionOutcome.DeniedStatic;
	}

	public WorldActionOutcome TryMoveUp(B_Player e)
	{
		int y = e.getY() - 1;
		if(y >= 0) return this.TryMove(new Point(e.getX(), y), e);

		return WorldActionOutcome.DeniedStatic;
	}

	public WorldActionOutcome TryMoveDown(B_Player e)
	{
		int y = e.getY() + 1;
		if(y < this.getGridHeight()) return this.TryMove(new Point(e.getX(), y), e);

		return WorldActionOutcome.DeniedStatic;
	}

	protected WorldActionOutcome TryMove(Point pos, B_Player e)
	{
		GridObject o = this.getElementAt(pos);

		switch(o)
		{
			case Empty:
				e.setLocation(pos);
				return WorldActionOutcome.Approved;
			case PowerUp1:
				e.setLocation(pos);
				//e.setPowerup(Powerup.PowerupA);
				e.setBombCount(e.bombCount + 5);
				this.SetElementAt(pos, GridObject.Empty);
				return WorldActionOutcome.Approved;
			case PowerUp2:
				e.setLocation(pos);
				//e.setPowerup(Powerup.PowerupB);
				e.setBombRange(e.getBombRange() + 1);
				this.SetElementAt(pos, GridObject.Empty);
				return WorldActionOutcome.Approved;
			case PowerUp3:
				e.setLocation(pos);
				//e.setPowerup(Powerup.PowerupC);
				this.SetElementAt(pos, GridObject.Empty);
				return WorldActionOutcome.Approved;
			case Door:
				e.setLocation(pos);
				this.atDoor = true;
				return WorldActionOutcome.Approved;
		case HiddenDoor:
			break;
		case Wall:
			break;
		default:
			break;
		}
		return WorldActionOutcome.DeniedStatic;
	}

	protected void SwapObjects(Point a, Point b)
	{
		GridObject o1 = this.getElementAt(a);
		GridObject o2 = this.getElementAt(b);

		this.SetElementAt(a, o2);
		this.SetElementAt(b, o1);
	}

	public WorldActionOutcome TryPlantBomb(B_Player e)
	{
		if(this.getElementAt(e.getLocation()) == GridObject.Bomb) return WorldActionOutcome.DeniedDynamic;

		if(e.getBombCount() <= 0) return WorldActionOutcome.DeniedDynamic;
		
		e.setBombCount(e.getBombCount()-1);
		
		this.SetElementAt(e.getLocation(), GridObject.Bomb);
		this.bombs.add(new Bomb(e.name, e.getLocation(), e.getBombRange(), 5));

		return WorldActionOutcome.Approved;
	}
	
	public WorldActionOutcome TryUsePowerup(B_Player p)
	{
		if (!p.isAlive() || p.getPowerup() == Powerup.None) 
			return WorldActionOutcome.DeniedStatic;
		
		//TODO: Perform powerup specific attribute change to p or world..
		System.out.println(p.getName()+" is using powerup " + p.getPowerup());
		
		p.setPowerup(Powerup.None);
		
		return WorldActionOutcome.Approved;
	}


	public void printGrid()
	{
		int w = this.getGridWidth();
		int h = this.getGridHeight();
		for (int i=0; i<w; i++)
		{
			for (int j=0; j<h; j++)
			{
				System.out.print(this.getElementAt(i, j));
			}
			System.out.println("");
		}
	}	

	protected void AddExistingPlayer(B_Player p)
	{
		B_Player copy = p.getCopy();

		this.players.add(copy);

		//this.SetElementAt(copy.getLocation(), copy.getGridObject());
	}
	
	protected void AddExistingBomb(Bomb b)
	{
		Bomb copy = b.getCopy();

		this.bombs.add(copy);

		//this.SetElementAt(copy.getLocation(), copy.getGridObject());
	}
}