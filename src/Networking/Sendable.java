package Networking;

public interface Sendable<T>
{
	public byte[] getBytes();

	public T getCopy();
}
