
public class PlayerCommand
{
	public String PlayerName;

	public float Time;

	public PlayerCommandType Command;

	public int Id;

	public PlayerCommand(PlayerCommandType command, float time, int id)
	{
		this.Command = command;
		this.Time = time;
		this.Id = id;

		this.PlayerName = "";
	}
}
