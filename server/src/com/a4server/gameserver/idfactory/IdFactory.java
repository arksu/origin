package com.a4server.gameserver.idfactory;

/**
 * Created by arksu on 03.01.2015.
 */
public abstract class IdFactory
{
	protected static final IdFactory _instance;
	protected boolean _initialized;

	static
	{
		_instance = new SimpleIdFactory();
	}

	public boolean isInitialized()
	{
		return _initialized;
	}

	public static IdFactory getInstance()
	{
		return _instance;
	}

	public abstract int getNextId();

	/**
	 * return a used Object ID back to the pool
	 * @param id
	 */
	public abstract void releaseId(int id);

	public abstract int size();
}
