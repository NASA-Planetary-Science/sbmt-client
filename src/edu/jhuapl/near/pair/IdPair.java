package edu.jhuapl.near.pair;

public class IdPair
{
	public int id1;
	public int id2;
	public IdPair(int id1, int id2)
	{
		this.id1 = id1;
		this.id2 = id2;
	}
	
	/**
	 * Add a numbers to each id so that the next block of a specified size
	 * is stored in this class.
	 * @param val
	 */
	public void nextBlock(int size)
	{
		id1 = id2;
		id2 += size;
	}
	
	/**
	 * Subtract numbers from each id so that the previous block of a specified size
	 * is stored in this class.
	 * @param val
	 */
	public void prevBlock(int size)
	{
		id2 = id1;
		id1 -= size;
	}
	
}
