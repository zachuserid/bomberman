package BombermanGame;

public enum GridObject
{
	Empty((byte)0),
	Wall((byte)1),
	HiddenDoor((byte)2),
	Door((byte)3),
	HiddenPowerUp1((byte)4),
	HiddenPowerUp2((byte)5),
	HiddenPowerUp3((byte)6),
	PowerUp1((byte)7),
	PowerUp2((byte)8),
	PowerUp3((byte)9),
	Bomb((byte)10),
	Player((byte)11);
	
	byte c;
	
	public byte getByte() {return this.c;}
	
	GridObject(byte i)
	{
		this.c = i;
	}
}
