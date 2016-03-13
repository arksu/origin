package com.a4server.gameserver.model;

/**
 * блокирока игрового объекта для различных действий
 * Created by arksu on 13.03.16.
 */
public class GameLock implements AutoCloseable
{
	private final GameObject _object;

	public GameLock(GameObject object)
	{
		_object = object;
	}

	@Override
	public void close() throws Exception
	{
		_object.unlock();
	}
}
